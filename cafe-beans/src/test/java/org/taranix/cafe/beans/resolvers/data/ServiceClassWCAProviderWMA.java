package org.taranix.cafe.beans.resolvers.data;

import org.taranix.cafe.beans.annotations.methods.CafeProvider;
import org.taranix.cafe.beans.annotations.classes.CafeSingleton;

import java.util.Date;

@CafeSingleton
public class ServiceClassWCAProviderWMA {

    @CafeProvider
    public ServiceClass create(final Date createDate) {
        return new ServiceClassWCA(createDate);
    }
}
