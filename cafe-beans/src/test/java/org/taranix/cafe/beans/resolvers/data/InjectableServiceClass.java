package org.taranix.cafe.beans.resolvers.data;

import lombok.Getter;
import org.taranix.cafe.beans.annotations.CafeInject;
import org.taranix.cafe.beans.annotations.CafeService;

@CafeService
@Getter
public class InjectableServiceClass {

    @CafeInject
    private ServiceClass serviceClass;
}
