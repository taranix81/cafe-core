# Cafe Beans — Implementation Plan

Execution order for all 18 items in [ProposedChanges.md](ProposedChanges.md).
Four phases; each phase can be committed independently.

---

## Phase 1 — Bug Fixes

**Goal:** fix all confirmed bugs before any structural changes.
Each item is one or two lines in a single file — low risk, no design decisions.
Implement in any order within the phase; one commit per item.

| Item | File | What to change |
|---|---|---|
| **#11** | `CafeMember.java:62` | Second `addAll`: replace `CafeWirerType.class` → `CafeHandlerType.class` |
| **#10** | `SingletonHandlerMethodResolver.java:17` | `.handlerClassAnnotations(methodInfo.getClass().getAnnotations())` → `methodInfo.getMemberDeclaringClass().getAnnotations()` |
| **#1** | `CafeHandlerFindService.java:35` | Replace `return Set.of()` with return of `matchedKeys` mapped to signatures; fix raw `Set` return type |
| **#14** | `ClassBeanTypeResolver.java:19-24` | Replace `findFirst()`: if 0 → return `null`; if 1 → resolve it; if >1 → select `@CafePrimary` or throw ambiguity error |
| **#15** | `CollectionBeanTypeResolver.java:46-48` | `resolveOrNull()`: wrap `resolve()` in try-catch, return empty collection on exception |
| **#15** | `ArrayBeanTypeResolver.java:15-17` | `resolve()`: if `resolveOrNull()` returns `null`, throw `ArrayTypeResolverException` |
| **#17** | `SingletonConstructorResolver.java:15` | Add guard: `if (beansFactory.isResolved(key)) return beansFactory.getResolved(key)` |

---

## Phase 2 — Type Resolver Improvements

**Goal:** additive improvements to the type resolver layer. No breaking changes to existing behaviour.

### #13 — Guard `isAnnotationMarkedBy()` against cycles

**File:** `CafeAnnotationUtils.java:172`

- Add private overload: `isAnnotationMarkedBy(annotationType, target, Set<Class<?>> visited)`
- Guard: `if (!visited.add(annotationType)) return false`
- Replace the `"java.lang"` string check with `startsWith("java.") || startsWith("javax.")`
- Public method delegates to private overload with `new HashSet<>()`

### #18 — `Optional<T>` injection support

**New file:** `OptionalBeanTypeResolver.java`
- `isApplicable`: `typeKey.isParametrizedType() && rawType == Optional.class`
- `resolve`: `getBeanOrNull(innerTypeKey)` wrapped in `Optional.ofNullable()`
- `resolveOrNull`: delegates to `resolve()` — `Optional.empty()` is never `null`

**File:** `CafeResolvers.java:57`
- Add `new OptionalBeanTypeResolver()` to the `beanTypekeyResolvers` set

**File:** `CafeResolvableBeansValidator.java:77`
- Skip members whose field type has raw type `Optional.class` — absence is a valid resolved value

**Delete:** `cafe-beans/src/main/java/org/taranix/cafe/beans/annotations/modifiers/CafeOptional.java`
- All usages replaced with `@CafeInject Optional<T>` field declarations
- Remove after verifying no remaining references via `grep -r "CafeOptional" .`

---

## Phase 3 — Annotation Topology

**Goal:** redesign the `@CafeService` hierarchy.
Broadest scope of any phase — touches annotations, scanner, ordering, scope resolution, and all bean declarations.
**Mandatory order: 3a → 3b → 3c. Verify the application runs after 3a before starting 3c.**

### Step 3a — Prep code sites (#12)

Four sites that currently use direct `@CafeService` checks; must be updated before the topology changes or the container breaks silently.

| File | Change |
|---|---|
| `CafeAnnotationUtils.java:57-68` | `getScope()`: replace `clazz.getAnnotation(CafeService.class).scope()` with annotation-presence check via `isAnnotationMarkedBy()` — if `@CafePrototype` in chain → `Scope.Prototype`; else `Scope.Singleton` |
| `CafeAnnotationUtils.java:77-88` | `getScope(Member)`: same pattern for the member overload |
| `DefaultClassResolver.java:15` | `supports()`: replace `Set.of(CafeService.class, CafeApplication.class).contains(annotation)` with `isAnnotationMarkedBy(annotation, CafeService.class) \|\| annotation.equals(CafeApplication.class)` |
| `CafeOrderedBeansService.java:87` | `offsetDepth()`: replace `getRootClassAnnotation(CafeService.class) != null` with `isAnnotationMarkedBy(cafeClass.getRootClass(), CafeService.class)` |
| `ClassScanner.java` | No change needed — keep `@CafeService` annotated with `@CafeWirerType`; scanner already picks up all beans |

Commit as: `refactor: prep annotation-topology code sites (no behaviour change)`

### Step 3b — Meta-annotation rename (#9)

Can be committed alongside 3a or separately.

| File | Change |
|---|---|
| `CafeWirerType.java` | Rename file and class → `CafeWiringType` |
| `CafePropertyType.java` | Delete |
| `CafeProperty.java` | Replace `@CafePropertyType` → `@CafeWiringType` |
| All annotations currently using `@CafeWirerType` | Replace with `@CafeWiringType` |

Commit as: `refactor: rename @CafeWirerType → @CafeWiringType, remove @CafePropertyType`

### Step 3c — Topology redesign (#8)

Only after 3a is verified working.

