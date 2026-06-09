# Cafe Beans — Bean Scope Design

See also: [Annotations.md](../architecture/Annotations.md) — full annotation reference.  
See also: [ProposedChanges.md](../proposal-changes/ProposedChanges.md) — implementation plan.

---

## Current state (to be replaced)

Scope is currently declared via an attribute on `@CafeService`:

```java
@CafeService                                   // Scope.Singleton (default)
@CafeService(scope = Scope.Prototype)          // Scope.Prototype
```

Problems with this approach:
- `@CafeService` carries domain-level meaning (scope) as an attribute — inconsistent with all other `cafe-beans` meta-annotations which are pure markers
- Adding a new scope requires modifying `@CafeService` and the `Scope` enum
- Cannot compose cleanly — `@CafeComponent` in `cafe-desktop` cannot imply prototype scope via meta-annotation
- `@CafePrototype` exists as a `@CafeModifier` but is unrelated to the scope mechanism — confusing

---

## Proposed topology

Remove the `scope` attribute entirely. `@CafeService` becomes a **pure meta-annotation** (scanner anchor only).
Scope is expressed by which concrete annotation is placed on the class.

### Annotation hierarchy

```
@CafeService   (meta — scanner anchor, @Target(ANNOTATION_TYPE) only, no attributes)
  ├── @CafeSingleton   @Target(TYPE, ANNOTATION_TYPE)   — one instance, lives forever
  └── @CafePrototype   @Target(TYPE, ANNOTATION_TYPE)   — new instance per construction
        └── @CafeComponent  (cafe-desktop)              — UI component, implies prototype
```

`@CafePrototype` moves out of `@CafeModifier` and into the `@CafeService` family.
`Scope` enum becomes an internal scanner detail — not part of the public annotation API.

### Annotation definitions

```java
// Pure meta-annotation — scanner anchor only
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
public @interface CafeService { }              // no scope() attribute
```

```java
@CafeService
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
public @interface CafeSingleton { }
```

```java
@CafeService
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
public @interface CafePrototype { }
```

```java
// cafe-desktop — derives from @CafePrototype
@CafePrototype
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface CafeComponent { }
```

### Usage

```java
@CafeSingleton
class FileExplorerContainer implements ContainerComponent { ... }

@CafeComponent                                 // implies @CafePrototype implies @CafeService
class EditorContainer implements ContainerComponent { ... }

@CafeSingleton
public class CafeHandlerSelector implements HandlerTypekeySelector { ... }
```

---

## Scanner changes

The scanner currently checks `cls.isAnnotationPresent(CafeService.class)` and reads `scope()`.
After the change it must traverse the annotation hierarchy — up to two hops to support `@CafeComponent`:

```java
// detect bean — walk annotation chain until @CafeService found or depth exceeded
boolean isBean(Class<?> cls) {
    return findCafeService(cls.getAnnotations(), 2) != null;
}

// determine scope — which concrete annotation is present
Scope scopeOf(Class<?> cls) {
    return isPrototype(cls.getAnnotations(), 2) ? Scope.Prototype : Scope.Singleton;
}

private boolean isPrototype(Annotation[] annotations, int depth) {
    if (depth == 0) return false;
    for (Annotation a : annotations) {
        if (a.annotationType() == CafePrototype.class) return true;
        if (isPrototype(a.annotationType().getAnnotations(), depth - 1)) return true;
    }
    return false;
}
```

`Scope` enum stays as an internal resolver concept — `PrototypeConstructorResolver` and
`PrototypeWireMethodResolver` already exist and continue to work based on the resolved scope.

---

## Migration path

| Current | After |
|---|---|
| `@CafeService` | `@CafeSingleton` |
| `@CafeService(scope = Scope.Prototype)` | `@CafePrototype` |
| `@CafeService` on `@CafeService` annotation itself | Remove (it was `@Target(TYPE, ANNOTATION_TYPE)`) |

All existing `@CafeService` usages in `cafe-beans` migrate to `@CafeSingleton`.
The `Scope` enum stays but is no longer part of the public API — internal use only.

---

## What is decided

| Topic | Decision |
|---|---|
| `@CafeService` | Pure meta-annotation, `@Target(ANNOTATION_TYPE)` only, no attributes |
| `@CafeSingleton` | New — replaces bare `@CafeService` on singleton beans |
| `@CafePrototype` | Moved from `@CafeModifier` to `@CafeService` family — first-class scope annotation |
| `Scope` enum | Kept as internal scanner detail — not public API |
| Scanner depth | Two hops — supports `@CafeComponent → @CafePrototype → @CafeService` chain |

## What is open

| Topic | Status |
|---|---|
| Max traversal depth | Two hops covers current needs — revisit if deeper chains arise |
| `@CafeModifier` cleanup | `@CafePrototype` removal from modifiers package — straightforward |
| Backward compat period | Remove old `scope` attribute immediately or deprecate first? |
