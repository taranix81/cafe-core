# Cafe Beans — Implementation Plan 0.0.6

Post-0.0.5 cycle. Covers code-review findings from a full source audit (99 main / 67 test files),
missing test coverage gaps, and structural refactoring.
All 18 items from the 0.0.5 plan are complete.

---

## Phase 1 — Code Review Fixes

Quick, localised changes with no design risk. Each item is one commit.

### 1.1 — CafeMember: remove Javadoc from trivial methods

**File:** `cafe-beans/src/main/java/.../metadata/CafeMember.java`

The class has Javadoc on 24+ methods, including one-liners that are fully self-described by their
name. The comments add maintenance burden with zero informational value.

**Remove** the Javadoc blocks from the following methods (keep the methods, only delete comments):

| Method | Reason |
|---|---|
| `isConstructor()` | Name is self-explanatory |
| `isField()` | Name is self-explanatory |
| `isMethod()` | Name is self-explanatory |
| `isStatic()` | Name is self-explanatory |
| `getParent()` | Trivial getter |
| `getParentTypeKey()` | Trivial getter |
| `getParentRootClass()` | Trivial getter |
| `getMemberDeclaringClass()` | Trivial getter |
| `isBelongToTheSameClass(CafeMember)` | Name is self-explanatory |
| `hashCode()` / `equals()` / `toString()` | Standard object overrides |

**Retain** Javadoc on: `getAnnotatedElement()`, `getAnnotationLifecycleMarkers()`,
`getAnnotationTypesMarkedBy()`, `hasDependencies()`, `getProvidedTypeKeys()`,
`getRequiredTypeKeys()`, `getRequiredPropertyTypeKeys()`.

The `"Should be moved to AnnotationUtils"` TODO comment in `getAnnotationTypesMarkedBy()` can be
removed — the method has lived there through the entire refactor and moving it would require making
`CafeAnnotationUtils` depend on `CafeMember`, creating a circular package dependency.

Commit: `refactor: remove trivial javadoc from CafeMember`

---

### 1.2 — Converters: log on parse failure and add explicit null guard

**Files:**
- `cafe-beans/src/main/java/.../converters/StringToIntegerConverter.java`
- `cafe-beans/src/main/java/.../converters/StringToDoubleConverter.java`
- `cafe-beans/src/main/java/.../converters/StringToLongConverter.java`
- `cafe-beans/src/main/java/.../converters/StringToBooleanConverter.java`

**Problem 1 — silent null on parse failure.** All three numeric converters swallow
`NumberFormatException` and return `null` silently. During startup this makes a misconfigured
`@CafeProperty` field appear to receive a valid `null` rather than failing loudly.

**Problem 2 — implicit null propagation.** None of the converters guards against a `null` source.
`Integer.parseInt(null)` throws `NumberFormatException` — the exception is caught and `null` is
returned. This is functionally correct but the code path is non-obvious.

**Changes to apply to all four converters (example for `StringToIntegerConverter`):**

```java
@Slf4j
public class StringToIntegerConverter implements CafeConverter<String, Integer> {
    @Override
    public Integer convert(String source) {
        if (source == null) return null;
        try {
            return Integer.parseInt(source.trim());
        } catch (NumberFormatException e) {
            log.warn("Cannot convert '{}' to Integer", source);
            return null;
        }
    }
}
```

`StringToBooleanConverter` already uses `Boolean.parseBoolean()` which never throws and accepts
`null` — add only the `@Slf4j` annotation and a `log.warn` for the case where the string is
neither `"true"` nor `"false"`:

```java
@Slf4j
public class StringToBooleanConverter implements CafeConverter<String, Boolean> {
    @Override
    public Boolean convert(String source) {
        if (source == null) return null;
        Boolean value = Boolean.parseBoolean(source.trim());
        if (!value && !source.trim().equalsIgnoreCase("false")) {
            log.warn("Cannot convert '{}' to Boolean — defaulting to false", source);
        }
        return value;
    }
}
```

Commit: `fix: log converter parse failures, guard against null source`

---

### 1.3 — AbstractClassResolver: document as extension point

