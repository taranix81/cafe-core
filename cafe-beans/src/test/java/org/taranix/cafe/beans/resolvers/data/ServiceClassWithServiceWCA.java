package org.taranix.cafe.beans.resolvers.data;

import lombok.Getter;
import org.taranix.cafe.beans.annotations.CafeService;

@CafeService
public class ServiceClassWithServiceWCA {

    @Getter
    private final ServiceClass serviceClass;


    public ServiceClassWithServiceWCA(final ServiceClass serviceClass) {
        this.serviceClass = serviceClass;
    }
}
