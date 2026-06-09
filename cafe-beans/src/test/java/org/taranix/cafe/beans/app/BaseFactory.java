package org.taranix.cafe.beans.app;


import org.taranix.cafe.beans.annotations.methods.CafeProvider;
import org.taranix.cafe.beans.annotations.classes.CafeSingleton;

import java.util.Date;

@CafeSingleton
public class BaseFactory {

    @CafeProvider
    Date getDate() {
        return new Date();
    }
}
