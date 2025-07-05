package org.taranix.cafe.beans.resolvers.data.prototype;

import org.taranix.cafe.beans.annotations.CafeFactory;
import org.taranix.cafe.beans.annotations.CafeProvider;

@CafeFactory
public class DefaultFactory {

    @CafeProvider
    Long getLong() {
        return 13L;
    }
}
