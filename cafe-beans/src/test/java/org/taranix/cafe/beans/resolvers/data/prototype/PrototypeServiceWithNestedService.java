package org.taranix.cafe.beans.resolvers.data.prototype;


import lombok.Getter;
import org.taranix.cafe.beans.annotations.classes.CafePrototype;
import org.taranix.cafe.beans.annotations.fields.CafeInject;

import java.util.UUID;

@CafePrototype
@Getter
public class PrototypeServiceWithNestedService {
    private final UUID id = UUID.randomUUID();
    @CafeInject
    private PrototypeServiceData nestedService;
}
