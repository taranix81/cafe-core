package org.taranix.cafe.beans.resolvers.data;

import org.taranix.cafe.beans.annotations.CafeFactory;
import org.taranix.cafe.beans.annotations.modifiers.CafeName;
import org.taranix.cafe.beans.annotations.CafeProvider;

@CafeFactory
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