**File:** `cafe-beans/src/main/java/.../resolvers/metadata/AbstractClassResolver.java`

The class has only one framework-internal subclass (`DefaultClassResolver`) but is also the base
for `CafeCommandClassResolver` in `cafe-shell` and two resolvers in `cafe-desktop`. Without a
comment, future developers may try to inline or seal the class.

Add a single class-level Javadoc:

```java
/**
 * Base class for class-level resolvers. Handles the standard resolve sequence:
 * constructor → fields → methods. Extend this class to add module-specific
 * class resolution logic (e.g. cafe-shell's @CafeCommand, cafe-desktop's SWT components).
 */
public abstract class AbstractClassResolver implements CafeClassResolver {
```

Commit: `docs: document AbstractClassResolver as intended extension point`

---

## Phase 2 — Test Coverage

Each section maps to one new test class (unless noted). Target: unit-level tests with minimal
fixture code — mock or minimal stub `CafeBeansFactory` where full DI is not needed.

### 2.1 — StringToXxxConverter tests

**New file:** `cafe-beans/src/test/.../converters/ConvertersTest.java`

One test class covering all four converters. Each converter needs:

| Scenario | Input | Expected |
|---|---|---|
| Valid positive number | `"42"` | `42` |
| Valid negative number | `"-7"` | `-7` |
| Whitespace trimming | `" 10 "` | `10` |
| Null input | `null` | `null` |
| Empty string | `""` | `null` (+ warn log) |
| Non-numeric string | `"abc"` | `null` (+ warn log) |
| Overflow (int/long) | `"9999999999"` for int | `null` |

`StringToBooleanConverter` specific:

| Scenario | Input | Expected |
|---|---|---|
| `"true"` | `"true"` | `true` |
| `"TRUE"` | `"TRUE"` | `true` |
| `"false"` | `"false"` | `false` |
| `"yes"` (non-boolean) | `"yes"` | `false` (+ warn log) |
| `null` | `null` | `null` |

Commit: `test: add converter unit tests`

---

### 2.2 — Type resolver unit tests

**New file:** `cafe-beans/src/test/.../resolvers/BeanTypeResolverTest.java`

Use a minimal `CafeBeansFactory` stub (or Mockito mock) that controls what `getBean`,
`getBeanOrNull`, and `getInstances` return.

#### `ClassBeanTypeResolver`

| Scenario | Setup | Expected |
|---|---|---|
| No providers | empty metadata | `resolveOrNull` → `null` |
| Exactly one provider | one bean registered | `resolveOrNull` → that bean |
| Multiple providers, one `@CafePrimary` | two beans, one primary | `resolveOrNull` → primary bean |
| Multiple providers, none primary | two beans, no primary | `resolveOrNull` → throws `CafeBeanResolverException` |

#### `CollectionBeanTypeResolver`

| Scenario | Expected |
|---|---|
| No providers | `resolve` → empty collection |
| Mixed singleton + prototype providers | `resolve` → collection with all instances |
| `resolveOrNull` called | returns empty collection, no exception |
| Non-parameterized type key | `resolve` → throws `CollectionTypeResolverException` |

#### `ArrayBeanTypeResolver`

| Scenario | Expected |
|---|---|
| Providers exist | `resolve` → array of beans |
| No providers | `resolve` → throws `ArrayTypeResolverException` |
| `resolveOrNull` called | returns null (not an exception) |

#### `OptionalBeanTypeResolver`

| Scenario | Expected |
|---|---|
| Bean present | `resolve` → `Optional.of(bean)` |
| Bean absent | `resolve` → `Optional.empty()` |
| `resolveOrNull` never returns null | `resolveOrNull` → `Optional.empty()` (not null) |
| `isApplicable`: raw `Optional.class` type key | `true` |
| `isApplicable`: non-Optional type key | `false` |

Commit: `test: add type resolver unit tests`

---

### 2.3 — Field resolver unit tests

**New file:** `cafe-beans/src/test/.../resolvers/FieldResolverTest.java`

#### `WireFieldResolver`

