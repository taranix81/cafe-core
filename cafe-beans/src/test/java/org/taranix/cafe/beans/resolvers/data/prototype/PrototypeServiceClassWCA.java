package org.taranix.cafe.beans.resolvers.data.prototype;

import lombok.Getter;
import org.taranix.cafe.beans.annotations.classes.CafePrototype;

@CafePrototype
@Getter
public class PrototypeServiceClassWCA extends PrototypeServiceData {

    private final Long aLong;

    public PrototypeServiceClassWCA(Long aLong) {
        this.aLong = aLong;
    }
}
