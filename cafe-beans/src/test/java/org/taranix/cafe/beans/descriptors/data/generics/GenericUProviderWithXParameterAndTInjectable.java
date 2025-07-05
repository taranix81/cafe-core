package org.taranix.cafe.beans.descriptors.data.generics;

import org.apache.commons.lang3.NotImplementedException;
import org.taranix.cafe.beans.annotations.CafeInject;
import org.taranix.cafe.beans.annotations.CafeProvider;

public class GenericUProviderWithXParameterAndTInjectable<T, U, X> {

    @CafeInject
    private T unknown;

    @CafeProvider
    public U getUnknown(X input) {
        throw new NotImplementedException();
    }
}
