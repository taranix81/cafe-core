package org.taranix.cafe.beans.metadata.data;

import org.apache.commons.lang3.NotImplementedException;
import org.taranix.cafe.beans.annotations.CafeFactory;
import org.taranix.cafe.beans.annotations.CafeProvider;

@CafeFactory
public class IntegerProviderWithStringParameter {
    @CafeProvider
    public Integer provide(String in) {
        throw new NotImplementedException();
    }
}
