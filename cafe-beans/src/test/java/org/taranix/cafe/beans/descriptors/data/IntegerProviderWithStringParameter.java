package org.taranix.cafe.beans.descriptors.data;

import org.apache.commons.lang3.NotImplementedException;
import org.taranix.cafe.beans.annotations.CafeProvider;

public class IntegerProviderWithStringParameter {
    @CafeProvider
    public Integer provide(String in) {
        throw new NotImplementedException();
    }
}
