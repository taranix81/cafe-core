# Cafe Shell

Cafe Shell is a lightweight, annotation-driven command-line framework built on top of [Cafe Beans](../cafe-beans). It
enables you to quickly create CLI applications with dependency injection, automatic command mapping, robust argument
parsing, and extensible command resolution.

---

## Features

- **Annotation-based Command Registration:** Define CLI commands as Java classes using `@CafeCommand` and mark the
  execution method with `@CafeCommandRun`.
- **Dependency Injection:** Integrates with Cafe Beans for DI — inject services and configuration into commands and
  the shell runtime.
- **Apache Commons CLI Integration:** Leverages Commons CLI's `Options` and `CommandLineParser` for argument parsing.
- **Command Dependency Ordering:** Declare execution order with `dependsOn` on `@CafeCommand`.
- **Conditional Execution:** Control when commands run with `@CafeCommandExecutionCondition`.
- **Automatic Help Generation:** Built-in `-h`/`--help` command; invalid arguments trigger help automatically.
- **Runtime Object Repository:** Command return values are stored and injectable into subsequent commands.
- **`@CafePrimary` Support:** Control bean injection precedence when multiple sources of the same type exist.
- **Colored Console Output:** `CafeConsole` utility provides ANSI color helpers.

---

## Installation

Add the following to your Maven `pom.xml`:

```xml
<dependency>
    <groupId>org.taranix.cafe</groupId>
    <artifactId>cafe-shell</artifactId>
    <version>0.0.5-SNAPSHOT</version>
</dependency>
```

---

## Quick Start

### 1. Entry point

Instantiate `CafeShell` with your `@CafeApplication`-annotated config class and call `run`:

```java
public class Main {
    public static void main(String[] args) {
        CafeShell shell = new CafeShell(MyShellConfig.class);
        System.exit(shell.run(args));
    }
}
```

### 2. Configuration class

```java
@CafeApplication
public class MyShellConfig { }
```

### 3. Define a command

```java
@CafeCommand(command = "greet", longCommand = "greet-all", description = "Prints a greeting")
public class GreetCommand {
    @CafeCommandRun
    public void run() {
        System.out.println("Hello from Cafe Shell!");
    }
}
```

Run with: `java -jar app.jar --greet-all` or `java -jar app.jar -greet`

---

## Annotation Reference

### `@CafeCommand`

Placed on a **class** to declare it as a CLI command.

| Attribute | Type | Default | Description |
|---|---|---|---|
| `command` | `String` | `""` | Short option flag (e.g. `"h"` → `-h`) |
| `longCommand` | `String` | `""` | Long option flag (e.g. `"help"` → `--help`) |
| `description` | `String` | `""` | Help text shown by `-h` |
| `noOfArgs` | `int` | `0` | Number of accepted arguments |
| `argumentName` | `String` | `""` | Argument name shown in help |
| `required` | `boolean` | `false` | Whether the option is required |
| `hasOptionalArgument` | `boolean` | `false` | Whether the argument is optional |
| `valueSeparator` | `char` | `','` | Separator for multi-value arguments |
| `dependsOn` | `Class<?>[]` | `{}` | Other command classes that must execute first |

A command with neither `command` nor `longCommand` set is a **default command** — it receives any positional
(non-option) arguments left after option parsing.

### `@CafeCommandRun`

Placed on exactly **one method** inside a `@CafeCommand` class. This method is called when the command is triggered.

### `@CafeCommandExecutionCondition`

Placed on a `@CafeCommand` class to control whether it runs.

| Attribute | Type | Default | Description |
|---|---|---|---|
| `strategy` | `CafeExecutionStrategy` | `ALWAYS_RUN` | Execution condition |

**`CafeExecutionStrategy` values:**

| Value | Meaning |
|---|---|
| `ALWAYS_RUN` | Execute unconditionally (default) |
| `NON_OPTED_COMMAND_EXISTS` | Execute only when a non-option (positional) argument is present |

Example — a setup command that only runs when the user provides a positional argument:

```java
@CafeCommand
@CafeCommandExecutionCondition(strategy = CafeExecutionStrategy.NON_OPTED_COMMAND_EXISTS)
public class SetupCommand {
    @CafeCommandRun
    public void run() { /* ... */ }
}
```

---

## Command Method Parameters

The `@CafeCommandRun` method can declare parameters in any order. At execution time the shell resolves them:

| Parameter type | Source |
|---|---|
| `CafeCommandArguments` | Parsed CLI values for this option |
| Any `@CafeSingleton` bean | Injected from the DI container |
| Return type of a previous command | Stored in the runtime repository by the prior command |

```java
@CafeCommand(command = "process", noOfArgs = 1, argumentName = "file")
public class ProcessCommand {
    @CafeCommandRun
    public Report run(CafeCommandArguments args, DataLoader loader, ParsedInput previous) {
        // args         — CLI values passed to -process
        // loader       — injected singleton bean
        // previous     — object returned by an earlier command
        return new Report(/* ... */);
    }
}
```

---

## Command Dependency Ordering

Use `dependsOn` to declare that one command must run after another:

```java
@CafeCommand(command = "export", dependsOn = { ParseCommand.class })
public class ExportCommand {
    @CafeCommandRun
    public void run(ParseResult result) { /* result produced by ParseCommand */ }
}

@CafeCommand(command = "parse")
public class ParseCommand {
    @CafeCommandRun
    public ParseResult run(CafeCommandArguments args) { return new ParseResult(/* ... */); }
}
```

