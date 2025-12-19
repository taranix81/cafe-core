# Cafe Shell

Cafe Shell is a lightweight, annotation-driven command-line framework built on top of [Cafe Beans](../cafe-beans). It
enables you to quickly create CLI applications with dependency injection, automatic command mapping, robust argument
parsing, and extensible command resolution.

---

## Features

- **Annotation-based Command Registration:** Define CLI commands as Java classes or methods using `@CafeCommand` and
  `@CafeCommandRun`.
- **Dependency Injection:** Integrates with Cafe Beans for DI, letting you inject services and configuration into
  commands and the shell runtime.
- **Apache Commons CLI Integration:** Leverages Commons CLI's `Options` and `CommandLineParser` for argument parsing.
- **Custom Command Resolvers:** Extensible mechanism for mapping CLI arguments to command implementations.
- **Automatic Help Generation:** Built-in help command and error handling for invalid or missing arguments.
- **Runtime Object Repository:** Commands can produce objects that are stored and made available for subsequent
  commands.
- **@CafePrimary Support:** Control bean injection precedence when multiple sources of the same type exist.
- **Flexible Parameter Injection:** Inject arguments, beans, services, and runtime-produced objects into your command
  methods.

---

## Installation

Add the following to your Maven `pom.xml`:

```xml
<dependency>
    <groupId>org.taranix.cafe</groupId>
    <artifactId>cafe-shell</artifactId>
    <version>0.0.4-SNAPSHOT</version>
</dependency>
```

Or for Gradle:

```gradle
implementation 'org.taranix.cafe:cafe-shell:0.0.4-SNAPSHOT'
```

---

## Quick Start

### 1. Run Your Shell Application

Simply instantiate `CafeShell` with your configuration class (annotated with `@CafeApplication`) and call `run`:

```java
public class Main {
    public static void main(String[] args) {
        CafeShell cafeShell = new CafeShell(MyShellConfig.class);
        int result = cafeShell.run(args);
        System.exit(result);
    }
}



```

### 2. Configure the Application Context

Set up your root configuration class and any beans you need:

```java
import org.taranix.cafe.beans.annotations.classes.CafeApplication;

@CafeApplication
public class MyShellConfig {
    // Additional beans and configuration (optional)
}
```

### 3. Define Your Commands

Annotate a command class with `@CafeCommand` and a method with `@CafeCommandRun`:

```java
import org.taranix.cafe.shell.annotations.CafeCommand;
import org.taranix.cafe.shell.annotations.CafeCommandRun;

@CafeCommand(command = "greet", description = "Prints a greeting")
public class GreetCommand {
    @CafeCommandRun
    public void run() {
        System.out.println("Hello from Cafe Shell!");
    }
}
```

#### More Complete Example

```java
import org.taranix.cafe.shell.annotations.CafeCommand;
import org.taranix.cafe.shell.annotations.CafeCommandRun;
import java.util.UUID;

@CafeCommand(command = "u", description = "Random UUID", hasOptionalArgument = true, noOfArgs = 1)
public class RandomUUIDCommand {
    @CafeCommandRun
    public UUID run(CafeCommandArguments args) {
        UUID generated = UUID.randomUUID();
        System.out.println("Generated UUID: " + generated);
        return generated;
    }
}
```

---

## Advanced Command Execution and Dependency Injection

### Command Method Parameters

Methods annotated with `@CafeCommandRun` can declare parameters in their signature. These parameters are automatically
resolved and injected at runtime using the following rules:

- **CafeCommandArguments**  
  If a parameter is of type `CafeCommandArguments` (a built-in class), it receives the parsed CLI arguments, mapped from
  Apache Commons CLI options.

- **Beans & Services**  
  Any other parameter is injected as a bean or service, produced by factory configuration classes or registered in the
  DI context.

- **Produced Objects from Previous Commands**  
  If a previous command produced an object, it is stored in the runtime repository and can be injected into subsequent
  commands by declaring a parameter of the matching type.

#### Example

```java
@CafeCommand(command = "do-something")
public class DoSomethingCommand {
    @CafeCommandRun
    public void run(CafeCommandArguments args, MyService myService, DataObject producedByPrevCommand) {
        // args: mapped CLI arguments
        // myService: injected service bean
        // producedByPrevCommand: object produced by another command (if available)
        // Command logic here...
    }
}
```

### Command Result Reuse and @CafePrimary

- Methods annotated with `@CafeCommandRun` can return objects. Returned objects are stored in the runtime repository.
- If multiple objects of the same type are available (e.g., one produced by a command, another by a factory), you can
  control which to inject using the `@CafePrimary` annotation.
    - Annotate the command method with `@CafePrimary` to give its produced object priority over factory beans when
      resolving parameters of that type for later commands.

#### Example

```java
@CafeCommand(command = "produce-data")
public class ProduceDataCommand {
    @CafeCommandRun
    @CafePrimary
    public DataObject run() {
        // This object will be preferred for injection into later commands
        return new DataObject();
    }
}
```

---

## How It Works

- **Startup:** `CafeShell` initializes a dependency-injected context based on your configuration.
- **Command Resolution:** CLI arguments are parsed using Apache Commons CLI. Each option or positional argument is
  mapped to a corresponding `@CafeCommand`.
- **Execution:** Resolved commands are executed in order, according to your logic and the shell runtime.
- **Help and Errors:** Invalid arguments or missing commands cause the shell to print the help screen automatically.

---

## Error Handling & Exit Codes

Cafe Shell includes robust error handling and uses exit codes for clarity:

- `0` — Success
- `1` — Error during execution
- `2` — Wrong or missing argument(s)

If argument parsing fails, the help message is shown.

---

## License

See the root project [LICENSE](../LICENSE) for details.

---

## References

- [Cafe Beans](../cafe-beans)
- [Apache Commons CLI](https://commons.apache.org/proper/commons-cli/)

---

## Contributing

Contributions are welcome! Please open an issue or submit a pull request.
