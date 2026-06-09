package org.taranix.cafe.beans.scanner;

import lombok.Getter;
import org.taranix.cafe.beans.annotations.fields.CafeInject;
import org.taranix.cafe.beans.annotations.classes.CafeSingleton;

@CafeSingleton
@Getter
public class ServiceClass {

    @CafeInject
    private Long magicNumber;
}