`CafeCommandRuntimeOrderService` topologically sorts the active commands before execution, ensuring `ParseCommand`
always runs first even if `-export` appears before `-parse` on the command line.

---

## `@CafePrimary` on Command Methods

When multiple beans of the same type exist (factory bean + command-produced value), annotate the `@CafeCommandRun`
method with `@CafePrimary` to give its return value priority for injection into later commands:

```java
@CafeCommand(command = "produce")
public class ProduceCommand {
    @CafeCommandRun
    @CafePrimary
    public DataObject run() { return new DataObject(); }
}
```

---

## Configuration

Cafe Shell reads `application.properties` (or `application.yml`) via Cafe Beans. The only built-in key is:

```yaml
cafe:
  shell:
    name: My App   # Used in the help header (default: "Cafe Shell")
```

---

## Console Colors

`CafeConsole` provides ANSI color helpers for terminal output:

```java
import org.taranix.cafe.shell.constants.CafeConsole;
import org.taranix.cafe.shell.constants.CafeConsole.CafeConsoleTextColour;
import org.taranix.cafe.shell.constants.CafeConsole.CafeConsoleBackgroundColour;

String colored = CafeConsole.ansiColouredText(
        "ERROR",
        CafeConsoleTextColour.RED,
        CafeConsoleBackgroundColour.NONE);
System.out.println(colored);
```

Available text colors: `BLACK`, `RED`, `GREEN`, `YELLOW`, `BLUE`, `PURPLE`, `CYAN`, `WHITE`, `NONE`  
Available background colors: same set.

---

## Error Handling and Exit Codes

| Code | Constant | Meaning |
|---|---|---|
| `0` | `CafeShell.SUCCESS` | All commands executed successfully |
| `1` | `CafeShell.ERROR` | Runtime exception during command execution |
| `2` | `CafeShell.WRONG_ARGUMENT_ERROR` | Argument parse failure (help is printed automatically) |

When argument parsing fails, `PrintHelpCommand` is invoked automatically before returning `2`.  
When no option matches and there is no default command, help is also printed.

---

## Architecture

```
CafeShell (extends CafeApplication)
  │
  ├── beforeContextInit()
  │     └── addBeanToContext(this)   ← CafeShell itself is injectable
  │
  ├── getCustomClassResolvers()
  │     └── CafeCommandClassResolver
  │           – discovers @CafeCommand classes during DI scan
  │           – creates one Apache CLI Option per command
  │           – stores Option in repository (injectable as Collection<Option>)
  │
  ├── getCustomMethodResolvers()
  │     └── CafeCommandMethodResolver
  │           – handles @CafeCommandRun methods
  │           – executes method and persists return value
  │
  └── postContextInit()
        └── createApacheOptions()
              – collects all Option beans via getInstances(Option.class)
              – adds them to the Options aggregate
```

### Runtime execution flow

```
CafeShell.execute(args)
  │
  ├── CommandLineParser.parse(options, args)
  │     ├── success → matched Option[] + leftover String[]
  │     └── ParseException → PrintHelpCommand → return 2
  │
  ├── CafeCommandRuntimeService.map(List<Option>)
  │     └── CafeCommandBindingService.getCommandBindings()
  │           – finds CafeCommandOptionBinding for each matched Option
  │           – each binding holds: command instance, executor CafeMethod, Option
  │
  ├── CafeCommandRuntimeService.order(commandRuntimes)
  │     └── CafeCommandRuntimeOrderService.order()
  │           – topological sort by @CafeCommand.dependsOn
  │
  ├── CafeCommandRuntimeService.map(leftArgs)   ← default command (no option)
  │
  └── CafeCommandRuntimeService.run(commandRuntimes)
        – resolves each method parameter (CafeCommandArguments / DI bean / prior result)
        – invokes @CafeCommandRun method
        – persists return value in repository for later commands
```

### Key classes

| Class | Role |
|---|---|
| `CafeShell` | Entry point — extends `CafeApplication`, drives parse → map → order → run |
| `CafeShellFactory` | `@CafeProvider` for `CommandLineParser`, `Options`, `HelpFormatter` |
| `CafeCommandClassResolver` | Resolves `@CafeCommand` classes at DI scan time — creates `Option` beans |
| `CafeCommandMethodResolver` | Resolves `@CafeCommandRun` methods — executes and persists results |
| `CafeCommandBindingService` | Maps CLI `Option` instances to `CafeCommandOptionBinding` pairs |
| `CafeCommandRuntimeService` | Orchestrates map → order → execute pipeline |
| `CafeCommandRuntimeOrderService` | Topological sort of command runtimes by `dependsOn` |
| `CafeCommandArguments` | `@CafeSingleton` — holds parsed CLI values; injected into command methods |
| `CafeCommandRuntime` | Value object: command instance + executor method + arguments |
| `CafeCommandOptionBinding` | Value object: command instance + executor + bound `Option` |
| `PrintHelpCommand` | Built-in `-h`/`--help` command |
| `CafeConsole` | ANSI color utility (not a bean) |

---

## References

- [Cafe Beans](../cafe-beans)
- [Apache Commons CLI](https://commons.apache.org/proper/commons-cli/)
