package org.taranix.cafe.beans.scanner;

import org.taranix.cafe.beans.annotations.CafeFactory;
import org.taranix.cafe.beans.annotations.CafeProvider;

@CafeFactory
public class FactoryClass {

    @CafeProvider
    Long magicNumber() {
        return 13L;
    }
}