| Scenario | Expected |
|---|---|
| Field annotated `@CafeInject` | `supports(CafeInject.class)` → `true` |
| Field annotated `@CafeProperty` | `supports(CafeProperty.class)` → `false` (excluded) |
| `resolve` injects bean into field | field value equals resolved bean |
| `resolve` with `Optional<T>` field | field value is `Optional` |

#### `PropertyResolver`

| Scenario | Expected |
|---|---|
| `@CafeProperty`-annotated field | `supports(CafeProperty.class)` → `true` |
| `@CafeInject`-annotated field | `supports(CafeInject.class)` → `false` |
| Property key exists | field receives converted value |
| Property key missing | field remains `null` (Optional-like) |
| `String` target field | raw string value injected without conversion |
| `Integer` target field | value converted via `StringToIntegerConverter` |

Use real `CafePropertiesService` loaded from a test resource `application.properties`.

Commit: `test: add field resolver unit tests`

---

### 2.4 — Constructor resolver unit tests

**New file:** `cafe-beans/src/test/.../resolvers/ConstructorResolverTest.java`

The existing `BeanResolverConstructorsTests` tests the full DI pipeline. These new tests isolate
`SingletonConstructorResolver` and `PrototypeConstructorResolver` directly.

#### `SingletonConstructorResolver`

| Scenario | Expected |
|---|---|
| Bean not yet resolved → `isResolved` false | calls `super.resolve()`, persists instance |
| Bean already resolved → `isResolved` true | returns existing instance, no new allocation |
| `isApplicable` on singleton `CafeConstructor` | `true` |
| `isApplicable` on prototype `CafeConstructor` | `false` |

Use a `CafeBeansFactory` stub with controllable `isResolved` / `getOne` state.

#### `PrototypeConstructorResolver`

| Scenario | Expected |
|---|---|
| Called twice with same descriptor | returns two **different** instances |
| `isApplicable` on prototype descriptor | `true` |

Commit: `test: add constructor resolver unit tests`

---

### 2.5 — Method resolver unit tests

**New file:** `cafe-beans/src/test/.../resolvers/MethodResolverTest.java`

#### `SingletonHandlerMethodResolver`

| Scenario | Expected |
|---|---|
| Constructed with `CafeHandler.class` | `supports(CafeHandler.class)` → `true` |
| Constructed with `CafeHandler.class` | `supports(CafeProvider.class)` → `false` |
| Constructed with custom annotation | `supports(customAnnotation)` → `true` |
| `resolve` on `@CafeHandler`-annotated method | registers `HandlerTypeKey` in repository |

Use fixture: a simple `@CafeSingleton` class with one `@CafeHandler` void method.

#### `PrototypeWireMethodResolver` / `SingletonWireMethodResolver`

| Scenario | Expected |
|---|---|
| `@CafeProvider` method → `supports(CafeProvider.class)` | `true` |
| `@CafeHandler` method → `supports(CafeHandler.class)` | `false` (not a provider) |
| `resolve` on `@CafeProvider` method returning `String` | result stored in repository |

Commit: `test: add method resolver unit tests`

---

### 2.6 — CafePropertiesService edge cases

**Existing file:** `cafe-beans/src/test/.../properties/CafePropertiesServiceTests.java`

Current tests cover basic `.yml` and `.properties` loading. Add:

| Scenario | Setup | Expected |
|---|---|---|
| No `application.*` on classpath | empty test resources | no exception; empty properties |
| Nested YAML 3 levels deep | `a.b.c: value` | key `a.b.c` resolves to `"value"` |
| Numeric value in YAML | `port: 8080` | stored as string `"8080"` |
| Boolean value in YAML | `debug: true` | stored as string `"true"` |
| `.properties` file coexists with `.yml` | both on classpath | both loaded (no override, keys merged) |

Test resources: add `application-edge-cases.yml` used only in these test methods.

Commit: `test: add CafePropertiesService edge case tests`

---

### 2.7 — CafeHandlerFindService tests

**New file:** `cafe-beans/src/test/.../events/CafeHandlerFindServiceTest.java`

