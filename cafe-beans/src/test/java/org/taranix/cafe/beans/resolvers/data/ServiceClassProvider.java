package org.taranix.cafe.beans.resolvers.data;

import org.taranix.cafe.beans.annotations.methods.CafeProvider;
import org.taranix.cafe.beans.annotations.classes.CafeService;
import org.taranix.cafe.beans.annotations.modifiers.CafeName;

@CafeService
public class ServiceClassProvider {

    @CafeProvider
    public ServiceClass getServiceClass() {
        return new ServiceClass().withId(1);
    }

    @CafeProvider
    @CafeName("2")
    public ServiceClass getServiceClassWithName() {
        return new ServiceClass().withId(2);
    }
}
