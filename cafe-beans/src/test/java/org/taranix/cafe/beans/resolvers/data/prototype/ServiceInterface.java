package org.taranix.cafe.beans.resolvers.data.prototype;

import lombok.Getter;
import org.taranix.cafe.beans.annotations.CafeService;

import java.util.UUID;

@CafeService
@Getter
public class ServiceInterface implements InterfaceMarker {
    private final UUID id = UUID.randomUUID();
}