`CafeHandlerFindService.find()` was fixed in Phase 1 of 0.0.5 (was returning `Set.of()` — bug #1).
No direct unit tests exist.

| Scenario | Expected |
|---|---|
| Repository contains handler with matching annotation type | `find` returns signature set |
| Repository contains handler with different annotation type | `find` returns empty set |
| Repository empty | `find` returns empty set |
| Multiple handlers for same annotation | all returned |

Use a minimal `BeansRepository` stub seeded with `HandlerTypeKey` entries.

Commit: `test: add CafeHandlerFindService unit tests`

---

### 2.8 — CafeValidationService tests

**New file:** `cafe-beans/src/test/.../validation/CafeValidationServiceTest.java`

| Scenario | Expected |
|---|---|
| All validators pass | `validate()` returns empty list |
| One validator returns failure | `validate()` returns list with one `ValidationResult` |
| Two validators each return failure | `validate()` returns two results |
| Validator returns `Optional.empty()` | result not included |
| `CafeValidationResultFormatter.format(results)` called | non-empty string returned |

Use a stub `CafeValidator` that returns a controlled `Optional<ValidationResult>`.

Commit: `test: add CafeValidationService and formatter tests`

---

## Phase 3 — Structural Refactoring

### 3.1 — Exception hierarchy: introduce `CafeException` base

**Files to create:**
- `cafe-beans/src/main/java/.../exceptions/CafeException.java`

**Files to change:** all 14 existing exception classes

**Problem:** All 14 exception classes extend `RuntimeException` directly. Callers cannot write
a single `catch (CafeException e)` to handle any framework error — they must either catch
`RuntimeException` (too broad) or enumerate every concrete type (unmaintainable).

**New base class:**

```java
package org.taranix.cafe.beans.exceptions;

public abstract class CafeException extends RuntimeException {
    protected CafeException(String message) {
        super(message);
    }
    protected CafeException(String message, Throwable cause) {
        super(message, cause);
    }
}
```

**Change each exception to extend `CafeException`:**

```java
// before
public class CafeBeansFactoryException extends RuntimeException {
    public CafeBeansFactoryException(String message) { super(message); }
}

// after
public class CafeBeansFactoryException extends CafeException {
    public CafeBeansFactoryException(String message) { super(message); }
    public CafeBeansFactoryException(String message, Throwable cause) { super(message, cause); }
}
```

Additionally, add the two-argument constructor to exceptions that wrap lower-level failures:
`ReflectionUtilsException`, `CafePropertiesContextException`, `CafeClassMetadataException`.

All existing `throw new XxxException(message)` call sites remain unchanged — the new constructor
is additive only.

No change to `EventHub`'s `@SuppressWarnings("unchecked")` — it is unrelated.

Commit: `refactor: introduce CafeException base class, add cause constructors`

---

## Summary

| Phase | Items | Risk | Commits |
|---|---|---|---|
| 1 — Code review fixes | 1.1 Javadoc, 1.2 Converters, 1.3 Extension point doc | Low | 3 |
| 2 — Test coverage | 2.1 Converters, 2.2 Type resolvers, 2.3 Field resolvers, 2.4 Constructor resolvers, 2.5 Method resolvers, 2.6 Properties edge cases, 2.7 HandlerFindService, 2.8 ValidationService | Low | 8 |
| 3 — Structural refactoring | 3.1 Exception hierarchy | Low | 1 |

**Total: 12 commits. No behaviour changes — all modifications are additive or cosmetic.**

---

## Out of scope for 0.0.6

The following were considered but excluded:

- **HashMapRepository direct tests** — covered transitively by `BeansRepositoryTest` and all
  resolver integration tests; direct tests would duplicate without adding value.
- **ClassDependencyRegistry / MemberDependencyResolverRegistry direct tests** — behaviour already
  covered by `CafeCycleDetectionValidatorTest`; internal data structures are an implementation
  detail.
- **ClassScanner thread safety** — `ClassScanner` is used only during DI context initialization,
  which is single-threaded. No concurrent access is possible.
- **CafeBeansFactory lookup reduction** — no repeated same-type lookups were identified in the
  current code paths; caching is already in place at the `CafeClass` metadata level.
