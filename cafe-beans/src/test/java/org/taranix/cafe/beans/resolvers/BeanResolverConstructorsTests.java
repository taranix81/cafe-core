package org.taranix.cafe.beans.resolvers;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.taranix.cafe.beans.CafeApplicationContext;
import org.taranix.cafe.beans.exceptions.CafeBeansContextException;
import org.taranix.cafe.beans.resolvers.data.*;
import org.taranix.cafe.beans.resolvers.data.prototype.AbstractServiceExtensionUse;

import java.util.Collection;

public class BeanResolverConstructorsTests {

    public static final String SHOULD_RESOLVE_SERVICE_CLASS_BY_CONSTRUCTOR = "Should resolve service class by constructor";

    @Test
    @DisplayName(SHOULD_RESOLVE_SERVICE_CLASS_BY_CONSTRUCTOR + ".")
    void shouldResolveServiceClass() {
        //given
        CafeApplicationContext cafeApplicationContext = CafeApplicationContext
                .builder()
                .withClass(ServiceClass.class)
                .build();

        //when
        cafeApplicationContext.initialize();
        ServiceClass instance = cafeApplicationContext.getInstance(ServiceClass.class);

        //then
        Assertions.assertNotNull(instance);
    }

    @Test
    @DisplayName(SHOULD_RESOLVE_SERVICE_CLASS_BY_CONSTRUCTOR + " which implement interface.")
    void shouldResolveServiceClassWhichImplementInterface() {
        //given
        CafeApplicationContext cafeApplicationContext = CafeApplicationContext
                .builder()
                .withClass(InterfaceServiceClass.class)
                .build();

        //when
        cafeApplicationContext.initialize();
        InterfaceServiceClass instance = cafeApplicationContext.getInstance(InterfaceServiceClass.class);
        InterfaceService instance2 = cafeApplicationContext.getInstance(InterfaceService.class);

        //then
        Assertions.assertNotNull(instance);
        Assertions.assertNotNull(instance2);

    }

    @Test
    @DisplayName(SHOULD_RESOLVE_SERVICE_CLASS_BY_CONSTRUCTOR + " which extends service.")
    void shouldResolveServiceClassWhichExtendService() {
        //given
        CafeApplicationContext cafeApplicationContext = CafeApplicationContext
                .builder()
                .withClass(ServiceClassExtension.class)
                .build();

        //when
        cafeApplicationContext.initialize();
        ServiceClassExtension instance = cafeApplicationContext.getInstance(ServiceClassExtension.class);
        ServiceClass instance2 = cafeApplicationContext.getInstance(ServiceClass.class);

        //then
        Assertions.assertNotNull(instance);
        Assertions.assertNotNull(instance2);

    }


    //Constructor injection
    @Test
    @DisplayName(SHOULD_RESOLVE_SERVICE_CLASS_BY_CONSTRUCTOR + " which required collection of services.")
    void shouldInjectCollectionIntoConstructor() {
        //given
        CafeApplicationContext cafeApplicationContext = CafeApplicationContext
                .builder()
                .withClass(ServiceClassWithCollectionWCA.class)
                .withClass(ServiceClass.class)
                .withClass(ServiceClassExtension.class)
                .build();

        //when
        cafeApplicationContext.initialize();
        ServiceClassWithCollectionWCA instance = cafeApplicationContext.getInstance(ServiceClassWithCollectionWCA.class);
        Collection<ServiceClass> manyInstances = cafeApplicationContext.getInstances(ServiceClass.class);


        //then
        Assertions.assertNotNull(instance);
        Assertions.assertNotNull(manyInstances);
        Assertions.assertEquals(2, manyInstances.size());
        Assertions.assertEquals(2, instance.getChildren().size());
    }

    @Test
    @DisplayName(SHOULD_RESOLVE_SERVICE_CLASS_BY_CONSTRUCTOR + " which required service.")
    void shouldInjectServiceIntoConstructor() {
        //given
        CafeApplicationContext cafeApplicationContext = CafeApplicationContext
                .builder()
                .withClass(ServiceClassWithServiceWCA.class)
                .withClass(ServiceClass.class)
                .build();

        //when
        cafeApplicationContext.initialize();
        ServiceClassWithServiceWCA instance = cafeApplicationContext.getInstance(ServiceClassWithServiceWCA.class);
        ServiceClass serviceClass = cafeApplicationContext.getInstance(ServiceClass.class);


        //then
        Assertions.assertNotNull(instance);
        Assertions.assertNotNull(instance.getServiceClass());
        Assertions.assertNotNull(serviceClass);

    }

    @Test
    @DisplayName(SHOULD_RESOLVE_SERVICE_CLASS_BY_CONSTRUCTOR + " which required service (injecting extension).")
    void shouldInjectServiceExtensionIntoConstructor() {
        //given
        CafeApplicationContext cafeApplicationContext = CafeApplicationContext
                .builder()
                .withClass(ServiceClassWithServiceWCA.class)
                .withClass(ServiceClassExtension.class)
                .build();


        //when
        cafeApplicationContext.initialize();
        ServiceClassWithServiceWCA instance = cafeApplicationContext.getInstance(ServiceClassWithServiceWCA.class);
        ServiceClassExtension serviceClassExtension = cafeApplicationContext.getInstance(ServiceClassExtension.class);
        ServiceClass serviceClass = cafeApplicationContext.getInstance(ServiceClassExtension.class);


        //then
        Assertions.assertNotNull(instance);
        Assertions.assertNotNull(serviceClass);
        Assertions.assertNotNull(serviceClassExtension);
        Assertions.assertEquals(serviceClassExtension, serviceClass);
        Assertions.assertNotNull(instance.getServiceClass());
    }

    @Test
    void shouldResolveServiceClassWCAByConstructor() {
        //given
        CafeApplicationContext cafeApplicationContext = CafeApplicationContext
                .builder()
                .withClass(ServiceClassWCA.class)
                .withClass(DateProvider.class)
                .build();

        //when
        cafeApplicationContext.initialize();
        ServiceClassWCA instance = cafeApplicationContext.getInstance(ServiceClassWCA.class);
        DateProvider instance3 = cafeApplicationContext.getInstance(DateProvider.class);

        //then
        Assertions.assertNotNull(instance);
        Assertions.assertNotNull(instance3);

    }

    @Test
    void shouldNotResolveAbstractServiceClass() {
        //given
        CafeApplicationContext cafeApplicationContext = CafeApplicationContext
                .builder()
                .withClass(AbstractService.class)
                .build();

        //when
        Assertions.assertThrows(CafeBeansContextException.class, cafeApplicationContext::initialize);


    }

    @Test
    void shouldNotResolveAbstractServiceAndItsExtensionClass() {
        //given
        CafeApplicationContext cafeApplicationContext = CafeApplicationContext
                .builder()
                .withClass(AbstractService.class)
                .withClass(AbstractServiceExtension.class)
                .withClass(AbstractServiceExtensionUse.class)
                .build();

        //when
        Assertions.assertThrows(CafeBeansContextException.class, cafeApplicationContext::initialize);


    }
}
