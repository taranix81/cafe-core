package org.taranix.cafe.beans.resolvers;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.taranix.cafe.beans.CafeApplicationContext;
import org.taranix.cafe.beans.annotations.CafeAnnotationUtils;
import org.taranix.cafe.beans.resolvers.data.*;

import java.util.Collection;

public class BeanResolverFieldsTests {


    @Test
    @DisplayName("Should inject service class, resolved by constructor, into another service class.")
    void shouldInjectServiceClassResolvedByConstructor() {
        //given
        CafeApplicationContext cafeApplicationContext = CafeApplicationContext
                .builder()
                .withAnnotations(CafeAnnotationUtils.BASE_ANNOTATIONS)
                .withClass(ServiceClass.class)
                .withClass(InjectableServiceClass.class)
                .build();

        //when
        cafeApplicationContext.initialize();
        cafeApplicationContext.initialize();
        ServiceClass instance = cafeApplicationContext.getInstance(ServiceClass.class);
        InjectableServiceClass instance2 = cafeApplicationContext.getInstance(InjectableServiceClass.class);

        //then
        Assertions.assertNotNull(instance);
        Assertions.assertNotNull(instance2);
        Assertions.assertEquals(instance2.getServiceClass(), instance);
    }

    @Test
    @DisplayName("Should inject collection of service classes, resolved by constructor, into another service class.")
    void shouldInjectCollectionOfServiceClassResolvedByConstructor() {
        //given
        CafeApplicationContext cafeApplicationContext = CafeApplicationContext
                .builder()
                .withAnnotations(CafeAnnotationUtils.BASE_ANNOTATIONS)
                .withClass(ServiceClass.class)
                .withClass(ServiceClassExtension.class)
                .withClass(InjectableCollectionsServiceClass.class)
                .build();

        //when
        cafeApplicationContext.initialize();
        Collection<ServiceClass> serviceClass = cafeApplicationContext.getInstances(ServiceClass.class);
        ServiceClassExtension serviceClassExtension = cafeApplicationContext.getInstance(ServiceClassExtension.class);
        InjectableCollectionsServiceClass injectableService = cafeApplicationContext.getInstance(InjectableCollectionsServiceClass.class);

        //then
        Assertions.assertNotNull(serviceClass);
        Assertions.assertNotNull(serviceClassExtension);
        Assertions.assertNotNull(injectableService);
        Assertions.assertNotNull(injectableService.getServiceClassArray());
        Assertions.assertNotNull(injectableService.getServiceClassList());
        Assertions.assertNotNull(injectableService.getServiceClassSet());
        Assertions.assertEquals(2, injectableService.getServiceClassArray().length);
        Assertions.assertEquals(2, injectableService.getServiceClassList().size());
        Assertions.assertEquals(2, injectableService.getServiceClassSet().size());
    }

    @Test
    @DisplayName("Should inject service class, resolved by constructor with arguments, into another service class.")
    void shouldInjectServiceResolvedByConstructorWithArgumentsProvided() {
        //given
        CafeApplicationContext cafeApplicationContext = CafeApplicationContext
                .builder()
                .withAnnotations(CafeAnnotationUtils.BASE_ANNOTATIONS)
                .withClass(ServiceClassWCA.class)
                .withClass(InjectableServiceClassWCA.class)
                .withClass(DateProvider.class)
                .build();

        //when
        cafeApplicationContext.initialize();
        ServiceClassWCA instance = cafeApplicationContext.getInstance(ServiceClassWCA.class);
        InjectableServiceClassWCA instance2 = cafeApplicationContext.getInstance(InjectableServiceClassWCA.class);
        DateProvider instance3 = cafeApplicationContext.getInstance(DateProvider.class);

        //then
        Assertions.assertNotNull(instance);
        Assertions.assertNotNull(instance2);
        Assertions.assertNotNull(instance3);
        Assertions.assertNotNull(instance2.getServiceClassWCA());
    }

    @Test
    @DisplayName("Should inject service class, resolved by method with arguments, into another service class.")
    void shouldInjectServiceResolvedByMethodWithArguments() {
        //given
        CafeApplicationContext cafeApplicationContext = CafeApplicationContext
                .builder()
                .withAnnotations(CafeAnnotationUtils.BASE_ANNOTATIONS)
                .withClass(ServiceClassWCAProviderWMA.class)
                .withClass(InjectableServiceClass.class)
                .withClass(DateProvider.class)
                .build();

        //when
        cafeApplicationContext.initialize();
        ServiceClassWCAProviderWMA instance = cafeApplicationContext.getInstance(ServiceClassWCAProviderWMA.class);
        InjectableServiceClass instance2 = cafeApplicationContext.getInstance(InjectableServiceClass.class);
        DateProvider instance3 = cafeApplicationContext.getInstance(DateProvider.class);

        //then
        Assertions.assertNotNull(instance);
        Assertions.assertNotNull(instance2);
        Assertions.assertNotNull(instance3);
        Assertions.assertNotNull(instance2.getServiceClass());

    }

    @Test
    @DisplayName("Should inject service class, resolved by method, into another service class.")
    void shouldInjectServiceResolvedByMethod() {
        //given
        CafeApplicationContext cafeApplicationContext = CafeApplicationContext
                .builder()
                .withAnnotations(CafeAnnotationUtils.BASE_ANNOTATIONS)
                .withClass(ServiceClassProvider.class)
                .withClass(InjectableServiceClass.class)
                .build();

        //when
        cafeApplicationContext.initialize();
        ServiceClassProvider instance = cafeApplicationContext.getInstance(ServiceClassProvider.class);
        InjectableServiceClass instance2 = cafeApplicationContext.getInstance(InjectableServiceClass.class);

        //then
        Assertions.assertNotNull(instance);
        Assertions.assertNotNull(instance2);
        Assertions.assertEquals(1, instance2.getServiceClass().getId());
    }

    @Test
    void shouldInjectGenericValue() {
        //given
        CafeApplicationContext cafeApplicationContext = CafeApplicationContext
                .builder()
                .withAnnotations(CafeAnnotationUtils.BASE_ANNOTATIONS)
                .withClass(DateGenericClass.class)
                .withClass(DateProvider.class)
                .build();
        //when
        cafeApplicationContext.initialize();
        DateGenericClass instance = cafeApplicationContext.getInstance(DateGenericClass.class);
        DateProvider instance2 = cafeApplicationContext.getInstance(DateProvider.class);

        //then
        Assertions.assertNotNull(instance);
        Assertions.assertNotNull(instance2);
        Assertions.assertNotNull(instance.getValue());
    }
}
