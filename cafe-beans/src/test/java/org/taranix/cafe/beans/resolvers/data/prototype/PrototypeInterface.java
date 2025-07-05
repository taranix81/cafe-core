package org.taranix.cafe.beans.resolvers.data.prototype;

import org.taranix.cafe.beans.annotations.CafeService;
import org.taranix.cafe.beans.annotations.Scope;

import java.util.UUID;

@CafeService(scope = Scope.Prototype)
public class PrototypeInterface implements InterfaceMarker {
    private final UUID id = UUID.randomUUID();
}
