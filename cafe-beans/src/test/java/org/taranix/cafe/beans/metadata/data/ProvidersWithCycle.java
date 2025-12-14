package org.taranix.cafe.beans.metadata.data;

import org.apache.commons.lang3.NotImplementedException;
import org.taranix.cafe.beans.annotations.CafeProvider;

public class ProvidersWithCycle {
    @CafeProvider
    public String getString(Integer integer) {
        throw new NotImplementedException();
    }

    @CafeProvider
    public Integer getInteger(String string) {
        throw new NotImplementedException();
    }
}
