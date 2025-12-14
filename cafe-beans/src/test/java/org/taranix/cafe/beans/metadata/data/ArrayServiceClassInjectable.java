package org.taranix.cafe.beans.metadata.data;

import lombok.Getter;
import org.taranix.cafe.beans.annotations.CafeInject;

@Getter
public class ArrayServiceClassInjectable {

    @CafeInject
    ServiceClass[] serviceClass;
}
