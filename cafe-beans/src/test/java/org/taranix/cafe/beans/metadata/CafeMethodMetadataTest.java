package org.taranix.cafe.beans.metadata;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.taranix.cafe.beans.metadata.CafeMethodMetadataTestFixtures.GenericClass;
import org.taranix.cafe.beans.metadata.CafeMethodMetadataTestFixtures.IntegerClass;
import org.taranix.cafe.beans.metadata.CafeMethodMetadataTestFixtures.SimpleBeanWithProvider;
import org.taranix.cafe.beans.repositories.typekeys.BeanTypeKey;

import java.lang.reflect.Method;
import java.util.Objects;

class CafeMethodMetadataTest {

    @Test
    @DisplayName("Positive: Should create MethodInfo for a provider method without parameters")
    void shouldCreateMethodInfoWithEmptyParameters() throws NoSuchMethodException {
        CafeClassMetadata cafeClassMetadata = CafeClassMetadataFactory.create(SimpleBeanWithProvider.class);
        Method method = SimpleBeanWithProvider.class.getDeclaredMethod("getNamedProvidedString");
        CafeMethodMetadata methodMetadata = cafeClassMetadata.getMethodMetadata(method);

        Assertions.assertNotNull(methodMetadata);
        Assertions.assertEquals(String.class, methodMetadata.getMethodReturnTypeKey().getType());
        Assertions.assertEquals(String.class, Objects.requireNonNull(methodMetadata.getProvidedTypes().stream().findFirst().orElse(null)).getType());
        Assertions.assertEquals(method, methodMetadata.getMember());
        Assertions.assertEquals(SimpleBeanWithProvider.SOME_EXAMPLE_ID, methodMetadata.getMethodReturnTypeKey().getTypeIdentifier());

        Assertions.assertTrue(methodMetadata.getRequiredTypes().contains(BeanTypeKey.from(SimpleBeanWithProvider.class)));
    }

    @Test
    @DisplayName("Positive: Should create MethodInfo for a method with parameter dependencies")
    void shouldCreateMethodInfoWithParameters() throws NoSuchMethodException {
        CafeClassMetadata cafeClassMetadata = CafeClassMetadataFactory.create(CafeMethodMetadataTestFixtures.StringClass.class);
        CafeMethodMetadata methodInfo = cafeClassMetadata.getMethodMetadata("getStringProviderWithParameter", BeanTypeKey.from(String.class));

        Assertions.assertNotNull(methodInfo);
        Assertions.assertEquals(String.class, methodInfo.getMethodReturnTypeKey().getType());
        Assertions.assertEquals(String.class, Objects.requireNonNull(methodInfo.getProvidedTypes().stream().findFirst().orElse(null)).getType());
        Assertions.assertTrue(methodInfo.getRequiredTypes().contains(BeanTypeKey.from(String.class)));
        Assertions.assertTrue(methodInfo.getRequiredTypes().contains(BeanTypeKey.from(CafeMethodMetadataTestFixtures.StringClass.class)));
    }

    @Test
    @DisplayName("Positive: Should correctly resolve generic return type (T -> Integer) from subclass context")
    void shouldResolveGenericReturnType() throws NoSuchMethodException {
        // given
        CafeClassMetadata cafeClassMetadata = CafeClassMetadataFactory.create(IntegerClass.class);
        CafeMethodMetadata methodInfo = cafeClassMetadata.getMethodMetadata("getUnknownValue");

        // then
        Assertions.assertEquals(Integer.class, methodInfo.getMethodReturnTypeKey().getType());
        Assertions.assertTrue(methodInfo.getProvidedTypes().contains(BeanTypeKey.from(Integer.class)), "Should provide Integer bean.");
        Assertions.assertTrue(methodInfo.getRequiredTypes().contains(BeanTypeKey.from(IntegerClass.class)), "Should depend on owner class (IntegerClass).");
    }

    @Test
    @DisplayName("Positive: Should not list owner class as dependency for static provider method")
    void shouldNotDependOnOwnerForStaticMethod() throws NoSuchMethodException {
        // given
        CafeClassMetadata cafeClassMetadata = CafeClassMetadataFactory.create(CafeMethodMetadataTestFixtures.StaticMethodProvider.class);

        CafeMethodMetadata staticMethodInfo = cafeClassMetadata.getMethodMetadata("provideStaticInteger");
        CafeMethodMetadata instanceMethodInfo = cafeClassMetadata.getMethodMetadata("provideInstanceString");

        // then
        Assertions.assertFalse(staticMethodInfo.getRequiredTypes().contains(BeanTypeKey.from(CafeMethodMetadataTestFixtures.StaticMethodProvider.class)),
                "Static method should not depend on owner class instance.");
        Assertions.assertTrue(instanceMethodInfo.getRequiredTypes().contains(BeanTypeKey.from(CafeMethodMetadataTestFixtures.StaticMethodProvider.class)),
                "Instance method must depend on owner class.");
    }

    @Test
    @DisplayName("Negative: Should not provide a bean for a non-annotated method")
    void shouldNotProvideBeanForNonAnnotatedMethod() throws NoSuchMethodException {
        // given
        CafeClassMetadata cafeClassMetadata = CafeClassMetadataFactory.create(CafeMethodMetadataTestFixtures.SimpleBeanWithProvider.class);
        CafeMethodMetadata methodInfo = cafeClassMetadata.getMethodMetadata("getNonProviderString", BeanTypeKey.from(String.class));

        // then
        Assertions.assertNull(methodInfo, "Non-provider method should not be available from CafeClassMetadata.");

    }

    @Test
    @DisplayName("Positive: Should correctly resolve parameters for an annotated provider method in generic context")
    @Disabled
    void shouldResolveProviderMethodParametersInGenericContext() {
        // given
        CafeClassMetadata cafeClassMetadata = CafeClassMetadataFactory.create(CafeMethodMetadataTestFixtures.GenericClass.class);
        CafeMethodMetadata methodInfo = cafeClassMetadata.getMethodMetadata("getStringProviderWithParameter", BeanTypeKey.from(String.class));

        // then
        Assertions.assertTrue(methodInfo.getProvidedTypes().contains(BeanTypeKey.from(String.class)), "Should provide String bean.");
        Assertions.assertTrue(methodInfo.getRequiredTypes().contains(BeanTypeKey.from(String.class)), "Parameter (String) must be a dependency.");
        Assertions.assertTrue(methodInfo.getRequiredTypes().contains(BeanTypeKey.from(GenericClass.class)), "Instance method must depend on owner class (GenericClass).");
    }
}