package org.taranix.cafe.beans.resolvers.data;

import org.taranix.cafe.beans.annotations.methods.CafeProvider;
import org.taranix.cafe.beans.annotations.classes.CafeSingleton;

@CafeSingleton
public class InterfaceServiceProvider {

    @CafeProvider
    InterfaceService getInterfaceService() {
        return new InterfaceServiceClass();
    }
}
