package org.taranix.cafe.beans.resolvers.data;

import org.taranix.cafe.beans.annotations.CafeFactory;
import org.taranix.cafe.beans.annotations.CafeProvider;

import java.util.Date;

@CafeFactory
public class DateProvider {

    @CafeProvider
    public Date now() {
        return new Date();
    }
}
