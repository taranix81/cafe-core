package org.taranix.cafe.beans.validation;

import org.apache.commons.lang3.NotImplementedException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.taranix.cafe.beans.annotations.CafeFactory;
import org.taranix.cafe.beans.annotations.CafeProvider;
import org.taranix.cafe.beans.metadata.CafeBeansDefinitionRegistry;
import org.taranix.cafe.beans.metadata.CafeClassInfo;
import org.taranix.cafe.beans.metadata.members.CafeMemberInfo;

import java.util.Date;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

class CafeCycleDetectionValidatorTest {


    @Test
    @DisplayName("Should find a dependency cycle within a single class using provider methods.")
    void shouldFindCycleInsideOneClass() {
        // given
        CafeBeansDefinitionRegistry cafeBeansDefinitionRegistry = CafeBeansDefinitionRegistry.builder()
                .withClass(ProvidersWithInternalCycle.class)
                .build();
        CafeCycleDetectionValidator validator = new CafeCycleDetectionValidator();

        // when
        Optional<ValidationResult> result = validator.validate(cafeBeansDefinitionRegistry);

        // then
        Assertions.assertTrue(result.isPresent(), "Expected a validation result due to cycle.");
        // Check if all provider methods (2 methods) are part of the cycle objects
        Set<CafeMemberInfo> expectedMembers = cafeBeansDefinitionRegistry.allMembers().stream()
                .filter(CafeMemberInfo::isMethod)
                .collect(Collectors.toSet());
        Assertions.assertTrue(result.get().objects().containsAll(expectedMembers), "All methods should be part of the detected cycle.");
    }

    @Test
    @DisplayName("Should find a dependency cycle spread across multiple classes.")
    void shouldFindCycleAmongManyClasses() {
        // given
        CafeBeansDefinitionRegistry cafeBeansDefinitionRegistry = CafeBeansDefinitionRegistry.builder()
                .withClass(StringProviderWithDateParameterService.class)
                .withClass(IntegerProviderWithStringParameter.class)
                .withClass(DateProviderWithIntegerParameterService.class)
                .build();
        CafeCycleDetectionValidator validator = new CafeCycleDetectionValidator();

        // when
        Optional<ValidationResult> result = validator.validate(cafeBeansDefinitionRegistry);
        Set<Object> objectsWithCycle = result.map(ValidationResult::objects).orElse(Set.of());

        // Filter members and classes from the result set
        Set<CafeClassInfo> classesInCycle = objectsWithCycle.stream().filter(CafeClassInfo.class::isInstance).map(CafeClassInfo.class::cast).collect(Collectors.toSet());
        Set<CafeMemberInfo> membersInCycle = objectsWithCycle.stream().filter(CafeMemberInfo.class::isInstance).map(CafeMemberInfo.class::cast).collect(Collectors.toSet());

        // then
        Assertions.assertTrue(result.isPresent(), "Expected a validation result due to cycle.");
        // Expecting 3 provider methods involved in the cycle
        Assertions.assertEquals(3, membersInCycle.size(), "Expected 3 members (provider methods) in the cycle.");
        // Expecting 3 classes involved in the cycle (String, Date, Integer providers)
        Assertions.assertEquals(3, classesInCycle.size(), "Expected 3 classes involved in the cycle.");
    }

    @Test
    @DisplayName("Should NOT find a cycle when dependencies are acyclic (positive scenario).")
    void shouldNotFindCycleWhenNoCycleExists() {
        // given
        CafeBeansDefinitionRegistry cafeBeansDefinitionRegistry = CafeBeansDefinitionRegistry.builder()
                .withClass(ValidProvider.class)
                // Assuming IntegerProviderWithStringParameter does not close a cycle here
                .build();
        CafeCycleDetectionValidator validator = new CafeCycleDetectionValidator();

        // when
        Optional<ValidationResult> result = validator.validate(cafeBeansDefinitionRegistry);

        // then
        Assertions.assertFalse(result.isPresent(), "Expected no validation result (no cycle found).");
    }

