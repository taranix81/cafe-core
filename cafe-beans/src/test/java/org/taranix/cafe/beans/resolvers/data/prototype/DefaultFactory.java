package org.taranix.cafe.beans.resolvers.data.prototype;

import org.taranix.cafe.beans.annotations.methods.CafeProvider;
import org.taranix.cafe.beans.annotations.classes.CafeService;

@CafeService
public class DefaultFactory {

    @CafeProvider
    Long getLong() {
        return 13L;
    }
}
