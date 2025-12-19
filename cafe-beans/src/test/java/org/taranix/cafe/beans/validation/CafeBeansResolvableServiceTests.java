package org.taranix.cafe.beans.validation;

import org.apache.commons.lang3.NotImplementedException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.taranix.cafe.beans.annotations.classes.CafeService;
import org.taranix.cafe.beans.annotations.fields.CafeInject;
import org.taranix.cafe.beans.annotations.methods.CafeProvider;
import org.taranix.cafe.beans.annotations.modifiers.CafeOptional;
import org.taranix.cafe.beans.exceptions.BeanTypeKeyException;
import org.taranix.cafe.beans.metadata.CafeConstructor;
import org.taranix.cafe.beans.metadata.CafeField;
import org.taranix.cafe.beans.metadata.CafeMetadataRegistry;
import org.taranix.cafe.beans.metadata.CafeMethod;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;

class CafeBeansResolvableServiceTests {
    @Test
    @DisplayName("(Positive) Should pass when class has no dependencies.")
    void shouldClassWithoutDependenciesBeResolvable() {
        // given
        CafeMetadataRegistry cafeMetadataRegistry = CafeMetadataRegistry.builder()
                .withClass(SubjectClassProvider.class)
                .build();
        CafeResolvableBeansValidator validator = new CafeResolvableBeansValidator();
        // when
        Optional<ValidationResult> result = validator.validate(cafeMetadataRegistry);

        // then
        Assertions.assertTrue(result.isEmpty(), "Expected successful validation.");
    }

    @Test
    @DisplayName("(Positive) Should pass when class dependencies are resolvable.")
    void shouldClassWithDependenciesBeResolvable() {
        // given
        CafeMetadataRegistry cafeMetadataRegistry = CafeMetadataRegistry.builder()
                .withClass(SubjectClassProvider.class)
                .withClass(SubjectClassInjectable.class)
                .build();
        CafeResolvableBeansValidator validator = new CafeResolvableBeansValidator();

        // when
        Optional<ValidationResult> result = validator.validate(cafeMetadataRegistry);

        // then
        Assertions.assertTrue(result.isEmpty(), "Expected successful validation when dependencies are met.");
    }

    @Test
    @DisplayName("(Negative) Should fail when field dependency is unresolvable.")
    void shouldClassWithoutDependenciesNotBeResolvable() {
        // given
        CafeMetadataRegistry cafeMetadataRegistry = CafeMetadataRegistry.builder()
                .withClass(ServiceClassInjectable.class)
                .build();
        CafeResolvableBeansValidator validator = new CafeResolvableBeansValidator();

        // when
        Optional<ValidationResult> result = validator.validate(cafeMetadataRegistry);

        // Extracting the unresolvable member from the result
        CafeField nonResolvableField = result
                .map(ValidationResult::objects)
                .flatMap(objects -> objects.stream()
                        .filter(CafeField.class::isInstance)
                        .findFirst())
                .map(CafeField.class::cast)
                .orElse(null);

        // then
        Assertions.assertTrue(result.isPresent(), "Expected validation failure due to missing ServiceClass provider.");
        Assertions.assertNotNull(nonResolvableField, "Expected the unresolvable member to be a field.");
        Assertions.assertEquals("serviceClass", nonResolvableField.getField().getName());
    }

    @Test
    @DisplayName("(Negative) Should fail when multiple members (Constructor and Method) lack providers.")
    void shouldDependantSubjectClassBeResolvableAndProviderSubjectClassWithDependencyNot() {

        // given
        CafeMetadataRegistry cafeMetadataRegistry = CafeMetadataRegistry.builder()
                .withClass(SubjectClassInjectable.class)
                .withClass(SubjectClassProviderWCADate.class)
                .build();

        CafeResolvableBeansValidator validator = new CafeResolvableBeansValidator();

        // when
        Optional<ValidationResult> result = validator.validate(cafeMetadataRegistry);

        // then
        Assertions.assertTrue(result.isPresent(), "Expected validation failure due to missing Date and String providers.");

        // Extracting members of specific types
        Set<Object> objects = result.get().objects();

        CafeMethod nonResolvableMethod = objects.stream()
                .filter(CafeMethod.class::isInstance)
                .map(CafeMethod.class::cast)
                .findFirst()
                .orElse(null);

        CafeConstructor nonResolvableConstructor = objects.stream()
                .filter(CafeConstructor.class::isInstance)
                .map(CafeConstructor.class::cast)
                .findFirst()
                .orElse(null);


        Assertions.assertNotNull(nonResolvableMethod, "Expected the unresolvable member to include a Method.");
        Assertions.assertNotNull(nonResolvableConstructor, "Expected the unresolvable member to include a Constructor.");
    }

