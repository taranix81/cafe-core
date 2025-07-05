package org.taranix.cafe.beans.resolvers;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.taranix.cafe.beans.CafeApplicationContext;
import org.taranix.cafe.beans.annotations.CafeAnnotationUtils;
import org.taranix.cafe.beans.resolvers.data.prototype.*;

public class PrototypeBeanResolverFieldsTests {

    @Test
    void shouldResolvePrototypeWithServiceCollectionField() {
        //given
        CafeApplicationContext cafeApplicationContext = CafeApplicationContext
                .builder()
                .withAnnotations(CafeAnnotationUtils.BASE_ANNOTATIONS)
                .withClass(PrototypeServiceWithNestedCollectionServices.class)
                .withClass(PrototypeServiceData.class)
                .build();

        //when
        cafeApplicationContext.initialize();
        PrototypeServiceWithNestedCollectionServices instanceOnDemand = cafeApplicationContext.getInstance(PrototypeServiceWithNestedCollectionServices.class);
        PrototypeServiceData instanceOnDemand2 = cafeApplicationContext.getInstance(PrototypeServiceData.class);

        //then
        Assertions.assertNotNull(instanceOnDemand);
        Assertions.assertFalse(instanceOnDemand.getServices().isEmpty());
        Assertions.assertNotNull(instanceOnDemand2);
    }

    @Test
    void shouldResolvePrototypeWithInterfaceCollectionField() {
        //given
        CafeApplicationContext cafeApplicationContext = CafeApplicationContext
                .builder()
                .withAnnotations(CafeAnnotationUtils.BASE_ANNOTATIONS)
                .withClass(PrototypeServiceWithNestedCollectionInterface.class)
                .withClass(PrototypeInterface.class)
                .withClass(ServiceInterface.class)
                .build();

        //when
        cafeApplicationContext.initialize();
        PrototypeServiceWithNestedCollectionInterface instanceOnDemand = cafeApplicationContext.getInstance(PrototypeServiceWithNestedCollectionInterface.class);
        PrototypeInterface instanceOnDemand2 = cafeApplicationContext.getInstance(PrototypeInterface.class);
        ServiceInterface instanceOnDemand3 = cafeApplicationContext.getInstance(ServiceInterface.class);
        ServiceInterface instanceOnDemand4 = cafeApplicationContext.getInstance(ServiceInterface.class);
        //then
        Assertions.assertNotNull(instanceOnDemand);
        Assertions.assertNotNull(instanceOnDemand2);
        Assertions.assertNotNull(instanceOnDemand3);
        Assertions.assertNotNull(instanceOnDemand4);
        Assertions.assertFalse(instanceOnDemand.getServices().isEmpty());
        Assertions.assertEquals(2, instanceOnDemand.getServices().size());
    }
}
