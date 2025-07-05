package org.taranix.cafe.beans.descriptors.data;

import org.apache.commons.lang3.NotImplementedException;
import org.taranix.cafe.beans.annotations.CafeInject;
import org.taranix.cafe.beans.annotations.CafeName;
import org.taranix.cafe.beans.annotations.CafeProvider;

import java.io.Serializable;
import java.math.BigDecimal;

public class ManyProvidersAndInjectables {

    @CafeInject
    Double aDouble;

    @CafeInject
    @CafeName("Sample1")
    Double namedDouble;

    @CafeInject
    @CafeName("Sample2")
    Double otherNamedDouble;

    Serializable serializable;

    ManyProvidersAndInjectables(BigDecimal bigDecimal) {

    }

    @CafeProvider
    String getString() {
        throw new NotImplementedException();
    }

    @CafeProvider
    String getString(int a) {
        throw new NotImplementedException();
    }


    @CafeProvider
    @CafeName("Sample1")
    String getNamedString() {
        throw new NotImplementedException();
    }

    @CafeProvider
    @CafeName("Sample1")
    String getOtherNamedString() {
        throw new NotImplementedException();
    }

    @CafeProvider
    @CafeName("Sample2")
    String getNamedString(int a) {
        throw new NotImplementedException();
    }

    Runnable getRunnable() {
        throw new NotImplementedException();
    }

    @CafeName("Runnable")
    Runnable getNamedRunnable() {
        throw new NotImplementedException();
    }
}