    @Test
    @DisplayName("(Positive) Should pass when a required dependency is marked as @CafeOptional.")
    void shouldPassWhenDependencyIsOptional() {
        // given
        CafeMetadataRegistry cafeMetadataRegistry = CafeMetadataRegistry.builder()
                .withClass(OptionalServiceInjectable.class)
                .build();
        CafeResolvableBeansValidator validator = new CafeResolvableBeansValidator();

        // when
        Optional<ValidationResult> result = validator.validate(cafeMetadataRegistry);

        // then
        Assertions.assertTrue(result.isEmpty(), "Expected successful validation as dependency is optional.");
    }

    @Test
    @DisplayName("(Negative) Should fail when a non-optional constructor dependency is unresolvable.")
    void shouldFailOnUnresolvableConstructorDependency() {
        // given
        CafeMetadataRegistry cafeMetadataRegistry = CafeMetadataRegistry.builder()
                .withClass(ConstructorInjectableMissingDependency.class)
                .build();
        CafeResolvableBeansValidator validator = new CafeResolvableBeansValidator();

        // when
        Optional<ValidationResult> result = validator.validate(cafeMetadataRegistry);

        // then
        Assertions.assertTrue(result.isPresent(), "Expected validation failure due to missing Date provider for constructor.");
        // Verify the unresolvable member is indeed a Constructor
        Assertions.assertTrue(result.get().objects().stream().anyMatch(CafeConstructor.class::isInstance),
                "Expected the unresolvable member to be the constructor.");
    }

    @Test
    @DisplayName("(Negative) Should fail when a method dependency is unresolvable.")
    void shouldFailOnUnresolvableMethodDependency() {
        // given
        CafeMetadataRegistry cafeMetadataRegistry = CafeMetadataRegistry.builder()
                .withClass(MethodInjectableMissingDependency.class)
                .build();
        CafeResolvableBeansValidator validator = new CafeResolvableBeansValidator();

        // when
        Optional<ValidationResult> result = validator.validate(cafeMetadataRegistry);

        // then
        Assertions.assertTrue(result.isPresent(), "Expected validation failure due to missing Date provider for method.");
        // Verify the unresolvable member is indeed a Method
        Assertions.assertTrue(result.get().objects().stream().anyMatch(CafeMethod.class::isInstance),
                "Expected the unresolvable member to be the method.");
    }

    @Test
    @DisplayName("(Positive) Should pass when required generic type (List<String>) is exactly matched.")
    void shouldPassOnExactGenericMatch() {
        // given
        CafeMetadataRegistry registry = CafeMetadataRegistry.builder()
                .withClass(StringListProvider.class)
                .withClass(StringListConsumer.class)
                .build();
        CafeResolvableBeansValidator validator = new CafeResolvableBeansValidator();

        // when
        Optional<ValidationResult> result = validator.validate(registry);

        // then
        Assertions.assertTrue(result.isEmpty(), "Expected successful validation due to exact generic match.");
    }

    @Test
    @DisplayName("(Negative) Should fail when required generic argument (List<String>) does not match provider (List<Integer>).")
    void shouldFailOnGenericTypeMismatch() {
        // given
        CafeMetadataRegistry registry = CafeMetadataRegistry.builder()
                .withClass(IntegerListProvider.class)
                .withClass(StringListConsumer.class)
                .build();
        CafeResolvableBeansValidator validator = new CafeResolvableBeansValidator();

        // when
        Optional<ValidationResult> result = validator.validate(registry);

        // then
        Assertions.assertTrue(result.isPresent(), "Expected validation failure due to List<Integer> not resolving List<String>.");
    }

    @Test
    @DisplayName("(Negative) Should FAIL during registry build because raw List type dependencies are unsupported.")
    void shouldFailRegistryBuildOnRawTypeConsumer() {
        // given / when / then
        Assertions.assertThrows(BeanTypeKeyException.class, () -> {
            CafeMetadataRegistry.builder()
                    .withClass(StringListProvider.class)
                    .withClass(RawListConsumer.class)
                    .build();
        }, "Registration of a class with a raw collection type should throw a BeanTypeKeyException..");
    }

