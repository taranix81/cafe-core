package org.taranix.cafe.beans.resolvers.data.prototype;

import org.taranix.cafe.beans.annotations.classes.CafePrototype;

import java.util.UUID;

@CafePrototype
public class PrototypeInterface implements InterfaceMarker {
    private final UUID id = UUID.randomUUID();
}
