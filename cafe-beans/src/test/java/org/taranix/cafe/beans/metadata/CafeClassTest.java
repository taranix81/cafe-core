package org.taranix.cafe.beans.metadata;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.taranix.cafe.beans.annotations.methods.CafeProvider;
import org.taranix.cafe.beans.annotations.modifiers.CafeName;
import org.taranix.cafe.beans.annotations.modifiers.CafeOptional;
import org.taranix.cafe.beans.annotations.modifiers.CafePrimary;
import org.taranix.cafe.beans.exceptions.CafeClassMetadataException;
import org.taranix.cafe.beans.metadata.CafeClassMetadataTestFixtures.StaticProvider;
import org.taranix.cafe.beans.repositories.typekeys.BeanTypeKey;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Date;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Negative/Positive: CafeClassInfo Definition & Inspection")
class CafeClassTest {

    @Test
    @DisplayName("Negative: Should throw exception when class has multiple constructors")
    void shouldThrowWhenMoreThanOneGetConstructor() {
        // building CafeClassInfo triggers scanMembers -> findConstructor -> should throw
        assertThrows(CafeClassMetadataException.class, () -> CafeClassFactory.create(CafeClassMetadataTestFixtures.TwoConstructorsClass.class));
    }

    @Test
    @DisplayName("Positive: Should correctly find all annotated methods and fields in class")
    void shouldFindAllAnnotatedGetMethodsMetadatasAndGetFieldsMetadata() {
        //given
        CafeClass cafeClass = CafeMetadataRegistry
                .builder()
                .withClass(CafeClassMetadataTestFixtures.ManyProvidersAndInjectables.class)
                .build()
                .getClassMetadata(CafeClassMetadataTestFixtures.ManyProvidersAndInjectables.class);


        //when
        Set<CafeMember> allMembers = cafeClass.getMembers();
        Set<CafeMethod> allMethods = cafeClass.getMethods();
        Set<CafeField> allFields = cafeClass.getFields();
        CafeConstructor constructor = cafeClass.getConstructor();

        //then
        assertEquals(9, allMembers.size());
        assertEquals(5, allMethods.size());
        assertEquals(3, allFields.size());

        assertTrue(constructor.hasDependencies());
        assertTrue(constructor.getRequiredTypeKeys().contains(BeanTypeKey.from(BigDecimal.class)));
        assertTrue(constructor.getProvidedTypeKeys().contains(BeanTypeKey.from(CafeClassMetadataTestFixtures.ManyProvidersAndInjectables.class)));
    }

    @Test
    @DisplayName("Positive: Should correctly determine simple field and method return types")
    void shouldDetermineFieldAndMethodType() {
        //given
        CafeClass cafeClass = CafeMetadataRegistry
                .builder()
                .withClass(CafeClassMetadataTestFixtures.IntegerProviderAndStringInjectable.class)
                .build()
                .getClassMetadata(CafeClassMetadataTestFixtures.IntegerProviderAndStringInjectable.class);

        //when
        CafeField genericField = cafeClass.getFields().stream().findFirst().orElse(null);
        CafeMethod genericMethod = cafeClass.getMethods().stream().findFirst().orElse(null);
        CafeConstructor cafeConstructorDescriptor = cafeClass.getConstructor();

        //then
        assertNotNull(genericField);
        assertNotNull(genericMethod);
        assertNotNull(cafeConstructorDescriptor);

        assertEquals(2, cafeConstructorDescriptor.getProvidedTypeKeys().size());
        assertEquals(String.class, genericField.getFieldTypeKey().getType());
        assertEquals(Integer.class, genericMethod.getMethodReturnTypeKey().getType());
    }

    @Test
    @DisplayName("Positive: Should correctly resolve generic types from class hierarchy for members")
    void shouldDetermineFieldAndMethodTypeAndParameterType() {
        //given
        CafeClass cafeClass = CafeMetadataRegistry
                .builder()
                .withClass(CafeClassMetadataTestFixtures.DateProviderWithInstantParameterAndLongInjectable.class)
                .build()
                .getClassMetadata(CafeClassMetadataTestFixtures.DateProviderWithInstantParameterAndLongInjectable.class);

        //when
        CafeField genericField = cafeClass.getFields().stream().findFirst().orElse(null);
        CafeMethod genericMethod = cafeClass.getMethods().stream().findFirst().orElse(null);
        CafeConstructor cafeConstructorDescriptor = cafeClass.getConstructor();

        //then
        assertNotNull(genericField);
        assertNotNull(genericMethod);
        assertNotNull(cafeConstructorDescriptor);

        assertEquals(2, cafeConstructorDescriptor.getProvidedTypeKeys().size());
        assertEquals(Long.class, genericField.getFieldTypeKey().getType());
        assertEquals(Date.class, genericMethod.getMethodReturnTypeKey().getType());
        assertTrue(genericMethod.getRequiredTypeKeys().contains(BeanTypeKey.from(Instant.class)));
    }