    @Test
    @DisplayName("(Positive) Should pass when generic type is resolved to String in Constructor.")
    void shouldPassWhenGenericTypeResolvedToConstructor() {
        // given
        CafeMetadataRegistry registry = CafeMetadataRegistry.builder()
                .withClass(StringValueProvider.class)
                .withClass(ValueConsumerConstructor.class)
                .build();
        CafeResolvableBeansValidator validator = new CafeResolvableBeansValidator();

        // when
        Optional<ValidationResult> result = validator.validate(registry);

        // then
        Assertions.assertTrue(result.isEmpty(), "Expected successful resolution of generic type String in constructor.");
    }

    @Test
    @DisplayName("(Positive) Should pass when generic type is resolved to Integer in Field.")
    void shouldPassWhenGenericTypeResolvedToField() {
        // given
        CafeMetadataRegistry registry = CafeMetadataRegistry.builder()
                .withClass(IntegerValueProvider.class)
                .withClass(ValueConsumerField.class)
                .build();
        CafeResolvableBeansValidator validator = new CafeResolvableBeansValidator();

        // when
        Optional<ValidationResult> result = validator.validate(registry);

        // then
        Assertions.assertTrue(result.isEmpty(), "Expected successful resolution of generic type Integer in field.");
    }

    @Test
    @DisplayName("(Negative) Should fail when required generic type (Boolean) is not provided (String provided).")
    void shouldFailOnGenericTypeMismatchOnInterface() {
        // given
        CafeMetadataRegistry registry = CafeMetadataRegistry.builder()
                .withClass(StringValueProvider.class)
                .withClass(BooleanValueConsumer.class)
                .build();
        CafeResolvableBeansValidator validator = new CafeResolvableBeansValidator();

        // when
        Optional<ValidationResult> result = validator.validate(registry);

        // then
        Assertions.assertTrue(result.isPresent(), "Expected validation failure: String cannot resolve required Boolean.");
    }

    interface ValueProvider<T> {
        T getValue();
    }

    // --- Test Classes ---

    static class SubjectClass {

    }

    @CafeService
    static class ValueConsumerField {
        @CafeInject
        Integer value;
    }

    // Provider String
    @CafeService
    static class StringValueProvider implements ValueProvider<String> {
        @Override
        @CafeProvider
        public String getValue() {
            throw new NotImplementedException();
        }
    }


    @CafeService
    static class BooleanValueConsumer {
        @CafeInject
        Boolean value;
    }

    @CafeService
    static class ValueConsumerConstructor {
        ValueConsumerConstructor(String value) {
        }
    }

    // Provider Integer
    @CafeService
    static class IntegerValueProvider implements ValueProvider<Integer> {
        @Override
        @CafeProvider
        public Integer getValue() {
            throw new NotImplementedException();
        }
    }

    @CafeService
    static class StringListProvider {
        @CafeProvider
        public List<String> getStringList() {
            throw new NotImplementedException();
        }
    }

    @CafeService
    static class IntegerListProvider {
        @CafeProvider
        public List<Integer> getIntegerList() {
            throw new NotImplementedException();
        }
    }

    @CafeService
    static class StringListConsumer {
        @CafeInject
        List<String> requiredList; // Requires List<String>
    }

    @CafeService
    static class RawListConsumer {
        @CafeInject
        @SuppressWarnings("rawtypes")
        List requiredList; // Requires raw List
    }

    // --- Test Classes ---

    @CafeService
    static class SubjectClassProvider {
        @CafeProvider
        private SubjectClass producer() {
            throw new NotImplementedException();
        }

    }

    @CafeService
    static class SubjectClassInjectable {
        @CafeInject
        private SubjectClass testClass2;
    }

    @CafeService
    static class ServiceClassInjectable {
        @CafeInject
        ServiceClass serviceClass; // Requires ServiceClass (missing provider)
    }

    @CafeService
    static class ServiceClass {
    }

    @CafeService
    static class OptionalServiceInjectable {
        @CafeInject
        @CafeOptional
        ServiceClass serviceClass; // Requires ServiceClass, but is optional
    }

    static class SubjectClassProviderWCADate {
        // Constructor requires Date (missing provider)
        SubjectClassProviderWCADate(Date input) {

        }

        @CafeProvider
        private SubjectClass producer(String input) { // Method requires String (missing provider)
            throw new NotImplementedException();
        }

    }

    @CafeService
    static class ConstructorInjectableMissingDependency {
        // Constructor requires Date (missing provider)
        ConstructorInjectableMissingDependency(Date missingDate) {
        }
    }

    @CafeService
    static class MethodInjectableMissingDependency {
        // Method requires Date (missing provider)
        @CafeProvider
        public String create(Date missingDate) {
            throw new NotImplementedException();
        }
    }
}