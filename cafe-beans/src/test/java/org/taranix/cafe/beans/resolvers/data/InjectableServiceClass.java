package org.taranix.cafe.beans.resolvers.data;

import lombok.Getter;
import org.taranix.cafe.beans.annotations.fields.CafeInject;
import org.taranix.cafe.beans.annotations.classes.CafeSingleton;

@CafeSingleton
@Getter
public class InjectableServiceClass {

    @CafeInject
    private ServiceClass serviceClass;
}
