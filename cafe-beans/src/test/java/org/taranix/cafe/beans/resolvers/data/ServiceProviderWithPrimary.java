package org.taranix.cafe.beans.resolvers.data;


import org.taranix.cafe.beans.annotations.CafeFactory;
import org.taranix.cafe.beans.annotations.CafePrimary;
import org.taranix.cafe.beans.annotations.CafeProvider;

@CafeFactory
public class ServiceProviderWithPrimary {

    @CafePrimary
    @CafeProvider
    public String primary() {
        return "Primary";
    }

    
    @CafeProvider
    public String other() {
        return "Other";
    }
}
