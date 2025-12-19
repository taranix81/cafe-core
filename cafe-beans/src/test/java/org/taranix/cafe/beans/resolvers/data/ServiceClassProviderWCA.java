package org.taranix.cafe.beans.resolvers.data;

import org.taranix.cafe.beans.annotations.methods.CafeProvider;
import org.taranix.cafe.beans.annotations.classes.CafeService;

@CafeService
public class ServiceClassProviderWCA {

    private final String id;

    public ServiceClassProviderWCA(final String id) {
        this.id = id;
    }

    @CafeProvider
    public ServiceClass getServiceClass() {
        return new ServiceClass().withId(1);
    }
}
