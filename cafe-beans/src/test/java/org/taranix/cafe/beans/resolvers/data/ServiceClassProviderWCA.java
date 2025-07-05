package org.taranix.cafe.beans.resolvers.data;

import org.taranix.cafe.beans.annotations.CafeFactory;
import org.taranix.cafe.beans.annotations.CafeProvider;

@CafeFactory
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
