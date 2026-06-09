package org.taranix.cafe.beans.scanner;

import org.taranix.cafe.beans.annotations.methods.CafeProvider;
import org.taranix.cafe.beans.annotations.classes.CafeSingleton;

@CafeSingleton
public class FactoryClass {

    @CafeProvider
    Long magicNumber() {
        return 13L;
    }
}
