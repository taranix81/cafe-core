# Cafe Beans — Annotation Reference

All annotations in `cafe-beans`, organized by purpose.

---

## Meta-annotations (annotation types)

These are placed on **other annotations**, not on classes. They are the scanner anchors.

| Annotation | Package | Purpose |
|---|---|---|
| `@CafeWiringType` | `annotations.base` | Marks an annotation as a DI wiring annotation — bean discovery, injection, lifecycle, properties |
| `@CafeHandlerType` | `annotations.base` | Marks an annotation as a handler annotation — event handler method discovery |
| `@CafeModifier` | `annotations.modifiers` | Marks an annotation as a behavioural modifier — changes how a bean or annotation behaves |

`@CafePropertyType` was removed — `@CafeProperty` is now directly under `@CafeWiringType`.

---

## Bean / class annotations

Placed on **classes** to declare them as DI-managed beans.

### `@CafeService`

Scanner anchor — **meta-annotation only**. Not placed directly on beans.

```java
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)           // annotation types only — never on classes
public @interface CafeService { }              // no attributes
```

### `@CafeSingleton`

```java
@CafeService
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
public @interface CafeSingleton { }
```

One instance per container, created at init, held for the application lifetime.

### `@CafePrototype`

```java
@CafeService
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
public @interface CafePrototype { }
```

New instance per explicit construction; container does not hold a reference after creation.
Can be used as a meta-annotation — `@CafeComponent` in `cafe-desktop` is annotated `@CafePrototype`
so desktop components automatically get prototype semantics.

### `Scope` enum

```java
public enum Scope { Singleton, Prototype }
```

Internal scanner detail — not part of the public annotation API.

### `@CafeApplication`

```java
@CafeWiringType
@Target(ElementType.TYPE)
public @interface CafeApplication { }
```

Marks the application entry-point class. Required on the class passed to the `CafeApplication` constructor.

---

## Modifier annotations (`@CafeModifier`)

Placed on **classes or annotations** to alter default DI behaviour.

| Annotation | Effect |
|---|---|
| `@CafePrimary` | Marks a bean as preferred when multiple candidates exist for a type |
| `@CafeName(value)` | Assigns a named identifier to a bean — used for named injection |
| `@CafeOptional` | Marks an injection point as optional — no error if no bean found |

---

## Field annotations (`@CafeWiringType`)

Placed on **fields** within a bean.

| Annotation | Effect |
|---|---|
| `@CafeInject` | Injects a dependency — resolved by type (and optionally name) from the container |
| `@CafeProperty(key)` | Injects a value from `application.properties` |

`@CafeProperty` has `@CafeWiringType` directly (not via the removed `@CafePropertyType`).
`WireFieldResolver` explicitly excludes `@CafeProperty` fields so they are handled by `PropertyResolver` only.

---

## Method annotations (`@CafeWiringType` / `@CafeHandlerType`)

Placed on **methods** within a bean.

| Annotation | Meta-annotation | Effect |
|---|---|---|
| `@CafeHandler` | `@CafeHandlerType` | Marks a method as an event handler — registered by `SingletonHandlerMethodResolver` |
| `@CafePostInit` | `@CafeWiringType` | Invoked after the bean is fully wired — equivalent to `@PostConstruct` |
| `@CafeProvider` | `@CafeWiringType` | Marks a method as a bean provider — return value registered as a bean in the container |

---

## Annotation hierarchy

```
@CafeWiringType
  ├── @CafeApplication        class  — entry point
  ├── @CafeInject             field  — dependency injection
  ├── @CafeProperty           field  — property injection
  ├── @CafePostInit           method — post-construction lifecycle
  └── @CafeProvider           method — factory method bean registration

@CafeService  (independent meta — DI bean scanner anchor, no attributes)
  ├── @CafeSingleton          class/annotation  — singleton scope
  └── @CafePrototype          class/annotation  — prototype scope
        └── @CafeComponent    class  (cafe-desktop)

@CafeHandlerType
  └── @CafeHandler            method — event handler registration

@CafeModifier
  ├── @CafePrimary            class  — preferred candidate
  ├── @CafeName               class  — named bean
  └── @CafeOptional           class  — optional injection
```
