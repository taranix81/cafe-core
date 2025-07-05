package org.taranix.cafe.beans.resolvers.data.prototype;

import lombok.Getter;
import org.taranix.cafe.beans.annotations.CafeService;
import org.taranix.cafe.beans.annotations.Scope;

@CafeService(scope = Scope.Prototype)
@Getter
public class PrototypeServiceClassWCA extends PrototypeServiceData {

    private final Long aLong;

    public PrototypeServiceClassWCA(Long aLong) {
        this.aLong = aLong;
    }
}
