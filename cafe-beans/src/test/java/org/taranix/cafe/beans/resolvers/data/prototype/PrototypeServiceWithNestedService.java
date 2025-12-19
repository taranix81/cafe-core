package org.taranix.cafe.beans.resolvers.data.prototype;


import lombok.Getter;
import org.taranix.cafe.beans.annotations.fields.CafeInject;
import org.taranix.cafe.beans.annotations.classes.CafeService;
import org.taranix.cafe.beans.annotations.classes.Scope;

import java.util.UUID;

@CafeService(scope = Scope.Prototype)
@Getter
public class PrototypeServiceWithNestedService {
    private final UUID id = UUID.randomUUID();
    @CafeInject
    private PrototypeServiceData nestedService;
}
