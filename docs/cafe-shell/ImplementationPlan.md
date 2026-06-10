# Cafe Shell — Implementation Plan

Source-audited as part of the 0.0.5 cycle.  
Items from `ToDo.MD` are incorporated as Phase 3.

---

## Phase 1 — Test Coverage

Cafe Shell currently has **zero tests**. Every class below needs a new test file created under
`cafe-shell/src/test/java/org/taranix/cafe/shell/`.

### 1.1 — CafeCommandClassResolver

**New file:** `.../resolvers/CafeCommandClassResolverTest.java`

| Scenario | Expected |
|---|---|
| Class annotated `@CafeCommand(command="x")` | `supports(CafeCommand.class)` → `true` |
| Class without `@CafeCommand` | `supports(...)` → `false` |
| `resolve` called with valid `@CafeCommand` class | `Option` created with correct short/long name |
| `@CafeCommand(required=true)` | built `Option.isRequired()` → `true` |
| `@CafeCommand(noOfArgs=2, argumentName="file")` | `Option.getArgs()` == 2, `Option.getArgName()` == `"file"` |
| `@CafeCommand(hasOptionalArgument=true)` | `Option.hasOptionalArg()` → `true` |
| `isOptedCommand(Option)` — option with name | `true` |
| `isOptedCommand(Option)` — option with no name (`""`) | `false` |

Commit: `test: CafeCommandClassResolver unit tests`

---

### 1.2 — CafeCommandRuntimeOrderService

**New file:** `.../services/CafeCommandRuntimeOrderServiceTest.java`

| Scenario | Expected |
|---|---|
| Single command, no dependencies | returned as-is |
| Two independent commands | both returned, any order |
| A depends on B → both active | B before A |
| A depends on B, only A active | A returned (B absent — not active) |
| Circular dependency A→B→A | throws or handles gracefully |

Use `CafeCommandRuntime` builder directly — no DI needed.

Commit: `test: CafeCommandRuntimeOrderService topological sort tests`

---

### 1.3 — CafeCommandBindingService

**New file:** `.../services/CafeCommandBindingServiceTest.java`

| Scenario | Expected |
|---|---|
| Option matches registered command | `getCommandBindings()` returns binding with correct instance and method |
| Option has no matching command | binding absent from result |
| `noOptedCommandBinding()` with leftover args | returns binding for default command |
| `noOptedCommandBinding()` when no default command registered | returns empty |
| `@CafeCommandExecutionCondition(NON_OPTED_COMMAND_EXISTS)` with no leftover args | binding excluded |
| `@CafeCommandExecutionCondition(NON_OPTED_COMMAND_EXISTS)` with leftover args | binding included |
| `@CafeCommandExecutionCondition(ALWAYS_RUN)` | binding always included |

Commit: `test: CafeCommandBindingService unit tests`

---

### 1.4 — CafeCommandRuntimeService

**New file:** `.../services/CafeCommandRuntimeServiceTest.java`

| Scenario | Expected |
|---|---|
| `map(List<Option>)` with two matched options | returns two runtimes |
| `map(String[])` — leftover args present | returns runtime for default command |
| `map(String[])` — no leftover args | returns empty |
| `run(CafeCommandRuntime)` — void method | executes without exception |
| `run(CafeCommandRuntime)` — method returns value | return value stored in repository |
| `run(CafeCommandRuntime)` — method takes `CafeCommandArguments` | `CafeCommandArguments.cliValues` set to runtime arguments |
| `run(List<CafeCommandRuntime>)` — ordered list | methods invoked in list order |

Commit: `test: CafeCommandRuntimeService unit tests`

---

### 1.5 — Integration test: full CLI invocation

**New file:** `.../CafeShellIntegrationTest.java`

End-to-end tests using a real `CafeShell` instantiated with a minimal `@CafeApplication` config.

| Scenario | Args | Expected exit code | Side effect |
|---|---|---|---|
| Valid single command | `["--greet"]` | `0` | command method invoked |
| Unknown option | `["--unknown"]` | `2` | help printed |
| No args, no default command | `[]` | `2` | help printed |
| No args, default command present | `[]` | `0` | default command invoked |
| Dependent command ordering | `["--b", "--a"]` where A depends on B | `0` | B invoked before A |
| Command method throws exception | `["--fail"]` | `1` | exception logged |
| `--help` | `["-h"]` | `0` | help printed |

Fixture: create a `TestShellConfig` with 3–4 commands and a spy to record invocation order.

Commit: `test: CafeShell integration tests`

---

## Phase 2 — Code Review Fixes

### 2.1 — CafeCommandArguments: document singleton-per-run contract

**File:** `cafe-shell/src/main/java/.../commands/CafeCommandArguments.java`

`CafeCommandArguments` is `@CafeSingleton` and is mutated during `CafeCommandRuntimeService.run()`.
If `CafeShell.run()` is called more than once in the same JVM (e.g. in tests), the singleton
retains values from the previous run.

