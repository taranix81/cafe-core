package org.taranix.cafe.beans.resolvers.data;


import org.taranix.cafe.beans.annotations.CafeFactory;
import org.taranix.cafe.beans.annotations.CafeProvider;

import java.util.UUID;

@CafeFactory
public class StringProvider {

    @CafeProvider
    String getUuid() {
        return UUID.randomUUID().toString();
    }
}
