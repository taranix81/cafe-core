package org.taranix.cafe.beans.app;


import org.taranix.cafe.beans.annotations.methods.CafeProvider;
import org.taranix.cafe.beans.annotations.classes.CafeService;

import java.util.Date;

@CafeService
public class BaseFactory {

    @CafeProvider
    Date getDate() {
        return new Date();
    }
}