    @Test
    @DisplayName("Should find a cycle involving component constructors.")
    void shouldFindCycleInvolvingConstructors() {
        // given
        CafeBeansDefinitionRegistry cafeBeansDefinitionRegistry = CafeBeansDefinitionRegistry.builder()
                .withClass(ConstructorCycleA.class)
                .withClass(ConstructorCycleB.class)
                .build();
        CafeCycleDetectionValidator validator = new CafeCycleDetectionValidator();

        // when
        Optional<ValidationResult> result = validator.validate(cafeBeansDefinitionRegistry);
        Set<CafeMemberInfo> membersInCycle = result.map(ValidationResult::objects).orElse(Set.of()).stream()
                .filter(CafeMemberInfo.class::isInstance)
                .map(CafeMemberInfo.class::cast)
                .collect(Collectors.toSet());

        // then
        Assertions.assertTrue(result.isPresent(), "Expected a validation result due to constructor cycle.");
        // Expecting two constructors involved in the cycle
        Assertions.assertEquals(2, membersInCycle.size(), "Expected two constructors to be found in the cycle.");
        Assertions.assertTrue(membersInCycle.stream().allMatch(CafeMemberInfo::isConstructor), "All members in the cycle should be constructors.");
    }

    @Test
    @DisplayName("Should NOT find a cycle when dependencies are linear (A -> B -> C).")
    void shouldNotFindCycleWhenOnlyLinearDependenciesExist() {
        // given
        CafeBeansDefinitionRegistry cafeBeansDefinitionRegistry = CafeBeansDefinitionRegistry.builder()
                .withClass(DependencyC.class)
                .withClass(DependencyB.class)
                .withClass(DependencyA.class)
                .build();
        CafeCycleDetectionValidator validator = new CafeCycleDetectionValidator();

        // when
        Optional<ValidationResult> result = validator.validate(cafeBeansDefinitionRegistry);

        // then
        Assertions.assertFalse(result.isPresent(), "Expected no validation result for linear dependencies.");
    }

    // --- Test Classes ---

    @CafeFactory
    static class ValidProvider {
        @CafeProvider
        public Boolean provide(Date date) {
            throw new NotImplementedException();
        }
    }

    @CafeFactory
    static class ConstructorCycleA {
        // A requires B
        public ConstructorCycleA(ConstructorCycleB b) {
        }
    }

    @CafeFactory
    static class ConstructorCycleB {
        // B requires A
        public ConstructorCycleB(ConstructorCycleA a) {
        }
    }

    @CafeFactory
    static class DependencyA {
        // Requires String
        public DependencyA(String s) {
        }

        @CafeProvider
        public String provideString() {
            throw new NotImplementedException();
        }
    }

    @CafeFactory
    static class DependencyB {
        // Requires Date
        public DependencyB(Date d) {
        }

        @CafeProvider
        public Date provideDate(String s) {
            throw new NotImplementedException();
        } // Requires String
    }

    @CafeFactory
    static class DependencyC {
        // Requires Integer
        public DependencyC(Integer i) {
        }

        @CafeProvider
        public Integer provideInteger(Date d) {
            throw new NotImplementedException();
        } // Requires Date
    }

    @CafeFactory
    static class DateProviderWithIntegerParameterService {
        @CafeProvider
        public Date provide(Integer in) {
            throw new NotImplementedException();
        }
    }

    @CafeFactory
    static class StringProviderWithDateParameterService {
        @CafeProvider
        public String provide(Date in) {
            throw new NotImplementedException();
        }
    }

    static class ProvidersWithInternalCycle {
        @CafeProvider
        public String getString(Integer integer) {
            throw new NotImplementedException();
        }

        @CafeProvider
        public Integer getInteger(String string) {
            throw new NotImplementedException();
        }
    }

    static class IntegerProviderWithStringParameter {
        @CafeProvider
        public Integer provide(String s) {
            throw new NotImplementedException();
        }
    }

}