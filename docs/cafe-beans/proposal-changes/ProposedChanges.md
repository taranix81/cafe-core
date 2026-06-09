# Cafe Beans — Proposed Changes

Changes identified during cafe-desktop design exploration and source analysis.  
Each item notes what already exists vs what is genuinely new work.

---

## Summary

| # | Change | Area | Already exists? | Effort |
|---|---|---|---|---|
| 1 | Fix `CafeHandlerFindService.find()` returning `Set.of()` | Events | Bug confirmed | Small |
| 2 | Add `dispatchAll()` + `dispatchTo()` to `CafeHandlerExecutorService` | Events | Partial (`invokeHandler` supports target) | Small |
| 3 | Rename `CafeHandlerExecutorService` → `HandlerMethodInvoker` | Events | No | Small |
| 4 | Expose `HandlerMethodInvoker` as a bean via `CafeApplication` | Events | `getDispatcherService()` exists, needs rename | Small |
| 5 | Add `EventDispatcher<A>` interface + `DefaultEventDispatcher<A>` | Events | No | Medium |
| 6 | Add `EventHub` concrete class | Events | No | Medium |
| 7 | Make `SingletonHandlerMethodResolver` generic (parameterised annotation type) | Events | Hard-coded to `@CafeHandler` | Small |
| 8 | Annotation topology redesign — remove `scope` attribute, introduce `@CafeSingleton` / `@CafePrototype` | DI | `@CafePrototype` exists as modifier; prototype resolvers exist | Medium |
| 9 | Meta-annotation cleanup — rename `@CafeWirerType` → `@CafeWiringType`; remove `@CafePropertyType`, move `@CafeProperty` under `@CafeWiringType` | DI | Both exist, no scanner logic change needed | Small |
| 10 | Fix `HandlerTypeKey.handlerClassAnnotations` — wrong source in `SingletonHandlerMethodResolver` | Events | Bug confirmed | Small |
| 11 | Fix `getAnnotationLifecycleMarkers()` copy-paste — `CafeHandlerType` never added | Metadata | Bug confirmed | Trivial |
| 12 | Fix 4 code sites that will break when annotation topology changes (#8) | DI | Latent — triggered by change #8 | Small |
| 13 | Guard `isAnnotationMarkedBy()` against annotation-hierarchy cycles | Reflection | No | Small |
| 14 | `ClassBeanTypeResolver` — non-deterministic `findFirst()` with multiple providers | Type resolvers | Bug confirmed | Small |
| 15 | `resolve()` / `resolveOrNull()` contract inconsistency across type resolvers | Type resolvers | Bug confirmed | Small |
| 16 | Document collection-injection prototype semantics — always creates fresh instances | Type resolvers | Undocumented design | Trivial |
| 17 | `SingletonConstructorResolver` — re-instantiates already-resolved singletons on collection path | Type resolvers | Optimization | Small |
| 18 | Add `Optional<T>` injection support — `OptionalBeanTypeResolver` + validator change | Type resolvers | No | Small |

---

## 1 — Fix `CafeHandlerFindService.find()`

**File:** `CafeHandlerFindService.java`

`find()` correctly builds `matchedKeys` but ignores the result:

```java
// current — broken
Collection<HandlerTypeKey> matchedKeys = repository.getKeys(HandlerTypeKey.class)
    .filter(...)
    .collect(Collectors.toSet());
return Set.of();   // ← bug: matchedKeys never used

// fix — map matchedKeys to handler signatures and return them
```

This is a prerequisite for all event dispatch to work correctly end-to-end.

---

## 2 — Add `dispatchAll()` and `dispatchTo()` to `CafeHandlerExecutorService`

**File:** `CafeHandlerExecutorService.java` (rename pending #3)

`dispatch()` stops after the first match.
`invokeHandler(key, targetInstance, params)` already supports a target instance but `dispatch()`
always passes `null`. The two new methods wire these capabilities:

```java
// fan-out — all matching handlers
public void dispatchAll(Class<? extends Annotation> annotationType, Object... args);

// targeted — handlers on target instance only
public void dispatchTo(Class<? extends Annotation> annotationType, Object target, Object... args);
```

`dispatchTo` calls `invokeHandler(key, target, args)` — minimal change.

---

## 3 — Rename `CafeHandlerExecutorService` → `HandlerMethodInvoker`

**Files:** `CafeHandlerExecutorService.java` + all usages in `CafeApplicationContext`, `CafeApplication`

Reflects the actual role — reflection-based method invocation, not a "service".

---

## 4 — Expose `HandlerMethodInvoker` as a bean

**File:** `CafeApplication.java`, `CafeApplicationContext.java`

`CafeApplicationContext` already exposes `getDispatcherService()` returning `CafeHandlerExecutorService`.
After rename (#3), update to `getHandlerMethodInvoker()` and register in context via
`addBeanToContext(invoker)` so `DefaultEventDispatcher<A>` can have it injected.

---

## 5 — Add `EventDispatcher<A>` + `DefaultEventDispatcher<A>`

**Detail:** [events/EventInfrastructure.md](../events/EventInfrastructure.md)

```java
public interface EventDispatcher<A extends Annotation> {
    void register(Object listener);
    void unregister(Object listener);
    void send(Object... args);
    void sendTo(Object target, Object... args);
}

public class DefaultEventDispatcher<A extends Annotation> implements EventDispatcher<A> {
    public DefaultEventDispatcher(Class<A> annotationType, HandlerMethodInvoker invoker) { ... }
}
```

**Files to create:** `EventDispatcher.java`, `DefaultEventDispatcher.java`

---

## 6 — Add `EventHub`

**Detail:** [events/EventInfrastructure.md](../events/EventInfrastructure.md)

```java
public class EventHub {
    public <A extends Annotation> void send(Class<A> annotationType, Object... args);
    public <A extends Annotation> void sendTo(Class<A> annotationType, Object target, Object... args);
    public <A extends Annotation> EventDispatcher<A> dispatcher(Class<A> annotationType);
    public <A extends Annotation> void register(Class<A> annotationType, EventDispatcher<A> dispatcher);
}
```

Registered at bootstrap: `addBeanToContext(eventHub)` in `CafeApplication.beforeContextInit()`.

**Files to create:** `EventHub.java`

---

## 7 — Make `SingletonHandlerMethodResolver` generic

**File:** `SingletonHandlerMethodResolver.java`

Currently hard-coded to `CafeHandler.class`. Needs to be parameterised so `cafe-desktop` can
register its own `@CafeEventHandler` resolver without modifying `cafe-beans`:

```java
public class SingletonHandlerMethodResolver implements CafeMethodResolver {
    private final Class<? extends Annotation> annotationType;   // injected, not hard-coded

    @Override
    public boolean supports(Class<? extends Annotation> annotation) {
        return annotation.equals(annotationType);
    }
}
```

---

## 8 — Annotation topology redesign

**Detail:** [di/BeanScopeDesign.md](../di/BeanScopeDesign.md)

Remove the `scope` attribute from `@CafeService`. Scope is expressed by which annotation is placed
on the class. `@CafeService` becomes a pure meta-annotation (scanner anchor only).

### New topology

```
@CafeService   (@Target(ANNOTATION_TYPE) only — no attributes)
  ├── @CafeSingleton   (@Target(TYPE, ANNOTATION_TYPE))
  └── @CafePrototype   (@Target(TYPE, ANNOTATION_TYPE))
        └── @CafeComponent  (cafe-desktop)
```

### Annotation changes

```java
// @CafeService — remove scope(), change @Target to ANNOTATION_TYPE only
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
public @interface CafeService { }

// @CafeSingleton — new
@CafeService
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
public @interface CafeSingleton { }

// @CafePrototype — move from @CafeModifier to @CafeService family
@CafeService
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
public @interface CafePrototype { }
```

### Scanner change

Two-hop traversal — supports `@CafeComponent → @CafePrototype → @CafeService`:

```java
boolean isBean(Class<?> cls) {
    return hasAnnotationInChain(cls.getAnnotations(), CafeService.class, 2);
}

Scope scopeOf(Class<?> cls) {
    return hasAnnotationInChain(cls.getAnnotations(), CafePrototype.class, 2)
        ? Scope.Prototype : Scope.Singleton;
}
```

### Migration

| Current | After |
|---|---|
| `@CafeService` | `@CafeSingleton` |
| `@CafeService(scope = Scope.Prototype)` | `@CafePrototype` |

`Scope` enum stays as internal scanner detail — no longer public API.  
`PrototypeConstructorResolver` and `PrototypeWireMethodResolver` continue to work unchanged.

**Files to change:**
- `CafeService.java` — remove `scope()`, change `@Target`
- `CafePrototype.java` — remove `@CafeModifier`, add `@CafeService`
- New: `CafeSingleton.java`
- `CafeApplicationContext.java` — scanner two-hop traversal
- All beans in `cafe-beans` — replace `@CafeService` with `@CafeSingleton`

---

## 9 — Meta-annotation cleanup

### Rename `@CafeWirerType` → `@CafeWiringType`

**File:** `CafeWirerType.java` → `CafeWiringType.java` + all `@CafeWirerType` usages

"Wirer" is not natural English. "Wiring" is the noun form that reads correctly.

```java
// before
@CafeWirerType
public @interface CafeInject { }

// after
@CafeWiringType
public @interface CafeInject { }
```

### Remove `@CafePropertyType` — `@CafeProperty` moves under `@CafeWiringType`

`@CafePropertyType` is a single-use meta-annotation — only `@CafeProperty` uses it.
It adds a layer with no value. `@CafeProperty` is a wiring annotation like `@CafeInject`;
it belongs under `@CafeWiringType` directly.

```java
// before
@CafePropertyType
public @interface CafeProperty { ... }

// after
@CafeWiringType
public @interface CafeProperty { ... }
```

**Files to change:**
- `CafeWirerType.java` → rename to `CafeWiringType.java`
- `CafePropertyType.java` → delete
- `CafeProperty.java` → replace `@CafePropertyType` with `@CafeWiringType`
- All annotations currently using `@CafeWirerType` → update to `@CafeWiringType`

### Resulting meta-annotation layer

```
@CafeWiringType  (renamed, absorbs @CafePropertyType)
  ├── @CafeApplication
  ├── @CafeInject
  ├── @CafeProperty          ← moved from @CafePropertyType
  ├── @CafePostInit
  └── @CafeProvider

@CafeHandlerType             (unchanged)
  └── @CafeHandler

@CafeService                 (independent meta — per change #8)
  ├── @CafeSingleton
  └── @CafePrototype

@CafeModifier                (unchanged, @CafePrototype removed from it per change #8)
  ├── @CafePrimary
  ├── @CafeName
  └── @CafeOptional
```

---

---

## 10 — Fix `HandlerTypeKey.handlerClassAnnotations` wrong source

**File:** `SingletonHandlerMethodResolver.java:17`

`methodInfo.getClass()` returns the Java class of the `CafeMethod` wrapper object, not the
declaring bean class. The stored class-level annotations are the framework's own metadata class
annotations — useless for handler lookup.

```java
// current — broken: stores annotations of CafeMethod.class, not the bean class
HandlerTypeKey handlerTypeKey = HandlerTypeKey.builder()
    .handlerClassAnnotations(methodInfo.getClass().getAnnotations())   // ← BUG
    ...

// fix — annotations of the declaring bean class
HandlerTypeKey handlerTypeKey = HandlerTypeKey.builder()
    .handlerClassAnnotations(methodInfo.getMemberDeclaringClass().getAnnotations())
    ...
```

Impact: `CafeHandlerFindService.find(classAnnotation, ...)` predicates against class-level
annotations will never match, even after fixing bug #1. Any handler selector that relies on
class-level annotations (e.g. "only handle if the bean is a `@CafeService`") is silently broken.

---

## 11 — Fix `getAnnotationLifecycleMarkers()` copy-paste

**File:** `CafeMember.java:60-63`

The second `addAll` call is identical to the first — `CafeHandlerType` annotations are never
collected:

```java
// current — broken: CafeHandlerType never added
public final Set<Class<? extends Annotation>> getAnnotationLifecycleMarkers() {
    Set<Class<? extends Annotation>> result = new HashSet<>(getAnnotationTypesMarkedBy(CafeWirerType.class));
    result.addAll(getAnnotationTypesMarkedBy(CafeWirerType.class));    // ← copy-paste: same as line above
    return result;
}

// fix
public final Set<Class<? extends Annotation>> getAnnotationLifecycleMarkers() {
    Set<Class<? extends Annotation>> result = new HashSet<>(getAnnotationTypesMarkedBy(CafeWirerType.class));
    result.addAll(getAnnotationTypesMarkedBy(CafeHandlerType.class));  // ← correct
    return result;
}
```

Impact: `CafeResolvableBeansValidator` checks
`cafeMember.getAnnotationLifecycleMarkers().contains(CafeHandlerType.class)` as an early skip
for handler methods. The check always returns `false` — it is dead code. Handler methods are
only accidentally skipped by the later `hasDependencies()` check, which returns `false` because
`CafeMethod.getRequiredTypeKeys()` returns `List.of()` for non-`@CafeWirerType` methods.
Practically harmless today; becomes confusing when a handler method gains a parameter.

---

## 12 — Topology-change code blockers (prerequisite for change #8)

Change #8 removes the `scope()` attribute from `@CafeService` and replaces it with concrete
annotations (`@CafeSingleton` / `@CafePrototype`). Four sites assume a direct `@CafeService`
annotation on beans and will silently break.

### A — `CafeAnnotationUtils.getScope()` and `isSingleton()`

**File:** `CafeAnnotationUtils.java:57-68`

```java
// current — reads scope() attribute directly
public static Scope getScope(Class<?> clazz) {
    return Optional.ofNullable(clazz.getAnnotation(CafeService.class))
            .map(CafeService::scope)
            .orElse(Scope.Singleton);   // ← silently returns Singleton for @CafePrototype beans
}
```

After change #8, `@CafePrototype` beans have no `@CafeService` annotation directly. The
fallback `Scope.Singleton` makes every prototype bean look like a singleton — silent data loss.

```java
// fix — infer scope from annotation presence (see BeanScopeDesign.md scanner pseudocode)
public static Scope getScope(Class<?> clazz) {
    if (isAnnotationMarkedBy(clazz, CafePrototype.class))  return Scope.Prototype;
    if (isAnnotationMarkedBy(clazz, CafeSingleton.class))  return Scope.Singleton;
    return Scope.Singleton;   // default
}
```

### B — `DefaultClassResolver.supports()`

**File:** `DefaultClassResolver.java:15-18`

```java
// current — hardcoded set; @CafeSingleton and @CafePrototype not recognised
public boolean supports(Class<? extends Annotation> annotation) {
    return Set.of(CafeService.class, CafeApplication.class).contains(annotation);
}
```

After change #8, beans annotated with `@CafeSingleton` or `@CafePrototype` won't match
`@CafeService.class`. `CafeResolvers.findClassResolvers()` will find no resolver →
`NO_RESOLVER_FOUND` exception for every bean in the application.

```java
// fix
public boolean supports(Class<? extends Annotation> annotation) {
    return CafeAnnotationUtils.isAnnotationMarkedBy(annotation, CafeService.class)
        || annotation.equals(CafeApplication.class);
}
```

### C — `CafeOrderedBeansService.offsetDepth()`

**File:** `CafeOrderedBeansService.java:87`

```java
// current — direct annotation check; @CafeSingleton beans get non-zero offset
if (cafeClass.getRootClassAnnotation(CafeService.class) != null) {
    return 0;
}
```

`getRootClassAnnotation` is `clz.getAnnotation()` — a direct check. After topology change,
`@CafeSingleton` beans have no `@CafeService` directly → they fall through to the custom-resolver
offset calculation and get placed at wrong positions in the resolution order.

```java
// fix
if (CafeAnnotationUtils.isAnnotationMarkedBy(cafeClass.getRootClass(), CafeService.class)) {
    return 0;
}
```

(Or check for `@CafeSingleton` / `@CafePrototype` directly.)

### D — `ClassScanner.scan()`

**File:** `ClassScanner.java:26-28`

```java
// current — filters by @CafeWirerType
.filter(aClass -> CafeAnnotationUtils.hasAnnotationMarker(aClass, CafeWirerType.class))
```

Currently `@CafeService` is annotated with `@CafeWirerType`, so all beans are picked up.
After change #8 and #9, if `@CafeService` loses its `@CafeWirerType` annotation
(it becomes a standalone meta-annotation with no `@CafeWirerType` parent), beans annotated
only with `@CafeSingleton` or `@CafePrototype` will not be scanned — they vanish silently.

Two options:
1. Keep `@CafeService` under `@CafeWirerType` (simplest — no scanner change needed).
2. Extend the scanner to also accept `@CafeService`-chain classes:
```java
.filter(aClass -> CafeAnnotationUtils.hasAnnotationMarker(aClass, CafeWirerType.class)
               || CafeAnnotationUtils.hasAnnotationMarker(aClass, CafeService.class))
```

**Option 1 is recommended** — it keeps the scanner logic unchanged and `@CafeService` being
`@CafeWirerType` is conceptually correct (a bean is a wiring-type thing).

---

## 13 — Guard `isAnnotationMarkedBy()` against annotation-hierarchy cycles

**File:** `CafeAnnotationUtils.java:172-186`

The recursion guard is `!a.annotationType().getPackageName().contains("java.lang")`. This:
- Misses JDK annotations outside `java.lang` (e.g. `java.util.concurrent`, `javax.*`)
- Does not guard against custom annotation cycles

```java
// current — fragile filter, no visited guard
public static boolean isAnnotationMarkedBy(Class<? extends Annotation> annotationType,
                                           Class<? extends Annotation> otherAnnotationClass) {
    if (annotationType.isAnnotationPresent(otherAnnotationClass)) return true;
    return Arrays.stream(annotationType.getAnnotations())
            .filter(a -> !a.annotationType().getPackageName().contains("java.lang"))
            .anyMatch(a -> isAnnotationMarkedBy(a, otherAnnotationClass));
}
```

```java
// fix — visited set prevents cycles; standard package prefix covers all JDK annotations
public static boolean isAnnotationMarkedBy(Class<? extends Annotation> annotationType,
                                           Class<? extends Annotation> otherAnnotationClass) {
    return isAnnotationMarkedBy(annotationType, otherAnnotationClass, new HashSet<>());
}

private static boolean isAnnotationMarkedBy(Class<? extends Annotation> annotationType,
                                            Class<? extends Annotation> target,
                                            Set<Class<? extends Annotation>> visited) {
    if (!visited.add(annotationType)) return false;           // cycle guard
    if (annotationType.isAnnotationPresent(target)) return true;
    return Arrays.stream(annotationType.getAnnotations())
            .filter(a -> !a.annotationType().getPackageName().startsWith("java.")
                      && !a.annotationType().getPackageName().startsWith("javax."))
            .anyMatch(a -> isAnnotationMarkedBy(a.annotationType(), target, visited));
}
```

This is a defensive change — no known cycle exists in the current annotation topology, but the
recursion is called on every class during scan so a future mis-annotated annotation would cause a
stack overflow that is hard to diagnose.

---

---

## 14 — `ClassBeanTypeResolver` — non-deterministic provider selection

**File:** `ClassBeanTypeResolver.java:19-23`

When multiple beans provide the same type, `resolveOrNull()` picks one arbitrarily:

```java
Set<CafeMember> providers = beansFactory.getCafeMetadataRegistry().findAnyTypeProviders(typeKey);
return providers.stream()
        .findFirst()   // ← stream of HashSet — order depends on hash values; changes between JVM runs
        .map(memberInfo -> resolveProvider(memberInfo, beansFactory))
        .orElse(null);
```

`findAnyTypeProviders()` returns an unordered `Set<CafeMember>`. `findFirst()` on a `HashSet` stream
is non-deterministic — the "winning" provider can silently change between runs.

This path is only reached when the bean is NOT yet in the repository (first-time resolution for a
non-collection type). The correct behaviour: if more than one provider exists and none is marked
`@CafePrimary`, throw an ambiguity error. The `@CafePrimary` selection already works at the
repository level (`BeansRepository.getOne()`), but the resolver level has no equivalent guard.

```java
// fix
@Override
public Object resolveOrNull(BeanTypeKey typeKey, CafeBeansFactory beansFactory) {
    Set<CafeMember> providers = beansFactory.getCafeMetadataRegistry().findAnyTypeProviders(typeKey);
    if (providers.isEmpty()) return null;
    if (providers.size() > 1) {
        // select @CafePrimary provider, or throw if none
        providers = providers.stream()
                .filter(m -> m.getAnnotationModifiers().contains(CafePrimary.class))
                .collect(Collectors.toSet());
        if (providers.size() != 1) {
            throw new CafeBeanResolverException("Ambiguous providers for " + typeKey, typeKey);
        }
    }
    return resolveProvider(providers.iterator().next(), beansFactory);
}
```

---

## 15 — `resolve()` / `resolveOrNull()` contract inconsistency

**Files:** `ArrayBeanTypeResolver.java`, `CollectionBeanTypeResolver.java`, `ClassBeanTypeResolver.java`

The three type resolvers implement the same interface but with inconsistent contracts:

| Resolver | `resolve()` when nothing found | `resolveOrNull()` behaviour |
|---|---|---|
| `ClassBeanTypeResolver` | throws `CafeBeanResolverException` | returns `null` ✓ |
| `CollectionBeanTypeResolver` | throws on bad type args | **calls `resolve()` — throws** ✗ |
| `ArrayBeanTypeResolver` | calls `resolveOrNull()` — **returns `null`** ✗ | returns `null` ✓ |

`CollectionBeanTypeResolver.resolveOrNull()`:
```java
@Override
public Object resolveOrNull(BeanTypeKey typeKey, CafeBeansFactory beansFactory) {
    return resolve(typeKey, beansFactory);   // ← throws CollectionTypeResolverException on bad input
}
```
Callers of `resolveOrNull` (e.g. `CafeBeansFactory.getBeanOrNull`) expect no exception — they will
not get `null`, they get an unhandled `CollectionTypeResolverException`.

`ArrayBeanTypeResolver.resolve()`:
```java
@Override
public Object resolve(BeanTypeKey typeKey, CafeBeansFactory beansFactory) {
    return resolveOrNull(typeKey, beansFactory);   // ← returns null; silently injects null into fields
}
```
An array injection with no providers silently sets the field to `null` rather than failing at startup.

**Fix:**
- `CollectionBeanTypeResolver.resolveOrNull()`: catch exceptions and return an empty collection
- `ArrayBeanTypeResolver.resolve()`: throw `ArrayTypeResolverException` if `resolveOrNull` returns `null`

---

## 16 — Document collection-injection prototype semantics

**File:** `CollectionBeanTypeResolver.java:63-65`

When `List<T>` (or `Set<T>`) is injected and `T` has prototype providers, the resolver creates
fresh instances — one per provider class — on every injection:

```java
Collection<Object> prototypes = beansFactory.getCafeMetadataRegistry()
        .findPrototypeProviders(typeKey).stream()
        .map(memberInfo -> resolveProvider(beansFactory, memberInfo))   // ← new instance each time
        .collect(Collectors.toSet());
```

This means:
- Two beans injecting `List<T>` each receive separate list with separate instances
- User-created instances (e.g. from `addComponent()`) are NOT included — they are outside the
  metadata registry entirely
- If you need "all currently live instances," you must maintain that collection yourself

This is the correct behaviour for prototypes in a DI context, but it is not obvious.
It should be documented in [di/BeanScopeDesign.md](../di/BeanScopeDesign.md) alongside the
general prototype caveats.

---

## 17 — `SingletonConstructorResolver` re-instantiates on collection injection path

**File:** `SingletonConstructorResolver.java`, `AbstractConstructorResolver.java`

When `List<SomeSingleton>` is injected, `CollectionBeanTypeResolver.resolveBeansByProvider()`
calls `resolveProvider()` for every singleton provider — even if that singleton is already in
the repository. This routes to `SingletonConstructorResolver`, which:

1. Calls `super.resolve()` → `AbstractConstructorResolver.resolve()` → **allocates a new instance**
2. Calls `cafeBeansFactory.persist()` → repository deduplication guard sees same source →
   **discards the new instance**

The allocation in step 1 is wasted. The number of wasted allocations equals
(number of `List<T>` injection sites) × (number of singleton providers for `T`).

```java
// fix — guard in SingletonConstructorResolver
@Override
public Object resolve(CafeConstructor descriptor, CafeBeansFactory beansFactory) {
    BeanTypeKey key = descriptor.getParent().getRootClassTypeKey();
    if (beansFactory.isResolved(key)) {
        return beansFactory.getResolved(key);   // reuse; skip allocation
    }
    Object instance = super.resolve(descriptor, beansFactory);
    beansFactory.persist(descriptor, instance);
    return instance;
}
```

---

## Implementation order

```
11 (copy-paste fix)          — trivial, anytime
15 (resolve contract)        — small correctness fix, low risk, anytime
16 (prototype collection doc) — documentation only, anytime
10 (handlerClassAnnotations) — do alongside fix #1; both are prerequisite for correct handler lookup
1  (fix find bug)            — prerequisite for event dispatch to work at all
14 (non-deterministic resolver) — fix before any multi-provider scenario is used
17 (singleton re-instantiation) — optimization; do after 15 (same area)
12 (topology blockers)       — prerequisite for #8; bundle with it
8  (annotation topology)     — independent; prerequisite for @CafeComponent in cafe-desktop
9  (meta-annotation cleanup) — independent; can be done alongside 8
13 (cycle guard)             — independent; low-risk, do anytime
3  (rename)                  — prerequisite for 4
2  (dispatchAll/To)          — prerequisite for 5
4  (expose as bean)          — prerequisite for 5, 6
7  (generic resolver)        — prerequisite for cafe-desktop @CafeEventHandler
5  (EventDispatcher)         ← depends on 2, 4
6  (EventHub)                ← depends on 4, 5
18 (Optional<T>)             — independent; do alongside 15 (same resolver area)
```
