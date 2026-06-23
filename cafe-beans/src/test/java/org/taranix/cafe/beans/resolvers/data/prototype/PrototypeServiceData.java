package org.taranix.cafe.beans.resolvers.data.prototype;

import lombok.Getter;
import org.taranix.cafe.beans.annotations.classes.CafePrototype;

import java.util.UUID;

@CafePrototype
@Getter
public class PrototypeServiceData {
    private final UUID id = UUID.randomUUID();

}
