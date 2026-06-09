# Cafe Beans — CafeResolvers

`CafeResolvers` is the resolver registry — the central lookup table that maps a descriptor or
type key to the concrete resolver responsible for handling it.

See also: [ResolvableTypes.md](ResolvableTypes.md) — what injection types the container supports.  
See also: [CafeApplicationContext.md](CafeApplicationContext.md) — how `CafeResolvers` is assembled.

---

## Role

Every DI operation routes through `CafeResolvers`:

```
CafeBeansFactory needs to resolve something
  → CafeResolvers.find*Resolver(descriptor)
  → selects the right resolver from its registered sets
  → resolver.resolve(descriptor, beansFactory)
```

`CafeResolvers` does NOT resolve beans itself — it only dispatches.

---

## Resolver families

| Family | Interface | Default implementations | Selects by |
|---|---|---|---|
| **Class** | `CafeClassResolver` | `DefaultClassResolver` | annotation on the class (`@CafeService`, `@CafeApplication`) |
| **Constructor** | `CafeConstructorResolver` | `SingletonConstructorResolver`, `PrototypeConstructorResolver` | scope (`isSingleton()`) |
| **Field** | `CafeFieldResolver` | `WireFieldResolver`, `PropertyResolver` | annotation on the field (`@CafeWirerType`, `@CafePropertyType`) |
| **Method** | `CafeMethodResolver` | `SingletonWireMethodResolver`, `PrototypeWireMethodResolver`, `SingletonHandlerMethodResolver` | scope + annotation on the method |
| **Bean type** | `CafeBeanTypeResolver` | `ClassBeanTypeResolver`, `CollectionBeanTypeResolver`, `ArrayBeanTypeResolver` | shape of the `BeanTypeKey` |
| **Provider** | `CafeProviderResolver` | `DefaultProviderResolver` | member kind (constructor, method) |

---

## Selection protocol

All `find*` methods use the same two-step filter:

```
1. isApplicable(descriptor)   — coarse filter: is this resolver capable in principle?
2. supports(annotationType)   — fine filter: does this resolver handle this specific annotation/type?
```

If exactly one resolver matches → it is returned.  
If zero resolvers match → throws `CafeBeansFactoryException("No resolver found for ...")`.  
If more than one matches → throws `CafeBeansFactoryException("Too many resolvers for ...")`.

The `findProviderResolver()` only applies `isApplicable()` (no annotation filter).

---

## Bean type resolver dispatch

`findBeanTypekeyResolver(BeanTypeKey)` only uses `isApplicable()`:

| `BeanTypeKey` shape | Resolver selected |
|---|---|
| Array (`T[]`) | `ArrayBeanTypeResolver` |
| Collection (`List<T>`, `Set<T>`, `Collection<T>`) | `CollectionBeanTypeResolver` |
| Everything else | `ClassBeanTypeResolver` |

`Optional<T>` is not yet handled — proposed in [ResolvableTypes.md](ResolvableTypes.md).

---

## Default resolvers

### `DefaultClassResolver`

Handles all classes annotated with `@CafeService` or `@CafeApplication`.
Routes to `AbstractClassResolver.resolve()`:
1. Resolve constructor → get instance
2. Resolve all fields (inject dependencies)
3. Resolve all methods (`@CafePostInit`, `@CafeProvider`, `@CafeHandler`)

Note: `supports()` is hardcoded to `Set.of(CafeService.class, CafeApplication.class)` —
will need updating when annotation topology changes (#8).
See [ProposedChanges #12B](../proposal-changes/ProposedChanges.md#12).

### `SingletonConstructorResolver`

Creates the instance and persists it to the repository.
`isApplicable()` returns `true` when `descriptor.isSingleton()`.
The duplicate-source guard in `BeansRepository` prevents double-persistence.

### `PrototypeConstructorResolver`

Creates the instance but does NOT persist it.
`isApplicable()` returns `true` when `descriptor.isPrototype()`.
Each call creates a new instance.

### `WireFieldResolver`

Handles `@CafeInject` fields. Calls `getBean()` normally or `getBeanOrNull()` if
`@CafeOptional` is present. Sets the resolved value via reflection.

### `PropertyResolver`

Handles `@CafeProperty` fields. Reads from `application.properties` via `BeansRepository`
(property entries loaded by `CafePropertiesService.load()` at context construction time).

### `SingletonWireMethodResolver`

Handles `@CafeProvider` and `@CafePostInit` methods on singletons. Invokes the method
and persists the return value. Guards against double-execution via `hasBeenExecuted()`.

### `SingletonHandlerMethodResolver`

Handles `@CafeHandler` methods. Registers them into the repository as `HandlerTypeKey` entries
for later dispatch via `CafeHandlerExecutorService`.
Currently hard-coded to `@CafeHandler` — see [ProposedChanges #7](../proposal-changes/ProposedChanges.md#7).

### `DefaultProviderResolver`

Called by type resolvers to force-resolve a specific member.
For a constructor → delegates to `findClassResolver().resolve()`.
For a method → finds the owner instance and invokes the method.

---

## Adding custom resolvers

Custom resolvers are registered before `build()` via `BeansContextBuilder`:

```java
CafeApplicationContext.builder()
    .withMethodResolver(Set.of(new MyCustomMethodResolver()))
    .withClassResolver(Set.of(new MyCustomClassResolver()))
    .build();
```

Custom resolvers are added to the same sets as defaults and participate in the same
`isApplicable` + `supports` selection. A custom resolver that returns `isApplicable = true`
for the same descriptor as an existing resolver will cause `TOO_MANY_RESOLVERS` unless
the default resolver narrows its `isApplicable` predicate.

---

## What is decided

| Topic | Decision |
|---|---|
| Selection rule | Two-step: `isApplicable` then `supports` — exactly one resolver must match |
| Error on ambiguity | `TOO_MANY_RESOLVERS` exception — prevents silent misbehaviour |
| Extension point | Custom resolvers registered via `BeansContextBuilder` before `build()` |
| `CafeBeanTypeResolver` | Applies `isApplicable` only — no annotation filter |

## What is open

| Topic | Status |
|---|---|
| `OptionalBeanTypeResolver` | Not yet added — see [ResolvableTypes.md](ResolvableTypes.md) |
| `DefaultClassResolver.supports()` hardcoded | Will break after annotation topology change — see [ProposedChanges #12B](../proposal-changes/ProposedChanges.md#12) |
| `SingletonHandlerMethodResolver` hard-coded to `@CafeHandler` | Needs parameterisation — see [ProposedChanges #7](../proposal-changes/ProposedChanges.md#7) |
| `CafeResolvers` mutable post-construction | `add()` can be called after `resolveAllBeans()` — no guard |
