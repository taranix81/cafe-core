package org.taranix.cafe.beans.resolvers.data.prototype;

import lombok.Getter;
import org.taranix.cafe.beans.annotations.classes.CafePrototype;
import org.taranix.cafe.beans.annotations.fields.CafeInject;

import java.util.List;

@CafePrototype
@Getter
public class PrototypeServiceWithNestedCollectionInterface {

    @CafeInject
    List<InterfaceMarker> services;
}
