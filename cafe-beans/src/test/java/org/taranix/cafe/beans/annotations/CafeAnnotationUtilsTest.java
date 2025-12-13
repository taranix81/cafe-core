package org.taranix.cafe.beans.annotations;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.taranix.cafe.beans.annotations.modifiers.CafeModifier;
import org.taranix.cafe.beans.annotations.modifiers.CafeName;
import org.taranix.cafe.beans.annotations.modifiers.CafePrimary;
import org.taranix.cafe.beans.annotations.types.CafeType;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the CafeAnnotationUtils class.
 * Focuses on verifying logic for annotation presence, scope resolution,
 * member naming, hierarchy scanning, and meta-annotation (marker) detection.
 */
class CafeAnnotationUtilsTest {


    // Marker that is meta-annotated by CafeModifier for meta-annotation testing
    @Retention(RetentionPolicy.RUNTIME)
    @CafeModifier
    @interface CustomModifierMarker {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @CafeType
    @interface CustomTypeMarker {
    }

    // Annotation to test isAnnotationExtend() recursively
    @Retention(RetentionPolicy.RUNTIME)
    @CustomModifierMarker
    @interface DoubleMetaAnnotation {
    }

    @CustomModifierMarker
    static class CustomClassWithCustomModifier {

    }

    // --- Test Classes for Reflection ---

    @CafeService(scope = Scope.Prototype)
    static class PrototypeService {
    }

    @CafeService
    static class SingletonService {
    }

    static class DefaultService {
    }

    static class MemberBean {

        @CafeName("fieldCustomName")
        String fieldWithCafeName;

        @CustomTypeMarker
        String fieldWithTypeMarker;

        String unannotatedField;

        // Constructor not explicitly annotated, scope determined by class
        public MemberBean() {
        }

        @DoubleMetaAnnotation
        public MemberBean(String param) {
        }

        @CafeProvider
        @CafeName("methodCustomName")
        Object singletonProviderMethod() {
            return null;
        }

        @CafeProvider
        @CustomModifierMarker
        Object modifiedProviderMethod() {
            return null;
        }

        void unannotatedMethod() {
        }
    }

    static class BaseClass {
        @CustomModifierMarker
        public void baseMethod() {
        }
    }

    static class SubClass extends BaseClass {
        @CustomModifierMarker
        @Override
        public void baseMethod() {
        } // Should be found twice if not careful, but included in the list

        @CustomModifierMarker
        public void subMethod() {
        }
    }

    // --- Test Methods ---

    @Nested
    class PresenceAndRetrievalTests {
        @Test
        void isAnnotationPresent_ShouldReturnTrueForPresentAnnotation() {
            assertTrue(CafeAnnotationUtils.isAnnotationPresent(SingletonService.class, CafeService.class));
        }

        @Test
        void isAnnotationPresent_ShouldReturnFalseForMissingAnnotation() {
            assertFalse(CafeAnnotationUtils.isAnnotationPresent(DefaultService.class, CafeService.class));
        }

        @Test
        void getAnnotations_ShouldRetrieveAllClassAnnotations() {
            Set<Annotation> annotations = CafeAnnotationUtils.getAnnotations(SingletonService.class);
            assertTrue(annotations.stream().anyMatch(a -> a instanceof CafeService));
            assertFalse(annotations.isEmpty());
        }

        @Test
        void getAnnotationByType_ShouldRetrieveSpecificAnnotation() {
            CafeService annotation = CafeAnnotationUtils.getAnnotationByType(SingletonService.class, CafeService.class);
            assertNotNull(annotation);
            assertEquals(Scope.Singleton, annotation.scope());
        }
    }

    @Nested
    class ScopeResolutionTests {
        private Method singletonMethod;
        private Constructor<?> defaultConstructor;

        @BeforeEach
        void setUp() throws NoSuchMethodException {
            singletonMethod = MemberBean.class.getDeclaredMethod("singletonProviderMethod");
            defaultConstructor = MemberBean.class.getConstructor();
        }

        // Class Scope Tests
        @Test
        void getScope_Class_ShouldReturnPrototype() {
            assertEquals(Scope.Prototype, CafeAnnotationUtils.getScope(PrototypeService.class));
            assertFalse(CafeAnnotationUtils.isSingleton(PrototypeService.class));
        }

        @Test
        void getScope_Class_ShouldReturnSingletonByDefault() {
            assertEquals(Scope.Singleton, CafeAnnotationUtils.getScope(DefaultService.class));
            assertTrue(CafeAnnotationUtils.isSingleton(DefaultService.class));
        }

        // Member Scope Tests

        @Test
        void getScope_Member_ShouldReturnSingletonForMethod() {
            assertEquals(Scope.Singleton, CafeAnnotationUtils.getScope(singletonMethod));
            assertTrue(CafeAnnotationUtils.isSingleton(singletonMethod));
        }

