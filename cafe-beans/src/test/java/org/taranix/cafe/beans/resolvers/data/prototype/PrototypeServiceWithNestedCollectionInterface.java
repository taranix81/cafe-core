package org.taranix.cafe.beans.resolvers.data.prototype;

import lombok.Getter;
import org.taranix.cafe.beans.annotations.CafeInject;
import org.taranix.cafe.beans.annotations.CafeService;
import org.taranix.cafe.beans.annotations.Scope;

import java.util.List;

@CafeService(scope = Scope.Prototype)
@Getter
public class PrototypeServiceWithNestedCollectionInterface {

    @CafeInject
    List<InterfaceMarker> services;
}
