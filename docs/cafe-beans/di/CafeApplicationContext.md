# Cafe Beans — CafeApplicationContext

`CafeApplicationContext` **is** the DI runtime. It holds the bean factory and event dispatcher,
and exposes the public API for resolving beans, dispatching events, and reading properties.

See also: [CafeBeansFactory.md](CafeBeansFactory.md) — resolver chain internals.  
See also: [CafeResolvers.md](CafeResolvers.md) — resolver types and registration.  
See also: [../events/EventInfrastructure.md](../events/EventInfrastructure.md) — event dispatch.

---

## Structure

```java
public class CafeApplicationContext {
    private final CafeBeansFactory           beansFactory;        // DI core
    private final CafeHandlerExecutorService dispatcherService;   // event dispatch
    // private constructor — only created via BeansContextBuilder.build()
}
```

There is no public constructor. The only entry point is `BeansContextBuilder.build()`.

---

## Public API

| Method | Delegates to | Purpose |
|---|---|---|
| `getInstance(Class<T>)` | `beansFactory.getBean(BeanTypeKey)` | Returns the single registered instance (singleton or new prototype) |
| `getInstance(Class<T>, String)` | `beansFactory.getBean(BeanTypeKey)` | Named variant |
| `getInstances(Class<T>)` | `beansFactory.getBeanOrNull(BeanTypeKey)` | Returns all registered instances of a type |
| `getInstances(Class<T>, String)` | same | Named variant |
| `getProperty(String)` | `beansFactory.getProperty(name)` | Reads a value from `application.properties` |
| `executeHandler(Class<Annotation>, Object...)` | `dispatcherService.dispatch(...)` | Dispatches an event to the first matching `@CafeHandler` method |
| `initialize()` | `beansFactory.resolveAllBeans()` | Triggers full DI resolution — called once by `CafeApplication` |
| `refresh(Object)` | `resolvers.findFieldResolver(...).resolve(...)` | Re-injects DI fields into an existing singleton object |

`refresh()` is used in `CafeApplication.postContextInit()` to wire the application instance itself
after the container is fully initialised.

---

## Assembly — `BeansContextBuilder.build()`

`BeansContextBuilder` is the DI pipeline factory. `build()` assembles every component:

```
1. ClassScanner.scan(packages) + explicit classes
        → allClasses : Set<Class<?>>

2. CafeMetadataRegistry.builder().withClasses(allClasses).build()
        → metadataRegistry : CafeMetadataRegistry   (annotation metadata per class)

3. new CafeValidationService(Set.of(
        new CafeCycleDetectionValidator(),
        new CafeResolvableBeansValidator(),
        new CafeHandlerMethodsParameterValidator()))
        → validationService

4. new BeansRepository()                             (default — can be overridden via builder)
        → repository

5. new CafeResolvers()  + add all registered resolvers
        → cafeResolvers

6. new CafeBeansFactory(repository, validationService, metadataRegistry, cafeResolvers)
        → beansFactory

7. new CafeHandlerExecutorService(repository)
        → dispatcherService

8. new CafeApplicationContext(beansFactory, dispatcherService, classLoader)
   └── CafePropertiesService.load(repository, classLoader)   ← loads application.properties
```

Everything shares a single `BeansRepository` instance — beans, handler keys, and selectors
are all stored in the same repository and looked up by type key.

### Builder methods

| Method | Effect |
|---|---|
| `withPackageScan(String...)` | Packages to scan for annotated classes |
| `withClass(Class<?>)` / `withClasses(Set<Class<?>>)` | Explicit classes to include (bypasses scan) |
| `withClassResolver(Set<CafeClassResolver>)` | Extra class-level resolvers |
| `withMethodResolver(Set<CafeMethodResolver>)` | Extra method-level resolvers |
| `repository(BeansRepository)` | Override the default `BeansRepository` |
| `withClassLoader(ClassLoader)` | Defaults to `Thread.currentThread().getContextClassLoader()` |

---

## `CafeApplication` lifecycle

`CafeApplication` is the abstract base for all user applications. It drives the context through
a fixed lifecycle:

```
CafeApplication(class, packages)
  1. validateApplicationClass()          checks @CafeApplication annotation is present
  2. createApplicationContext()          BeansContextBuilder.builder()
                                           .withPackageScan(packages)
                                           .build()
  3. beforeContextInit()                 hook — override to add beans before DI resolves
  4. cafeApplicationContext.initialize() beansFactory.resolveAllBeans()
  5. postContextInit()                   cafeApplicationContext.refresh(this)
                                         ← DI-injects fields on the CafeApplication instance
```

`beforeContextInit()` is the canonical place to manually seed the container — `EventHub`,
framework services, and external objects that DI cannot construct.

`postContextInit()` wires the `CafeApplication` subclass itself so its `@CafeInject` fields
are populated by the time user code runs.

---

## Manual bean registration — `addBeanToContext()`

DI normally discovers and constructs beans during `initialize()`. For objects that exist before
DI resolves (framework singletons, the application itself), `addBeanToContext` inserts them
directly into the repository:

```java
protected void addBeanToContext(Object object) {
    // creates a BeanRepositoryEntry and puts it into BeansRepository directly
    // bypasses resolvers — the object is used as-is
}
```

Call from `beforeContextInit()` so the object is available when `resolveAllBeans()` runs
and injects it into other beans.

### `EventHub` bootstrap (proposed — see [ProposedChanges.md](../proposal-changes/ProposedChanges.md#6))

```java
@Override
protected void beforeContextInit() {
    addBeanToContext(new EventHub());
}
```

This makes `EventHub` injectable as a singleton into any bean that declares `@CafeInject EventHub`.

---

## `dispatcherService` exposure gap

`CafeApplicationContext.getDispatcherService()` returns `CafeHandlerExecutorService`.
`CafeApplication` does not expose it directly — only `getBeansFactory()` is public.

Proposed fix ([ProposedChanges.md #4](../proposal-changes/ProposedChanges.md#4)):
register the invoker as a bean via `addBeanToContext(dispatcherService)` in `beforeContextInit()`
so `DefaultEventDispatcher<A>` can have it injected rather than looked up manually.

---

## What is decided

| Topic | Decision |
|---|---|
| Construction | Private constructor — `BeansContextBuilder.build()` is the only entry point |
| Shared repository | All components (`beansFactory`, `dispatcherService`) share one `BeansRepository` |
| Lifecycle hook | `beforeContextInit()` — seed manual beans before `resolveAllBeans()` |
| `refresh()` | Used by `postContextInit()` to wire the `CafeApplication` subclass itself |
| `addBeanToContext()` | Bypasses resolvers — direct repository insert; must be called in `beforeContextInit()` |

## What is open

| Topic | Status |
|---|---|
| `HandlerMethodInvoker` exposure | `getDispatcherService()` exists but not registered as bean — see [ProposedChanges.md #4](../proposal-changes/ProposedChanges.md#4) |
| `EventHub` bootstrap | Design documented — not yet implemented; see [ProposedChanges.md #6](../proposal-changes/ProposedChanges.md#6) |
| `CafeApplication.getDispatcherService()` | Not exposed — access is via `CafeApplicationContext.getDispatcherService()` only |
