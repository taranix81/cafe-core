package org.taranix.cafe.beans.properties;

import lombok.Getter;
import org.taranix.cafe.beans.annotations.CafeProperty;
import org.taranix.cafe.beans.annotations.CafeService;

@CafeService
public class ServiceClassWithProperty {

    @Getter
    @CafeProperty(name = "test.property")
    String property;

    @Getter
    @CafeProperty(name = "test.double")
    Double doubleNumber;

    @Getter
    @CafeProperty(name = "test.boolean")
    Boolean booleanValue;

    @Getter
    @CafeProperty(name = "test.integer")
    Integer integerValue;
}