        @Test
        void getScope_Member_ShouldReturnSingletonForConstructorByDefault() {
            // MemberBean class is a default service (Singleton)
            assertEquals(Scope.Singleton, CafeAnnotationUtils.getScope(defaultConstructor));
            assertTrue(CafeAnnotationUtils.isSingleton(defaultConstructor));
        }

        @Test
        void getScope_Member_ShouldReturnSingletonForFieldByDefault() throws NoSuchFieldException {
            Field field = MemberBean.class.getDeclaredField("unannotatedField");
            assertEquals(Scope.Singleton, CafeAnnotationUtils.getScope(field));
            assertTrue(CafeAnnotationUtils.isSingleton(field));
        }
    }

    @Nested
    class NamingTests {
        @Test
        void getMemberName_ShouldReturnCustomNameForField() throws NoSuchFieldException {
            Field field = MemberBean.class.getDeclaredField("fieldWithCafeName");
            assertEquals("fieldCustomName", CafeAnnotationUtils.getMemberName(field));
        }

        @Test
        void getMemberName_ShouldReturnCustomNameForMethod() throws NoSuchMethodException {
            Method method = MemberBean.class.getDeclaredMethod("singletonProviderMethod");
            assertEquals("methodCustomName", CafeAnnotationUtils.getMemberName(method));
        }

        @Test
        void getMemberName_ShouldReturnEmptyStringForUnannotatedMember() throws NoSuchFieldException {
            Field field = MemberBean.class.getDeclaredField("unannotatedField");
            assertEquals(StringUtils.EMPTY, CafeAnnotationUtils.getMemberName(field));
        }
    }

    @Nested
    class HierarchyScanningTests {
        @Test
        void getMethodsAnnotatedBy_ShouldFindMethodsInBaseAndSubclass() {
            // Looking for @CustomModifierMarker
            List<Method> methods = CafeAnnotationUtils.getClassMethodsAnnotatedBy(SubClass.class, CustomModifierMarker.class);
            Set<String> methodNames = methods.stream().map(Method::getName).collect(Collectors.toSet());

            assertEquals(2, methods.size()); // baseMethod (from Base) and subMethod (from Sub)
            assertTrue(methodNames.contains("baseMethod"));
            assertTrue(methodNames.contains("subMethod"));
        }

        @Test
        void getMethodsAnnotatedBy_ShouldReturnEmptyListWhenNotFound() {
            List<Method> methods = CafeAnnotationUtils.getClassMethodsAnnotatedBy(SubClass.class, CafePrimary.class);
            assertTrue(methods.isEmpty());
        }
    }

    @Nested
    class MetaAnnotationTests {

        @Test
        void isAnnotationExtend_ShouldReturnTrueForDirectMarker() {
            // Check if CustomModifierMarker is extended by CafeModifier
            Annotation customMarker = CustomClassWithCustomModifier.class.getAnnotation(CustomModifierMarker.class);
            assertTrue(CafeAnnotationUtils.isAnnotationMarkedBy(customMarker, CafeModifier.class));
        }

        @Test
        void isAnnotationExtend_ShouldReturnTrueForDoubleMetaAnnotation() throws NoSuchMethodException {
            // Check if DoubleMetaAnnotation is extended by CafeModifier (via CustomModifierMarker)
            Method method = MemberBean.class.getDeclaredMethod("modifiedProviderMethod");
            Annotation doubleMeta = method.getAnnotation(CustomModifierMarker.class);
            assertTrue(CafeAnnotationUtils.isAnnotationMarkedBy(doubleMeta, CafeModifier.class));
        }

        @Test
        void isAnnotationExtend_ShouldReturnFalseForUnrelatedAnnotation() throws NoSuchFieldException {
            // Check if CafeName is extended by CafeType (should be false)
            Field field = MemberBean.class.getDeclaredField("fieldWithCafeName");
            Annotation cafeName = field.getAnnotation(CafeName.class);
            assertFalse(CafeAnnotationUtils.isAnnotationMarkedBy(cafeName, CafeType.class));
        }

        @Test
        void hasMarker_ShouldReturnTrueForFieldMarker() throws NoSuchFieldException {
            Field field = MemberBean.class.getDeclaredField("fieldWithTypeMarker");
            assertTrue(CafeAnnotationUtils.hasMarker(field, CafeType.class));
        }

        @Test
        void hasMarker_ShouldReturnTrueForExecutableMarker() throws NoSuchMethodException {
            Method method = MemberBean.class.getDeclaredMethod("modifiedProviderMethod");
            assertTrue(CafeAnnotationUtils.hasMarker(method, CafeModifier.class));
        }

        @Test
        void hasMarker_ShouldReturnFalseWhenMarkerIsMissing() throws NoSuchMethodException {
            Method method = MemberBean.class.getDeclaredMethod("unannotatedMethod");
            assertFalse(CafeAnnotationUtils.hasMarker(method, CafeType.class));
        }

    }
}