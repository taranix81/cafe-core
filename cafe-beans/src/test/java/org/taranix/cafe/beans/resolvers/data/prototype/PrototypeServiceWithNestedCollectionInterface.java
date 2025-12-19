package org.taranix.cafe.beans.resolvers.data.prototype;

import lombok.Getter;
import org.taranix.cafe.beans.annotations.fields.CafeInject;
import org.taranix.cafe.beans.annotations.classes.CafeService;
import org.taranix.cafe.beans.annotations.classes.Scope;

import java.util.List;

@CafeService(scope = Scope.Prototype)
@Getter
public class PrototypeServiceWithNestedCollectionInterface {

    @CafeInject
    List<InterfaceMarker> services;
}