    @Test
    @DisplayName("Positive: Should find annotated members inherited from superclass")
    void shouldFindInheritedAnnotatedMembers() {
        CafeClass info = CafeClassFactory.create(CafeClassMetadataTestFixtures.Sub.class);

        // inherited annotated field
        assertTrue(info.getFields().stream()
                        .anyMatch(f -> f.getField().getName().equals("deprecatedField")),
                "Inherited field should be found");

        // inherited annotated method
        assertTrue(info.getMethods().stream()
                        .anyMatch(m -> m.getMethod().getName().equals("deprecatedMethod")),
                "Inherited method should be found");
    }

    @Test
    @DisplayName("Positive: Should correctly identify dependencies for static vs. instance provider methods")
    void staticMethodShouldNotDependOnOwner() throws NoSuchMethodException {
        CafeClass info = CafeClassFactory.create(StaticProvider.class);

        Method staticM = StaticProvider.class.getDeclaredMethod("provideStatic");
        Method instM = StaticProvider.class.getDeclaredMethod("provideInstance");

        CafeMethod staticDesc = info.getMethodMetadata(staticM);
        CafeMethod instDesc = info.getMethodMetadata(instM);

        // static provider must not require owner class
        assertFalse(staticDesc.getRequiredTypeKeys().contains(BeanTypeKey.from(StaticProvider.class)),
                "Static method should not depend on owner class");

        // instance provider must require owner class
        assertTrue(instDesc.getRequiredTypeKeys().contains(BeanTypeKey.from(StaticProvider.class)),
                "Instance method should depend on owner class");
    }

    @Test
    @DisplayName("Positive: Should correctly identify 'primary' and 'named' beans")
    void shouldCorrectlyIdentifyPrimaryAndNamedBeans() {
        // given
        CafeClass info = CafeClassFactory.create(CafeClassMetadataTestFixtures.PrimaryAndNamedProvider.class);

        // when
        CafeMethod primaryMethod = info.getMethods().stream()
                .filter(m -> m.getMethod().getName().equals("providePrimary"))
                .findFirst().orElseThrow();

        CafeMethod namedMethod = info.getMethods().stream()
                .filter(m -> m.getMethod().getName().equals("provideNamed"))
                .findFirst().orElseThrow();

        // then
        assertTrue(primaryMethod.getAnnotationModifiers().contains(CafePrimary.class), "Primary method should be marked as primary");
        assertFalse(namedMethod.getAnnotationModifiers().contains(CafePrimary.class), "Named method should not be marked as primary");

        assertEquals("MyNamedBean", namedMethod.getAnnotation(CafeName.class).value(), "Named method should have correct name");
        assertNull(primaryMethod.getAnnotation(CafeName.class), "Primary method should not have an explicit name");
    }

    @Test
    @DisplayName("Positive: Should correctly identify Optional dependencies via CafeOptional")
    void shouldIdentifyOptionalGetRequiredTypes() {
        // given
        CafeClass info = CafeClassFactory.create(CafeClassMetadataTestFixtures.OptionalInjectionService.class);

        // when
        CafeField requiredField = info.getFields().stream()
                .filter(f -> f.getField().getName().equals("requiredDependency"))
                .findFirst().orElseThrow();

        CafeField optionalField = info.getFields().stream()
                .filter(f -> f.getField().getName().equals("optionalDependency"))
                .findFirst().orElseThrow();

        // then
        assertFalse(requiredField.getAnnotationModifiers().contains(CafeOptional.class), "Required field should not be optional");
        assertTrue(optionalField.getAnnotationModifiers().contains(CafeOptional.class), "Optional field should be marked as optional");
    }