Options:
1. **Document the limitation** (simplest — one-shot CLI tools are the intended use case).
2. **Clear state between runs** — add a `reset()` method called at the start of `execute()`.

Recommendation: option 2 — add `reset()` and call it from `CafeShell.execute()` before
`CafeCommandRuntimeService.map()`. This makes integration tests reliable without a framework restart.

**Change:**
```java
// CafeCommandArguments
public void reset() {
    this.cliValues = new String[0];
    this.variables.clear();
}
```
```java
// CafeShell.execute() — before mapping
cafeCommandRuntimeService.reset();   // or commandArguments.reset() if directly injected
```

Commit: `fix: reset CafeCommandArguments between CafeShell.run() calls`

---

### 2.2 — CafeCommandMethodResolver: clarify extends choice

**File:** `cafe-shell/src/main/java/.../resolvers/CafeCommandMethodResolver.java`

The resolver extends `PrototypeWireMethodResolver`. `@CafeCommand` classes are resolved as
prototype instances — each command class gets a fresh instance for each execution, which is
correct. However, the intent is not obvious without a comment.

Add a single class-level note explaining that commands are prototype-scoped intentionally
(one fresh instance per command class, constructed by the prototype path in the DI container).

Commit: `docs: clarify CafeCommandMethodResolver prototype scope intent`

---

### 2.3 — PrintHelpCommand: fall back gracefully when `cafe.shell.name` is absent

**File:** `cafe-shell/src/main/java/.../commands/PrintHelpCommand.java`

`applicationName` is injected via `@CafeProperty("cafe.shell.name")`. If the property is absent,
`PropertyResolver` returns `null`, and `HelpFormatter.printHelp(null, options)` will NPE or print
a blank header.

**Change:**
```java
// replace direct field use
String headerName = applicationName != null ? applicationName : "Application";
formatter.printHelp(headerName, options);
```

Commit: `fix: PrintHelpCommand null-safe fallback for missing cafe.shell.name`

---

## Phase 3 — Roadmap (from ToDo.MD)

### 3.1 — Interactive commands

Allow a `@CafeCommand` method to prompt for input during execution via a `Scanner` or
`CafeConsole` read method. The method receives a `CafeShellReader` parameter that wraps
`System.in`.

**Proposed API:**

```java
@CafeCommand(command = "init", description = "Interactive project setup")
public class InitCommand {
    @CafeCommandRun
    public void run(CafeShellReader reader) {
        String name = reader.prompt("Project name: ");
        // ...
    }
}
```

`CafeShellReader` would be a `@CafeSingleton` wrapping `new Scanner(System.in)`.
`CafeCommandRuntimeService.run()` resolves it as a regular DI bean — no special handling needed.

Design note: `CafeShellReader` must be provided by the application; `cafe-shell` should define
the interface, the concrete `ConsoleShellReader` implementation can live in the same module.

---

### 3.2 — Command groups

Allow commands to be associated by group so that mutually related options can be declared as a
named set. Two sub-approaches from `ToDo.MD`:

**3.2a — Annotation approach:**
```java
@CafeCommand(command = "generate", group = "conversion")
@CafeCommand(command = "srcType",  group = "conversion")
@CafeCommand(command = "destType", group = "conversion")
```
When any command in a group is active, the group is considered active. Useful for
`@CafeCommandExecutionCondition(GROUP_OPTED)`.

**3.2b — Interface approach:**
```java
public interface ConversionGroup extends CafeCommandGroup { }

@CafeCommand(command = "generate")
@CafeCommandGroup(ConversionGroup.class)
public class GenerateCommand { ... }
```

Recommendation: start with the annotation approach (3.2a) — simpler to implement and covers
the use case shown in `UseCases.MD` without requiring extra interface declarations.

---

### 3.3 — Extract cafe-shell as a standalone module

Currently `cafe-shell` lives inside the `cafe-core` multi-module Maven project. Extracting it to
its own repository requires:

1. Publish `cafe-beans` to a local or remote Maven repository so `cafe-shell` can depend on it.
2. Create a new `cafe-shell` Git repository with the module's contents.
3. Update version management — `cafe-shell` would have its own `pom.xml` versioning independent
   of `cafe-core`.

This is a project-structure change, not a code change. Defer until `cafe-beans` has a stable
published artifact (i.e. first non-SNAPSHOT release).

---

### 4 Update document
Update README file
Add diagrams (class, sequence)

## Summary

| Phase | Items | Risk | Commits |
|---|---|---|---|
| 1 — Test coverage | 1.1 ClassResolver, 1.2 OrderService, 1.3 BindingService, 1.4 RuntimeService, 1.5 Integration | Low | 5 |
| 2 — Code review | 2.1 CommandArguments reset, 2.2 MethodResolver comment, 2.3 PrintHelpCommand null-safe | Low | 3 |
| 3 — Roadmap | 3.1 Interactive commands, 3.2 Command groups, 3.3 Extract module | Medium–High | TBD |

**Phase 3 items are design proposals — each needs a separate design session before implementation.**
