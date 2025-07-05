package org.taranix.cafe.beans.app;


import org.taranix.cafe.beans.annotations.CafeFactory;
import org.taranix.cafe.beans.annotations.CafeProvider;

import java.util.Date;

@CafeFactory
public class BaseFactory {

    @CafeProvider
    Date getDate() {
        return new Date();
    }
}
