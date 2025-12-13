package org.taranix.cafe.beans.resolvers;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.taranix.cafe.beans.CafeApplicationContext;
import org.taranix.cafe.beans.exceptions.BeansRepositoryException;
import org.taranix.cafe.beans.resolvers.data.*;

import java.util.Collection;
import java.util.Set;

class BeanResolverMethodsTests {

    @Test
    @DisplayName("Should provide service class by method")
    void shouldProvideServicesByMethod() {
        //given
        CafeApplicationContext cafeApplicationContext = CafeApplicationContext
                .builder()
                .withClasses(Set.of(ServiceClassProvider.class))
                .build();

        //when
        cafeApplicationContext.initialize();
        ServiceClassProvider instance = cafeApplicationContext.getInstance(ServiceClassProvider.class);
        ServiceClass instance2 = cafeApplicationContext.getInstance(ServiceClass.class);
        ServiceClass instance3 = cafeApplicationContext.getInstance(ServiceClass.class, "2");

        //then
        Assertions.assertNotNull(instance);
        Assertions.assertNotNull(instance2);
        Assertions.assertEquals(1, instance2.getId());
        Assertions.assertEquals(2, instance3.getId());
    }


    @Test
    @DisplayName("Should provide service class by method with arguments")
    void shouldProvideServiceByMethodWithArguments() {
        //given
        CafeApplicationContext cafeApplicationContext = CafeApplicationContext
                .builder()
                .withClass(ServiceClassProviderWCA.class)
                .withClass(StringProvider.class)
                .build();

        //when
        cafeApplicationContext.initialize();
        ServiceClassProviderWCA instance = cafeApplicationContext.getInstance(ServiceClassProviderWCA.class);
        StringProvider instance2 = cafeApplicationContext.getInstance(StringProvider.class);
        ServiceClass instance3 = cafeApplicationContext.getInstance(ServiceClass.class);

        //then
        Assertions.assertNotNull(instance);
        Assertions.assertNotNull(instance2);
        Assertions.assertNotNull(instance3);

    }


    @Test
    @DisplayName("Should provide service class by method returning service class interface")
    void shouldProvideServicesImplementingInterface() {
        //given
        CafeApplicationContext cafeApplicationContext = CafeApplicationContext
                .builder()
                .withClass(InterfaceServiceProvider.class)
                .build();

        //when
        cafeApplicationContext.initialize();
        InterfaceServiceProvider instance = cafeApplicationContext.getInstance(InterfaceServiceProvider.class);
        InterfaceService instance2 = cafeApplicationContext.getInstance(InterfaceService.class);

        //then
        Assertions.assertNotNull(instance);
        Assertions.assertNotNull(instance2);
        Assertions.assertTrue(instance2 instanceof InterfaceServiceClass);
    }

    @Test
    @DisplayName("Should provide string as primary bean")
    void shouldResolveMethodBeanAsPrimary() {
        //given
        CafeApplicationContext cafeApplicationContext = CafeApplicationContext
                .builder()
                .withClass(ServiceProviderWithPrimary.class)
                .build();

        //when
        cafeApplicationContext.initialize();
        String primary = cafeApplicationContext.getInstance(String.class);
        Collection<String> strings = cafeApplicationContext.getInstances(String.class);

        //then
        Assertions.assertNotNull(primary);
        Assertions.assertNotNull(strings);
        Assertions.assertEquals(2, strings.size());
        Assertions.assertEquals("Primary", primary);

    }

    @Test
    @DisplayName("Should throw exception for many Primary Beans")
    void shouldThrowExceptionForManyPrimaryBeans() {
        //given
        CafeApplicationContext cafeApplicationContext = CafeApplicationContext
                .builder()
                .withClass(ServiceProviderWithPrimary.class)
                .withClass(OtherServiceProviderWithPrimary.class)
                .build();

        //when-then
        Assertions.assertThrowsExactly(BeansRepositoryException.class, cafeApplicationContext::initialize);


    }


}
