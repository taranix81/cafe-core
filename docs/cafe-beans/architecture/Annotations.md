# Cafe Beans — Annotation Reference

All annotations in `cafe-beans`, organized by purpose.

---

## Meta-annotations (annotation types)

These are placed on **other annotations**, not on classes. They are the scanner anchors.

| Annotation | Package | Purpose | Status |
|---|---|---|---|
| `@CafeWirerType` | `annotations.base` | Marks an annotation as a DI wiring annotation — bean discovery, injection, lifecycle, properties | **→ rename to `@CafeWiringType`** |
| `@CafeHandlerType` | `annotations.base` | Marks an annotation as a handler annotation — event handler method discovery | Unchanged |
| `@CafePropertyType` | `annotations.base` | Marks an annotation as a property annotation — only `@CafeProperty` uses it | **→ remove; `@CafeProperty` moves to `@CafeWiringType`** |
| `@CafeModifier` | `annotations.modifiers` | Marks an annotation as a behavioural modifier — changes how a bean or annotation behaves | `@CafePrototype` removed from it (moves to `@CafeService` family) |

---

## Bean / class annotations (`@CafeWirerType`)

Placed on **classes** to declare them as DI-managed beans.

### `@CafeService`

Scanner anchor — **meta-annotation only**. Not placed directly on beans.

```java
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)           // annotation types only — never on classes
public @interface CafeService { }              // no scope() attribute
```

> **Proposed:** Remove `scope()` attribute. See [di/BeanScopeDesign.md](../di/BeanScopeDesign.md).  
> Currently: `@CafeService(scope = Scope.Singleton/Prototype)` — attribute still present in source.

### `@CafeSingleton` *(proposed)*

```java
@CafeService
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
public @interface CafeSingleton { }
```

One instance per container, created at init, held for the application lifetime.
Replaces bare `@CafeService` on singleton beans.

### `@CafePrototype` *(topology change proposed)*

```java
@CafeService                                   // proposed: move from @CafeModifier
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
public @interface CafePrototype { }
```

New instance per explicit construction; container does not hold a reference after creation.
Currently exists as `@CafeModifier` — proposed to move into the `@CafeService` family.

### `Scope` enum

```java
public enum Scope { Singleton, Prototype }
```

Internal scanner detail — not part of the public annotation API after the topology change.

### `@CafeApplication`

```java
@CafeWirerType
@Target(ElementType.TYPE)
public @interface CafeApplication { }
```

Marks the application entry-point class. Required on the class passed to `CafeApplication` constructor.

---

## Modifier annotations (`@CafeModifier`)

Placed on **classes or annotations** to alter default DI behaviour.

| Annotation | Effect |
|---|---|
| `@CafePrimary` | Marks a bean as preferred when multiple candidates exist for a type |
| `@CafeName(value)` | Assigns a named identifier to a bean — used for named injection |
| `@CafeOptional` | Marks an injection point as optional — no error if no bean found |
| `@CafePrototype` | Currently: marks an annotation as prototype-scoped (meta-annotation use). **Proposed:** move to `@CafeService` family as first-class scope annotation |

`@CafePrototype` (modifier) is distinct from `@CafeService(scope = Scope.Prototype)`:
it is intended as a meta-annotation to declare custom annotations that imply prototype scope
(e.g. `@CafeComponent` in `cafe-desktop` would be annotated with `@CafePrototype`).

---

## Field annotations (`@CafeWirerType` / `@CafePropertyType`)

Placed on **fields** within a bean.

| Annotation | Effect |
|---|---|
| `@CafeInject` | Injects a dependency — resolved by type (and optionally name) from the container |
| `@CafeProperty(key)` | Injects a value from `application.properties` |

---

## Method annotations (`@CafeWirerType` / `@CafeHandlerType`)

Placed on **methods** within a bean.

| Annotation | Meta-annotation | Effect |
|---|---|---|
| `@CafeHandler` | `@CafeHandlerType` | Marks a method as an event handler — registered by `SingletonHandlerMethodResolver` |
| `@CafePostInit` | `@CafeWirerType` | Invoked after the bean is fully wired — equivalent to `@PostConstruct` |
| `@CafeProvider` | `@CafeWirerType` | Marks a method as a bean provider — return value registered as a bean in the container |

---

## Summary: annotation hierarchy

Proposed topology (see [di/BeanScopeDesign.md](../di/BeanScopeDesign.md) and [proposal-changes/ProposedChanges.md](../proposal-changes/ProposedChanges.md)):

```
@CafeWiringType  (renamed from @CafeWirerType — absorbs @CafePropertyType)
  ├── @CafeApplication        class  — entry point
  ├── @CafeInject             field  — dependency injection
  ├── @CafeProperty           field  — property injection  ← moved from @CafePropertyType
  ├── @CafePostInit           method — post-construction lifecycle
  └── @CafeProvider           method — factory method bean registration

@CafeService  (independent meta — DI bean scanner anchor, no attributes)
  ├── @CafeSingleton          class/annotation  ← NEW
  └── @CafePrototype          class/annotation  ← moved from @CafeModifier
        └── @CafeComponent    class  (cafe-desktop)

@CafeHandlerType  (unchanged)
  └── @CafeHandler            method — event handler registration

@CafeModifier  (unchanged except @CafePrototype removed)
  ├── @CafePrimary            class  — preferred candidate
  ├── @CafeName               class  — named bean
  └── @CafeOptional           class  — optional injection

@CafePropertyType  → REMOVED
```
