package org.taranix.cafe.beans.resolvers.data.prototype;

import lombok.Getter;
import org.taranix.cafe.beans.annotations.classes.CafeSingleton;

import java.util.UUID;

@CafeSingleton
@Getter
public class ServiceInterface implements InterfaceMarker {
    private final UUID id = UUID.randomUUID();
}
