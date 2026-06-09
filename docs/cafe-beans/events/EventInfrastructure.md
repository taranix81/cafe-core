# Cafe Beans — Event Infrastructure

Part of the cafe-beans DI and event dispatch layer.  
See also: [cafe-desktop/events/UIFrameworkEvents.md](../../cafe-desktop/events/UIFrameworkEvents.md) — full dispatch design including desktop-side routing.

---

## Components

| Class | Role |
|---|---|
| `HandlerMethodInvoker` | Dispatches to `@CafeHandler` methods via `Repository` lookup |
| `CafeHandlerFindService` | Queries the handler repository by predicate |
| `CafeHandlerSignature` | Value object: holds a handler `CafeMethod` + optional instance |
| `CafeHandlerSelector` | `@CafeService` — matches a `HandlerTypeKey` against annotation type + parameter types |
| `HandlerTypekeySelector` | Interface implemented by `CafeHandlerSelector` |
| `SingletonHandlerMethodResolver` | Registers handler methods of singleton beans into the `Repository` at startup — parameterised by annotation type |
| `EventDispatcher<A>` | Interface: `register`, `unregister`, `send`, `sendTo` |
| `DefaultEventDispatcher<A>` | Default implementation — delegates to `HandlerMethodInvoker` |
| `EventHub` | Registry of `EventDispatcher` instances keyed by annotation type |

---

## `HandlerMethodInvoker`

Dispatches events by looking up matching `HandlerTypeKey` entries from the `Repository`.

```java
public class HandlerMethodInvoker {
    // single dispatch — first match only, returns result
    public Object dispatch(Class<? extends Annotation> annotationType, Object... parameters);

    // fan-out — all matching handlers
    public void dispatchAll(Class<? extends Annotation> annotationType, Object... args);

    // targeted — handlers whose declaring instance matches target (identity check)
    public void dispatchTo(Class<? extends Annotation> annotationType, Object target, Object... args);
}
```

Registered as a bean via `CafeApplication.beforeContextInit()` so it is injectable into any bean.
`CafeApplicationContext` exposes it as `getHandlerMethodInvoker()`.

---

## `SingletonHandlerMethodResolver`

Runs during singleton bean resolution. For each handler method on a singleton:
1. Builds a `HandlerTypeKey` (annotation type + parameter types + return type)
2. Builds a `CafeHandlerSignature` (method + instance)
3. Persists both into `CafeBeansFactory`

Parameterised by `Class<? extends Annotation> annotationType` — one resolver instance per
annotation type. `CafeResolvers` registers it with `CafeHandler.class`; downstream modules
(e.g. `cafe-desktop`) can register their own resolver for their own annotation without changing
`cafe-beans`.

---

## `EventDispatcher<A>` / `DefaultEventDispatcher<A>`

```java
public interface EventDispatcher<A extends Annotation> {
    void register(Object listener);
    void unregister(Object listener);
    void send(Object... args);
    void sendTo(Object target, Object... args);
}
```

`DefaultEventDispatcher<A>` implements `EventDispatcher<A>`:
- `send`   → `invoker.dispatchAll(annotationType, args)`
- `sendTo` → `invoker.dispatchTo(annotationType, target, args)`
- `register` / `unregister` maintain a `ConcurrentHashMap`-backed listener set

---

## `EventHub`

Registry keyed by annotation type. Registered as a singleton bean in `beforeContextInit()`.

```java
public class EventHub {
    public <A extends Annotation> void register(Class<A> annotationType, EventDispatcher<A> dispatcher);
    public <A extends Annotation> EventDispatcher<A> dispatcher(Class<A> annotationType);
    public <A extends Annotation> void send(Class<A> annotationType, Object... args);
    public <A extends Annotation> void sendTo(Class<A> annotationType, Object target, Object... args);
}
```

At bootstrap, `CafeApplication.beforeContextInit()` registers a `DefaultEventDispatcher<CafeHandler>`
for `CafeHandler.class`:

```java
EventHub eventHub = new EventHub();
eventHub.register(CafeHandler.class,
        new DefaultEventDispatcher<>(CafeHandler.class, getHandlerMethodInvoker()));
addBeanToContext(eventHub);
```

`send` / `sendTo` are no-ops when no dispatcher is registered for the given annotation type.

---

## `HandlerTypeKey`

Repository key for handler entries:

```java
@Builder
class HandlerTypeKey extends AbstractTypeKey {
    Annotation[]  handlerAnnotations;        // method-level annotations
    Annotation[]  handlerClassAnnotations;   // class-level annotations
    BeanTypeKey   handlerReturnTypeKey;
    BeanTypeKey[] handlerParameters;
}
```

---

## Routing dimensions

| Dimension | Resolved by |
|---|---|
| **Annotation type** | `EventHub` selects dispatcher, or caller injects dispatcher directly |
| **Arguments** | `CafeHandlerSelector` matches handler parameter types |
| **Target** | `send` = broadcast; `sendTo` = restricted to one instance |
