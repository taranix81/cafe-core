# Cafe Beans — Bean Scope Design

See also: [Annotations.md](../architecture/Annotations.md) — full annotation reference.

---

## Annotation topology

Scope is expressed by which annotation is placed on the class — there is no `scope` attribute.
`@CafeService` is a pure meta-annotation (scanner anchor only, no attributes).

### Hierarchy

```
@CafeService   (meta — scanner anchor, @Target(ANNOTATION_TYPE) only, no attributes)
  ├── @CafeSingleton   @Target(TYPE, ANNOTATION_TYPE)   — one instance, lives forever
  └── @CafePrototype   @Target(TYPE, ANNOTATION_TYPE)   — new instance per construction
        └── @CafeComponent  (cafe-desktop)              — UI component, implies prototype
```

### Annotation definitions

```java
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
public @interface CafeService { }              // no scope() attribute

@CafeService
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
public @interface CafeSingleton { }

@CafeService
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
public @interface CafePrototype { }
```

### Usage

```java
@CafeSingleton
class FileExplorerContainer implements ContainerComponent { ... }

@CafeComponent                                 // implies @CafePrototype → @CafeService
class EditorContainer implements ContainerComponent { ... }
```

---

## Scope resolution

`CafeAnnotationUtils.getScope()` traverses the annotation chain with cycle protection:

```java
public static Scope getScope(Class<?> clazz) {
    if (isAnnotationMarkedBy(clazz, CafePrototype.class))  return Scope.Prototype;
    if (isAnnotationMarkedBy(clazz, CafeSingleton.class))  return Scope.Singleton;
    return Scope.Singleton;   // unannotated classes default to singleton
}
```

`isAnnotationMarkedBy` uses a visited-set to guard against annotation-hierarchy cycles and
filters out standard JDK annotations (`java.*`, `javax.*`) from traversal.

---

## Prototype injection semantics

### Single-bean injection

When a prototype class is injected directly (e.g. `@CafeInject MyPrototype field`), a **fresh
instance is created** on each injection. Two beans receiving the same prototype type each get
their own distinct instance.

### Collection injection

When `List<T>` or `Set<T>` is injected and `T` has prototype providers, `CollectionBeanTypeResolver`
creates fresh instances at injection time:

```java
// CollectionBeanTypeResolver.resolveBeansByProvider() — prototype path
Collection<Object> prototypes = beansFactory.getCafeMetadataRegistry()
        .findPrototypeProviders(typeKey).stream()
        .map(memberInfo -> resolveProvider(beansFactory, memberInfo))  // new instance each call
        .collect(Collectors.toSet());
```

Consequences:
- Each injection site gets a **separate set of fresh instances** — they are not shared
- Instances created outside the DI container (e.g. via `addBeanToContext()`) are **not included**
  — they bypass the metadata registry entirely
- If you need "all currently live instances," maintain that collection yourself

Singleton and prototype items are merged: a `List<T>` injection collects all registered
singletons of type `T` plus fresh prototype instances.

---

## Scanner depth

`@CafeService`-chain traversal supports two hops: `@CafeComponent → @CafePrototype → @CafeService`.
The scanner uses `isAnnotationMarkedBy()` which handles this chain transparently.

---

## What is decided

| Topic | Decision |
|---|---|
| `@CafeService` | Pure meta-annotation, `@Target(ANNOTATION_TYPE)` only, no attributes |
| `@CafeSingleton` | First-class scope annotation — replaces bare `@CafeService` on singleton beans |
| `@CafePrototype` | First-class scope annotation — moved from `@CafeModifier` to `@CafeService` family |
| `Scope` enum | Internal scanner detail — not public API |
| Scanner depth | Two hops — supports `@CafeComponent → @CafePrototype → @CafeService` chain |
| Prototype collection | Fresh instances per injection site; not shared across injection points |
