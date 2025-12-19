package org.taranix.cafe.beans.scanner;

import lombok.Getter;
import org.taranix.cafe.beans.annotations.fields.CafeInject;
import org.taranix.cafe.beans.annotations.classes.CafeService;

@CafeService
@Getter
public class ServiceClass {

    @CafeInject
    private Long magicNumber;
}
