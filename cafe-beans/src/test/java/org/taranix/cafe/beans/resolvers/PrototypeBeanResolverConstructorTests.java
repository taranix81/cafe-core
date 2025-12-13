package org.taranix.cafe.beans.resolvers;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.taranix.cafe.beans.CafeApplicationContext;
import org.taranix.cafe.beans.descriptors.CafeClassInfo;
import org.taranix.cafe.beans.resolvers.data.prototype.DefaultFactory;
import org.taranix.cafe.beans.resolvers.data.prototype.PrototypeServiceClassWCA;
import org.taranix.cafe.beans.resolvers.data.prototype.PrototypeServiceData;

public class PrototypeBeanResolverConstructorTests {

    @Test
    void shouldResolvePrototype() {
        //given
        CafeApplicationContext cafeApplicationContext = CafeApplicationContext
                .builder()
                .withClass(PrototypeServiceData.class)
                .build();

        //when
        cafeApplicationContext.initialize();
        CafeClassInfo descriptor = cafeApplicationContext.getClassDescriptor(PrototypeServiceData.class);
        PrototypeServiceData instanceOnDemand = cafeApplicationContext.getInstance(PrototypeServiceData.class);
        PrototypeServiceData instanceOnDemand2 = cafeApplicationContext.getInstance(PrototypeServiceData.class);

        //then
        Assertions.assertNotNull(descriptor);
        Assertions.assertNotNull(instanceOnDemand);
        Assertions.assertNotNull(instanceOnDemand2);
        Assertions.assertNotEquals(instanceOnDemand.getId(), instanceOnDemand2.getId());
    }

    @Test
    void shouldResolvePrototypeWithConstructorSimpleArgument() {
        //given
        CafeApplicationContext cafeApplicationContext = CafeApplicationContext
                .builder()
                .withClass(PrototypeServiceClassWCA.class)
                .withClass(DefaultFactory.class)
                .build();

        //when
        cafeApplicationContext.initialize();
        PrototypeServiceClassWCA instanceOnDemand = cafeApplicationContext.getInstance(PrototypeServiceClassWCA.class);
        PrototypeServiceClassWCA instanceOnDemand2 = cafeApplicationContext.getInstance(PrototypeServiceClassWCA.class);

        //then
        Assertions.assertNotNull(instanceOnDemand);
        Assertions.assertNotNull(instanceOnDemand2);
        Assertions.assertNotEquals(instanceOnDemand.getId(), instanceOnDemand2.getId());
        Assertions.assertEquals(instanceOnDemand.getALong(), instanceOnDemand2.getALong());

    }


}
