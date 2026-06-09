# Cafe Beans — Event Infrastructure

Part of the cafe-beans DI and event dispatch layer.  
See also: [cafe-desktop/events/UIFrameworkEvents.md](../../cafe-desktop/events/UIFrameworkEvents.md) — full dispatch design including desktop-side routing.

---

## Current state

### Components

| Class | Role |
|---|---|
| `CafeHandlerExecutorService` | Dispatches to `@CafeHandler` methods via `Repository` lookup |
| `CafeHandlerFindService` | Queries the handler repository by predicate — **broken** (see below) |
| `CafeHandlerSignature` | Value object: holds a handler `CafeMethod` + optional instance |
| `CafeHandlerSelector` | `@CafeService` — matches a `HandlerTypeKey` against annotation type + parameter types |
| `HandlerTypekeySelector` | Interface implemented by `CafeHandlerSelector` |
| `SingletonHandlerMethodResolver` | Registers `@CafeHandler` methods of singleton beans into the `Repository` at startup |

### `CafeHandlerExecutorService`

Dispatches events by looking up matching `HandlerTypeKey` entries from the `Repository`.

```java
public class CafeHandlerExecutorService {
    // single dispatch — first match only
    public Object dispatch(Class<? extends Annotation> methodAnnotationType, Object... parameters);

    // private — invokes a single matched handler; supports optional targetInstance
    private Object invokeHandler(HandlerTypeKey key, Object targetInstance, Object... parameters);
}
```

`dispatch()` returns after the **first match** — fan-out to all handlers is not implemented.
`invokeHandler()` already accepts a `targetInstance` parameter but `dispatch()` always passes `null` —
targeted dispatch is structurally present but not wired.

`CafeApplicationContext` exposes this as `getDispatcherService()`.

### `CafeHandlerFindService` — known bug

`find()` correctly filters `HandlerTypeKey` entries by predicate but **ignores the result**:

```java
public Set find(...) {
    Collection<HandlerTypeKey> matchedKeys = repository.getKeys(HandlerTypeKey.class)
        .filter(...)
        .collect(Collectors.toSet());

    return Set.of();   // ← BUG: matchedKeys computed but never returned or used
}
```

No handler resolution works through `CafeHandlerFindService` until this is fixed.

### `CafeHandlerSelector`

`@CafeService` — auto-registered singleton. Matches a `HandlerTypeKey` against a call:

```java
boolean isMatch(HandlerTypeKey key, Class<? extends Annotation> methodAnnotation, Object... parameters) {
    // 1. any handler annotation matches methodAnnotation
    // 2. parameter types are compatible (count + type-compatible check)
}
```

Registered in the `Repository` as a `HandlerTypekeySelector` bean.
`CafeHandlerExecutorService` looks it up at dispatch time via `getSelectors()`.

### `SingletonHandlerMethodResolver`

Runs during singleton bean resolution. For each `@CafeHandler` method on a singleton:
1. Builds a `HandlerTypeKey` (annotation type + parameter types + return type)
2. Builds a `CafeHandlerSignature` (method + instance)
3. Persists both into `CafeBeansFactory`

Currently hard-coded to `CafeHandler.class` — one resolver instance, one annotation type.

### `HandlerTypeKey`

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

## Proposed additions

These are the additions needed to support `EventDispatcher<A>` and `EventHub` from the desktop design.
See [proposal-changes/ProposedChanges.md](../proposal-changes/ProposedChanges.md) for the full change list.

### `dispatchAll()` and `dispatchTo()` on `CafeHandlerExecutorService`

```java
// fan-out — all matching handlers, not just first
void dispatchAll(Class<? extends Annotation> annotationType, Object... args);

// targeted — handlers on target instance only (identity check)
void dispatchTo(Class<? extends Annotation> annotationType, Object target, Object... args);
```

`dispatchTo` wires through the existing `invokeHandler(key, targetInstance, params)` signature
which already handles target instance filtering.

### Rename `CafeHandlerExecutorService` → `HandlerMethodInvoker`

Reflects the actual role — reflection-based method invocation, not a "service".

### `EventDispatcher<A extends Annotation>` interface

```java
public interface EventDispatcher<A extends Annotation> {
    void register(Object listener);
    void unregister(Object listener);
    void send(Object... args);
    void sendTo(Object target, Object... args);
}
```

### `DefaultEventDispatcher<A>` implementation

```java
public class DefaultEventDispatcher<A extends Annotation> implements EventDispatcher<A> {
    public DefaultEventDispatcher(Class<A> annotationType, HandlerMethodInvoker invoker) { ... }
    // send  → invoker.dispatchAll(annotationType, args)
    // sendTo → invoker.dispatchTo(annotationType, target, args)
}
```

### `EventHub` — global facade

```java
public class EventHub {
    public <A extends Annotation> void send(Class<A> annotationType, Object... args);
    public <A extends Annotation> void sendTo(Class<A> annotationType, Object target, Object... args);
    public <A extends Annotation> EventDispatcher<A> dispatcher(Class<A> annotationType);
    public <A extends Annotation> void register(Class<A> annotationType, EventDispatcher<A> dispatcher);
}
```

Registered at bootstrap as a singleton bean via `addBeanToContext(eventHub)`.

### `SingletonHandlerMethodResolver` — multi-annotation support

Currently hard-coded to `CafeHandler.class`. Needs to become generic — one resolver instance
per annotation type — so `cafe-desktop`'s `@CafeEventHandler` can register its own resolver
without changing `cafe-beans`.

```java
// parameterised resolver
public class SingletonHandlerMethodResolver implements CafeMethodResolver {
    private final Class<? extends Annotation> annotationType;
    public SingletonHandlerMethodResolver(Class<? extends Annotation> annotationType) { ... }

    @Override
    public boolean supports(Class<? extends Annotation> annotation) {
        return annotation.equals(annotationType);
    }
}
```

---

## Routing dimensions (unchanged)

| Dimension | Resolved by |
|---|---|
| **Annotation type** | `EventHub` selects dispatcher, or caller injects dispatcher directly |
| **Arguments** | `CafeHandlerSelector` matches handler parameter types |
| **Target** | `send` = broadcast; `sendTo` = restricted to one instance |
