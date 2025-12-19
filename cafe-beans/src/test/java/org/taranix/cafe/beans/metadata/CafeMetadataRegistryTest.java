package org.taranix.cafe.beans.metadata;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.taranix.cafe.beans.metadata.CafeBeansRegistryTestFixture.*;
import org.taranix.cafe.beans.repositories.typekeys.BeanTypeKey;

import java.util.Set;

class CafeMetadataRegistryTest {

    @Test
    @DisplayName("Should link List<Service> field dependency to Service constructor provider")
    void shouldMatchConstructorProviderForListField() {
        // Scenario: A class has a List<ServiceClass> field.
        // The registry should identify that this field depends on the constructor of ServiceClass.

        // given
        CafeMetadataRegistry registry = createRegistry(ListServiceClassInjectable.class, ServiceClass.class);

        // when
        // We look for the 'serviceClass' field which is of type List<ServiceClass>
        CafeMember field = registry.getClassMetadata(ListServiceClassInjectable.class)
                .getField("serviceClass");

        CafeMember serviceConstructor = registry.getClassMetadata(ServiceClass.class).getConstructor();

        CafeClass dependantClass = registry.getClassMetadata(ListServiceClassInjectable.class);
        CafeClass providerClass = registry.getClassMetadata(ServiceClass.class);

        // then
        Assertions.assertAll("Dependencies Check",
                () -> Assertions.assertNotNull(field, "Field 'serviceClass' should exist"),
                () -> Assertions.assertNotNull(serviceConstructor, "ServiceClass constructor should exist"),
                // Verify that the field's provider list contains the ServiceClass constructor
                () -> Assertions.assertTrue(
                        registry.getMemberDependencyRegistry().providers(field).contains(serviceConstructor),
                        "Field should depend on ServiceClass constructor"
                ),
                // Verify class-level dependency graph
                () -> Assertions.assertTrue(
                        registry.getClassDependencyRegistry().providers(dependantClass).contains(providerClass),
                        "Class graph should reflect dependency between Injectable and Service"
                )
        );
    }

    @Test
    @DisplayName("Should link Set<Service> field dependency to Service constructor provider")
    void shouldMatchConstructorProviderForSetField() {
        // Scenario: Similar to List, but checking Set<ServiceClass> injection.

        // given
        CafeMetadataRegistry registry = createRegistry(SetServiceClassInjectable.class, ServiceClass.class);

        // when
        CafeMember field = registry.getClassMetadata(SetServiceClassInjectable.class)
                .getField("serviceClass");

        CafeMember serviceConstructor = registry.getClassMetadata(ServiceClass.class).getConstructor();

        CafeClass dependantClass = registry.getClassMetadata(SetServiceClassInjectable.class);
        CafeClass providerClass = registry.getClassMetadata(ServiceClass.class);

        // then
        Assertions.assertAll("Set Dependency Check",
                () -> Assertions.assertNotNull(field),
                () -> Assertions.assertTrue(
                        registry.getMemberDependencyRegistry().providers(field).contains(serviceConstructor),
                        "Field Set<ServiceClass> should resolve to ServiceClass constructor"
                ),
                () -> Assertions.assertTrue(
                        registry.getClassDependencyRegistry().providers(dependantClass).contains(providerClass)
                )
        );
    }

    @Test
    @DisplayName("Should resolve Generic Type (T) from superclass context")
    void shouldFindStringProviderForGenericSuperClass() {
        // Scenario: A class extends a Generic class <String, Integer>.
        // The field 'unknown' (type T) in parent should be resolved as String in the child context.

        // given
        CafeMetadataRegistry registry = createRegistry(IntegerProviderAndStringInjectable.class, StringProvider.class);

        // when
        // The field 'unknown' comes from the generic parent class
        CafeField stringField = registry.getClassMetadata(IntegerProviderAndStringInjectable.class)
                .getField("unknown");

        // We explicitly check if there is a provider for String.class for this field
        Set<CafeMember> stringProviders = registry.getMemberDependencyRegistry()
                .providers(stringField, BeanTypeKey.from(String.class));

        // then
        Assertions.assertNotNull(stringField, "Inherited field 'unknown' should be found");
        Assertions.assertEquals(1, stringProviders.size(), "Should find exactly one provider for String (from StringProvider class)");
    }

