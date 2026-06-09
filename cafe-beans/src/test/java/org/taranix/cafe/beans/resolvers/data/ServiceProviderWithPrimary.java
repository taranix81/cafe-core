package org.taranix.cafe.beans.resolvers.data;


import org.taranix.cafe.beans.annotations.classes.CafeSingleton;
import org.taranix.cafe.beans.annotations.methods.CafeHandler;
import org.taranix.cafe.beans.annotations.methods.CafeProvider;
import org.taranix.cafe.beans.annotations.modifiers.CafeName;
import org.taranix.cafe.beans.annotations.modifiers.CafePrimary;

@CafeSingleton
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


    @CafeHandler
    public void someHandler() {

    }

    @CafeHandler
    public void someHandler2() {

    }

    @CafeName(value = "other")
    @CafeHandler()
    public void someHandler1() {

    }
}
