package org.taranix.cafe.beans.metadata.data;

import org.apache.commons.lang3.NotImplementedException;
import org.taranix.cafe.beans.annotations.CafeFactory;
import org.taranix.cafe.beans.annotations.CafeProvider;

import java.util.Date;

@CafeFactory
public class DateProviderWithIntegerParameter {
    @CafeProvider
    public Date provide(Integer in) {
        throw new NotImplementedException();
    }
}
