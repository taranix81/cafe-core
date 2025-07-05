package org.taranix.cafe.beans.resolvers.data.prototype;


import lombok.Getter;
import org.taranix.cafe.beans.annotations.CafeInject;
import org.taranix.cafe.beans.annotations.CafeService;
import org.taranix.cafe.beans.annotations.Scope;

import java.util.UUID;

@CafeService(scope = Scope.Prototype)
@Getter
public class PrototypeServiceWithNestedService {
    private final UUID id = UUID.randomUUID();
    @CafeInject
    private PrototypeServiceData nestedService;
}
