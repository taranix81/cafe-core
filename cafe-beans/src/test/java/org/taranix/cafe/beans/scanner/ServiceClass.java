package org.taranix.cafe.beans.scanner;

import lombok.Getter;
import org.taranix.cafe.beans.annotations.CafeInject;
import org.taranix.cafe.beans.annotations.CafeService;

@CafeService
@Getter
public class ServiceClass {

    @CafeInject
    private Long magicNumber;
}
