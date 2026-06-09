# Cafe Beans — Resolvable Injection Types

What types the container can inject at a `@CafeInject` field or constructor parameter.
Each type maps to a `CafeBeanTypeResolver` selected by `CafeResolvers.findBeanTypekeyResolver()`.

See also: [CafeResolvers.md](CafeResolvers.md) — how resolvers are selected.  
See also: [BeanScopeDesign.md](BeanScopeDesign.md) — singleton vs prototype scoping.  
See also: [../proposal-changes/ProposedChanges.md](../proposal-changes/ProposedChanges.md) — known issues referenced below.

---

## Quick reference

| Injection type | Resolver | Missing provider → | Notes |
|---|---|---|---|
| `T` | `ClassBeanTypeResolver` | throws at startup | `@CafeOptional` injects `null` |
| `List<T>` / `Set<T>` / `Collection<T>` | `CollectionBeanTypeResolver` | empty collection | prototypes: fresh instance per provider |
| `T[]` | `ArrayBeanTypeResolver` | `null` (bug — see [#15](../proposal-changes/ProposedChanges.md#15)) | delegates to collection resolver |
| `Optional<T>` | *(proposed — not implemented)* | `Optional.empty()` | replaces `@CafeOptional` |

---

## Single bean — `T`

**Resolver:** `ClassBeanTypeResolver`  
**Applies when:** the injection type is a plain class or interface.

```java
@CafeInject
FooService fooService;

// or via constructor
class MyBean {
    MyBean(FooService fooService) { ... }
}
```

`ClassBeanTypeResolver` calls `findAnyTypeProviders(typeKey)` and resolves the first match.

| Scenario | Behaviour |
|---|---|
| One provider | Resolves and injects it |
| No provider | Throws `CafeBeanResolverException` — startup fails |
| No provider + `@CafeOptional` | `WireFieldResolver` calls `getBeanOrNull()` → injects `null` |
| Multiple providers, one `@CafePrimary` | `BeansRepository.getOne()` selects the primary |
| Multiple providers, no `@CafePrimary` | **Non-deterministic** — `findFirst()` on a `HashSet` stream; see [ProposedChanges #14](../proposal-changes/ProposedChanges.md#14) |

Named injection — `@CafeName` narrows resolution to a specific registered name:

```java
@CafeInject
@CafeName("backup")
FooService backupService;
```

### `@CafeOptional` — current null-injection behaviour

`@CafeOptional` is a modifier annotation, not a type. If the provider is absent, the field is
set to `null`. The caller must null-check.

```java
@CafeInject
@CafeOptional
FooService fooService;   // null if not registered — caller must check

if (fooService != null) { ... }
```

`Optional<T>` (proposed below) replaces this pattern.

---

## Collection — `List<T>`, `Set<T>`, `Collection<T>`

**Resolver:** `CollectionBeanTypeResolver`  
**Applies when:** the injection type is a parameterised `Collection` subtype.

```java
@CafeInject
List<Component> components;

@CafeInject
Set<EventListener> listeners;
```

The resolver collects ALL providers of element type `T`:

```
1. findSingletonProviders(T)  → force-resolve each if not yet in repository
2. getAllResolved(T)           → gather all singleton instances from repository
3. findPrototypeProviders(T)  → instantiate one fresh instance per prototype provider
4. return union of singletons + prototype instances
```

| Element scope | What is injected |
|---|---|
| Singleton | The single resolved instance from the repository |
| Prototype | **A freshly-created instance for this injection** — one per provider class |

The collection is **not cached**. Every injection rebuilds it from the repository.
Each injection site receives its own independent `List` / `Set` instance.

**Prototype caveat:** instances created outside the container (e.g. via `addComponent()`) are
NOT included — only framework-managed instances appear in the list.
See [BeanScopeDesign.md](BeanScopeDesign.md) — "Prototypes — the problem."

Concrete return types:
- `List<T>` → `ArrayList`
- `Set<T>` → `HashSet`
- `Collection<T>` → `ArrayList` (fallback)

Known issue: `resolveOrNull()` delegates to `resolve()` which throws on invalid type arguments
rather than returning an empty collection — see [ProposedChanges #15](../proposal-changes/ProposedChanges.md#15).

---

## Array — `T[]`

**Resolver:** `ArrayBeanTypeResolver`  
**Applies when:** the injection type is an array.

```java
@CafeInject
Component[] components;
```

Delegates entirely to `CollectionBeanTypeResolver` with `Collection<T>` as the intermediate type,
then converts the result to an array via `Array.newInstance`. The collection semantics (singleton
vs prototype, no external instances) are identical to the collection resolver above.

Known issue: `resolve()` returns `null` instead of throwing when no providers are found,
silently leaving the field as `null` — see [ProposedChanges #15](../proposal-changes/ProposedChanges.md#15).

---

## `Optional<T>` *(proposed)*

**Status:** Not implemented. No `OptionalBeanTypeResolver` exists.

**Motivation:** `@CafeOptional` currently injects `null` for absent beans, requiring null-checks
throughout the codebase. `Optional<T>` is the idiomatic Java way to express "this dependency may
not be present":

```java
// current — null injection
@CafeInject
@CafeOptional
FooService fooService;          // null if absent — caller must check

// proposed — Optional<T>
@CafeInject
Optional<FooService> fooService; // Optional.empty() if absent — caller uses ifPresent / orElse
```

### Proposed behaviour

| Scenario | Injected value |
|---|---|
| One provider found | `Optional.of(resolved)` |
| No provider | `Optional.empty()` |
| Multiple providers, one `@CafePrimary` | `Optional.of(primary)` |
| Multiple providers, no `@CafePrimary` | throw — ambiguous |

### Proposed implementation

```java
public class OptionalBeanTypeResolver implements CafeBeanTypeResolver {

    @Override
    public boolean isApplicable(BeanTypeKey typeKey) {
        return typeKey.isParametrizedType()
            && ((ParameterizedType) typeKey.getType()).getRawType().equals(Optional.class);
    }

    @Override
    public Object resolve(BeanTypeKey typeKey, CafeBeansFactory beansFactory) {
        Type innerType = typeKey.getActualParameters()[0];
        Object bean = beansFactory.getBeanOrNull(BeanTypeKey.from(innerType));
        return Optional.ofNullable(bean);
    }

    @Override
    public Object resolveOrNull(BeanTypeKey typeKey, CafeBeansFactory beansFactory) {
        return resolve(typeKey, beansFactory);   // Optional.empty() is never null
    }
}
```

Register in `CafeResolvers`:

```java
private final Set<CafeBeanTypeResolver> beanTypekeyResolvers = new HashSet<>(List.of(
    new ClassBeanTypeResolver(),
    new ArrayBeanTypeResolver(),
    new CollectionBeanTypeResolver(),
    new OptionalBeanTypeResolver()));   // ← add
```

### Validator change

`CafeResolvableBeansValidator` must recognise `Optional<T>` fields as inherently optional —
`Optional.empty()` is a valid resolved value, not a missing dependency. Without this change,
the validator throws at startup for every `Optional<T>` field whose provider is absent.

```java
// in findNonResolvableTypeForMember():
if (fieldType.isOptional()) {    // new check
    return null;                 // skip — absence is valid
}
```

### Migration path

`@CafeOptional` on plain `T` fields can remain for backward compatibility.
`Optional<T>` becomes the preferred pattern for optional dependencies going forward.

---

## What is NOT supported

| Type | Notes |
|---|---|
| `Map<K, V>` | No `MapBeanTypeResolver` — a `Map<String, T>` keyed by `@CafeName` would be a natural extension |
| `Supplier<T>` | No lazy injection — every bean is resolved eagerly at `initialize()` |
| Raw `Collection` (no type parameter) | Throws `CollectionTypeResolverException` at injection time |
| Generics with multiple type parameters (`Map`, `Pair`) | Not supported; only single-parameter generics work |

---

## What is decided

| Topic | Decision |
|---|---|
| Single `T` | Resolved by type + optional `@CafeName`; `@CafePrimary` disambiguates multiples |
| Collection / Array | All providers included; each injection site gets a fresh independent instance |
| Prototype in collection | Always fresh — one new instance per provider class per injection |
| `@CafeOptional` | Injects `null` for absent single beans — not `Optional.empty()` |

## What is open

| Topic | Status |
|---|---|
| `Optional<T>` injection | Proposed — needs `OptionalBeanTypeResolver` + validator change |
| Non-deterministic multi-provider | `ClassBeanTypeResolver` picks arbitrarily — see [ProposedChanges #14](../proposal-changes/ProposedChanges.md#14) |
| `resolve()` / `resolveOrNull()` contracts | Inconsistent across resolvers — see [ProposedChanges #15](../proposal-changes/ProposedChanges.md#15) |
| `Map<String, T>` injection | Not planned — evaluate when named-bean patterns become common |
