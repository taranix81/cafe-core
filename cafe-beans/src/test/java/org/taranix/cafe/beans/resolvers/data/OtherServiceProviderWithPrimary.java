package org.taranix.cafe.beans.resolvers.data;


import org.taranix.cafe.beans.annotations.methods.CafeProvider;
import org.taranix.cafe.beans.annotations.classes.CafeService;
import org.taranix.cafe.beans.annotations.modifiers.CafePrimary;

@CafeService
public class OtherServiceProviderWithPrimary {

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
