package org.taranix.cafe.beans.resolvers.data;


import org.taranix.cafe.beans.annotations.methods.CafeProvider;
import org.taranix.cafe.beans.annotations.classes.CafeSingleton;

import java.util.UUID;

@CafeSingleton
public class StringProvider {

    @CafeProvider
    String getUuid() {
        return UUID.randomUUID().toString();
    }
}