| File | Change |
|---|---|
| `CafeService.java` | Remove `scope()` attribute; change `@Target` to `ANNOTATION_TYPE` only; keep `@CafeWirerType` (now `@CafeWiringType`) |
| `CafePrototype.java` | Remove `@CafeModifier`; add `@CafeService` |
| New: `CafeSingleton.java` | `@CafeService @Target({TYPE, ANNOTATION_TYPE}) public @interface CafeSingleton {}` |
| `CafeAnnotationUtils.java` `isSingleton(Member)` | Mirror the class-variant fix from 3a |
| All beans in `cafe-beans` | `@CafeService` → `@CafeSingleton`; `@CafeService(scope=Prototype)` → `@CafePrototype` |
| `Scope.java` | Keep as-is — internal use only, not public API |

Migration reference:

| Before | After |
|---|---|
| `@CafeService` | `@CafeSingleton` |
| `@CafeService(scope = Scope.Prototype)` | `@CafePrototype` |

Commit as: `feat: annotation topology — @CafeSingleton / @CafePrototype replace @CafeService(scope)`

---

## Phase 4 — Event Infrastructure

**Goal:** rename, extend, and expose the event dispatch layer.
**Sequential** — each step depends on the previous one.

### Step 4.1 — Rename `CafeHandlerExecutorService` → `HandlerMethodInvoker` (#3)

| File | Change |
|---|---|
| `CafeHandlerExecutorService.java` | Rename class → `HandlerMethodInvoker` |
| `CafeApplicationContext.java` | Update field type, getter name: `getDispatcherService()` → `getHandlerMethodInvoker()` |
| `CafeApplication.java` | Update all references |
| `BeansContextBuilder.java` | Update local variable name |

### Step 4.2 — Parameterise `SingletonHandlerMethodResolver` (#7)

| File | Change |
|---|---|
| `SingletonHandlerMethodResolver.java` | Add `Class<? extends Annotation> annotationType` constructor parameter; replace hard-coded `CafeHandler.class` |
| `CafeResolvers.java:54` | Update construction: `new SingletonHandlerMethodResolver(CafeHandler.class)` |

After this step, `cafe-desktop` can register its own `@CafeEventHandler` resolver via:
```java
.withMethodResolver(Set.of(new SingletonHandlerMethodResolver(CafeEventHandler.class)))
```

### Step 4.3 — Add `dispatchAll()` and `dispatchTo()` (#2)

**File:** `HandlerMethodInvoker.java`

```java
public void dispatchAll(Class<? extends Annotation> annotationType, Object... args) {
    // iterate all candidates, invoke each via invokeHandler(key, null, args)
}

public void dispatchTo(Class<? extends Annotation> annotationType, Object target, Object... args) {
    // iterate all candidates, invoke each via invokeHandler(key, target, args)
}
```

### Step 4.4 — Expose `HandlerMethodInvoker` as a bean (#4)

**File:** `CafeApplication.java` — in `beforeContextInit()`:
```java
addBeanToContext(cafeApplicationContext.getHandlerMethodInvoker());
```

This makes `HandlerMethodInvoker` injectable via `@CafeInject` in any bean that needs it
(including `DefaultEventDispatcher<A>` created in the next step).

### Step 4.5 — `EventDispatcher<A>` + `DefaultEventDispatcher<A>` (#5)

**New file:** `EventDispatcher.java` (interface)
```java
public interface EventDispatcher<A extends Annotation> {
    void register(Object listener);
    void unregister(Object listener);
    void send(Object... args);
    void sendTo(Object target, Object... args);
}
```

**New file:** `DefaultEventDispatcher.java`
```java
public class DefaultEventDispatcher<A extends Annotation> implements EventDispatcher<A> {
    private final Class<A> annotationType;
    private final HandlerMethodInvoker invoker;
    // send  → invoker.dispatchAll(annotationType, args)
    // sendTo → invoker.dispatchTo(annotationType, target, args)
    // register/unregister → manage a Set<Object> of live listeners for prototype dispatch
}
```

### Step 4.6 — `EventHub` (#6)

**New file:** `EventHub.java`
```java
public class EventHub {
    private final Map<Class<? extends Annotation>, EventDispatcher<?>> dispatchers = new HashMap<>();

    public <A extends Annotation> void send(Class<A> annotationType, Object... args);
    public <A extends Annotation> void sendTo(Class<A> annotationType, Object target, Object... args);
    public <A extends Annotation> EventDispatcher<A> dispatcher(Class<A> annotationType);
    public <A extends Annotation> void register(Class<A> annotationType, EventDispatcher<A> dispatcher);
}
```

**File:** `CafeApplication.java` — extend `beforeContextInit()`:
```java
EventHub eventHub = new EventHub();
eventHub.register(CafeHandler.class,
    new DefaultEventDispatcher<>(CafeHandler.class, getHandlerMethodInvoker()));
addBeanToContext(eventHub);
```

---

### Step 5
Update and add unit tests to cover other scenarion

### Step 6
Clean up and update documentation

## Summary

| Phase | Items | Risk | Notes |
|---|---|---|---|
| 1 — Bug fixes | 11, 10, 1, 14, 15, 17 | Low | 1 commit per item; any order |
| 2 — Type resolvers | 13, 18 | Low | Additive only |
| 3 — Topology | 12 → 9 → 8 | Medium | Verify after 3a before continuing |
| 4 — Events | 3 → 7 → 2 → 4 → 5 → 6 | Medium | Sequential — each step enables the next |

**Total: 18 items across ~15 commits.**
