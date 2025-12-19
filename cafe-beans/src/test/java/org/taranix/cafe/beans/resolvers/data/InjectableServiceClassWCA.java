package org.taranix.cafe.beans.resolvers.data;

import lombok.Getter;
import org.taranix.cafe.beans.annotations.fields.CafeInject;
import org.taranix.cafe.beans.annotations.classes.CafeService;

@CafeService
public class InjectableServiceClassWCA {

    @CafeInject
    @Getter
    private ServiceClassWCA serviceClassWCA;
}
