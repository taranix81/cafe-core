package org.taranix.cafe.beans.descriptors.data;

import org.taranix.cafe.beans.annotations.CafeFactory;
import org.taranix.cafe.beans.annotations.CafeProvider;


@CafeFactory
public class ServiceClassProvider {

    @CafeProvider
    public ServiceClass getServiceClass() {
        return new ServiceClass();
    }
}
