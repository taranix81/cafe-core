package org.taranix.cafe.beans.resolvers.data;

import org.taranix.cafe.beans.annotations.CafeFactory;
import org.taranix.cafe.beans.annotations.CafeProvider;

@CafeFactory
public class InterfaceServiceProvider {

    @CafeProvider
    InterfaceService getInterfaceService() {
        return new InterfaceServiceClass();
    }
}