    @Test
    @DisplayName("Positive: Should handle nested generic types (List<String>) in fields and methods")
    void shouldHandleNestedGenerics() {
        // given
        CafeClass info = CafeClassFactory.create(CafeClassMetadataTestFixtures.NestedGenericsProvider.class);

        // when
        CafeField listField = info.getFields().stream()
                .filter(f -> f.getField().getName().equals("listOfStrings"))
                .findFirst().orElseThrow();

        CafeMethod listProvider = info.getMethods().stream()
                .filter(m -> m.getMethod().getName().equals("provideList"))
                .findFirst().orElseThrow();

        // then
        // Check field type key (List<String>)
        assertTrue(listField.getFieldTypeKey().toString().contains("java.util.List<java.lang.String>"));
        // Check method return type key (List<String>)
        assertTrue(listProvider.getMethodReturnTypeKey().toString().contains("java.util.List<java.lang.String>"));
    }


    @Test
    @DisplayName("Positive: Should correctly resolve dependencies from Generic-typed Constructor")
    void shouldResolveGenericGetConstructorGetRequiredTypes() {
        // given
        CafeClass info = CafeClassFactory.create(CafeClassMetadataTestFixtures.GenericConstructorService.class);

        // when
        CafeConstructor constructor = info.getConstructor();

        // then
        // The constructor should depend on the generic type T, which is resolved to Integer
        assertTrue(constructor.hasDependencies());
        assertTrue(constructor.getRequiredTypeKeys().contains(BeanTypeKey.from(Integer.class)));
        assertTrue(constructor.getProvidedTypeKeys().contains(BeanTypeKey.from(CafeClassMetadataTestFixtures.GenericConstructorService.class)));
    }

    @Test
    @DisplayName("Positive: Should correctly resolve dependencies from a SuperClass constructor")
    void shouldResolveSuperClassGetConstructorGetRequiredTypes() {
        // given
        CafeClass info = CafeClassFactory.create(CafeClassMetadataTestFixtures.SubClassWithInheritedConstructor.class);

        // when
        CafeConstructor constructor = info.getConstructor();

        // then
        // The effective constructor is from SuperClassWithConstructor and requires String
        assertTrue(constructor.hasDependencies());
        assertTrue(constructor.getRequiredTypeKeys().contains(BeanTypeKey.from(String.class)));
        assertTrue(constructor.getProvidedTypeKeys().contains(BeanTypeKey.from(CafeClassMetadataTestFixtures.SubClassWithInheritedConstructor.class)));
    }

    @Test
    @DisplayName("Should categorize members correctly during initialization")
    void shouldCategorizeMembers() {
        // given
        CafeClass metadata = CafeClassFactory.create(CafeClassMetadataTestFixtures.PerformanceTestBean.class);

        // when
        Set<CafeField> fields = metadata.getFields();
        Set<CafeMethod> methods = metadata.getMethods();

        // then
        assertEquals(2, fields.size(), "Should identify exactly 2 fields");
        assertEquals(1, methods.size(), "Should identify exactly 1 method");
        assertNotNull(metadata.getConstructor(), "Should identify constructor");

        // Verify lookup speed/identity (should not recreate sets)
        assertSame(fields, metadata.getFields(), "Getters should return cached collections");
    }

    @Test
    @DisplayName("Should be robust against Bridge/Synthetic methods")
    void shouldIgnoreBridgeMethods() {
        // This test requires a scenario where compiler generates bridge methods
        // e.g. covariant return types in inheritance.

        class Base<T> {
            @CafeProvider
            public T getVal() {
                return null;
            }

            @CafeProvider
            public Integer getVal1() {
                return 0;
            }
        }

        class Concrete extends Base<String> {
            @Override
            @CafeProvider
            public String getVal() {
                return "test";
            }
        }

        CafeClass metadata = CafeClassFactory.create(Concrete.class);

        // Java compiler creates a bridge method Object getVal() in Concrete class.
        // We expect only 1 visible CafeProvider method.

        long providerCount = metadata.getMethods().stream()
                .filter(m -> m.getMethod().getName().equals("getVal"))
                .count();

        // Warning: This assertion depends on whether reflection returns bridge methods
        // The factory has been updated to filter them out (!m.isBridge()).
        assertEquals(1, providerCount, "Should exclude bridge methods generated by compiler");
    }
}