    @Test
    @DisplayName("Should link field dependency to a @CafeProvider method")
    void shouldMatchMethodProviderForField() {
        // Scenario: A field requires ServiceClass. A separate class provides ServiceClass via a @CafeProvider method.
        // The dependency should point to the method, not a constructor.

        // given
        CafeMetadataRegistry registry = createRegistry(ServiceClassInjectable.class, ServiceClassProvider.class);

        // when
        CafeMember field = registry.getClassMetadata(ServiceClassInjectable.class)
                .getField("serviceClass");

        // We assume ServiceClassProvider has a method providing the service
        CafeMember providerMethod = registry.getClassMetadata(ServiceClassProvider.class)
                .getMethods().stream()
                .findFirst()
                .orElse(null);

        // then
        Assertions.assertNotNull(field);
        Assertions.assertNotNull(providerMethod);
        Assertions.assertTrue(
                registry.getMemberDependencyRegistry().providers(field).contains(providerMethod),
                "Field should be resolved by the @CafeProvider method"
        );
    }

    @Test
    @DisplayName("Should include owner class constructor as dependency for injected field")
    void shouldFindEmptyProvidersForDependantField() {
        // Scenario: Validates lifecycle dependencies. An injected field implicitly depends on
        // the constructor of the class it belongs to (the instance must exist to inject the field).

        // given
        // Note: We only register the injectable class, no external providers.
        CafeMetadataRegistry registry = createRegistry(ServiceClassInjectable.class);

        // when
        CafeMember field = registry.getClassMetadata(ServiceClassInjectable.class)
                .getField("serviceClass");

        CafeMember constructor = registry.getClassMetadata(ServiceClassInjectable.class)
                .getConstructor();

        // then
        Assertions.assertNotNull(field);

        // 1. Check if metadata knows it depends on its own class (lifecycle dependency)
        Assertions.assertTrue(field.hasDependencies(BeanTypeKey.from(ServiceClassInjectable.class)),
                "Field must depend on its owner class instantiation");

        // 2. Check if metadata knows it depends on the target type (ServiceClass)
        Assertions.assertTrue(field.hasDependencies(BeanTypeKey.from(ServiceClass.class)),
                "Field must depend on the type being injected");

        // 3. Verify dependency registry links the field to the class constructor
        Assertions.assertTrue(
                registry.getMemberDependencyRegistry().providers(field).contains(constructor),
                "Dependency registry should link field to owner constructor"
        );
    }

    @Test
    @DisplayName("Should link simple field dependency to Service constructor")
    void shouldMatchConstructorProviderForField() {
        // Scenario: Simple Injection. Field ServiceClass -> ServiceClass Constructor.

        // given
        CafeMetadataRegistry registry = createRegistry(ServiceClassInjectable.class, ServiceClass.class);

        // when
        CafeMember field = registry.getClassMetadata(ServiceClassInjectable.class)
                .getField("serviceClass");

        CafeMember constructor = registry.getClassMetadata(ServiceClass.class).getConstructor();

        // then
        Assertions.assertTrue(
                registry.getMemberDependencyRegistry().providers(field).contains(constructor),
                "Field should resolve directly to ServiceClass constructor"
        );
    }

    @Test
    @DisplayName("Should link Array[] field dependency to Service constructor provider")
    void shouldMatchConstructorProviderForArrayField() {
        // Scenario: A field is an Array (ServiceClass[]).
        // The registry should handle array types and link them to the provider of the component type.

        // given
        CafeMetadataRegistry registry = createRegistry(ArrayServiceClassInjectable.class, ServiceClass.class);

        // when
        CafeMember field = registry.getClassMetadata(ArrayServiceClassInjectable.class)
                .getField("serviceClass");

        CafeMember constructor = registry.getClassMetadata(ServiceClass.class).getConstructor();

        CafeClass dependant = registry.getClassMetadata(ArrayServiceClassInjectable.class);
        CafeClass provider = registry.getClassMetadata(ServiceClass.class);

        // then
        Assertions.assertAll("Array Dependency Check",
                () -> Assertions.assertNotNull(field),
                () -> Assertions.assertNotNull(constructor),
                () -> Assertions.assertTrue(
                        registry.getMemberDependencyRegistry().providers(field).contains(constructor),
                        "Array field should resolve to component type constructor"
                ),
                () -> Assertions.assertTrue(
                        registry.getClassDependencyRegistry().providers(dependant).contains(provider)
                )
        );
    }

    // --- Helper Methods ---

    private CafeMetadataRegistry createRegistry(Class<?>... classes) {
        return CafeMetadataRegistry.builder()
                .withClasses(Set.of(classes))
                .build();
    }
}