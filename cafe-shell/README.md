## Advanced Command Execution and Dependency Injection

### Command Method Parameters

Methods annotated with `@CafeCommandRun` can declare parameters in their signature. These parameters are automatically resolved and injected at runtime using the following rules:

- **CafeCommandArguments**  
  If a parameter is of type `CafeCommandArguments` (a built-in class), it receives the parsed CLI arguments, mapped from Apache Commons CLI options.

- **Beans & Services**  
  Any other parameter is injected as a bean or service, produced by factory configuration classes or registered in the DI context.

- **Produced Objects from Previous Commands**  
  If a previous command produced an object, it is stored in the runtime repository and can be injected into subsequent commands by declaring a parameter of the matching type.

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
- If multiple objects of the same type are available (e.g., one produced by a command, another by a factory), you can control which to inject using the `@CafePrimary` annotation.
    - Annotate the command method with `@CafePrimary` to give its produced object priority over factory beans when resolving parameters of that type for later commands.

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

This flexible parameter resolution allows for rich, composable CLI flows, where commands can collaborate, share context, and use the same dependency injection model as the rest of the Cafe Beans ecosystem.