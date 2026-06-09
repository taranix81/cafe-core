package org.taranix.cafe.beans.resolvers.data;

import org.taranix.cafe.beans.annotations.methods.CafeProvider;
import org.taranix.cafe.beans.annotations.classes.CafeSingleton;

import java.util.Date;

@CafeSingleton
public class DateProvider {

    @CafeProvider
    public Date now() {
        return new Date();
    }
}
