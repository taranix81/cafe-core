package org.taranix.cafe.beans.metadata.data;

import org.taranix.cafe.beans.annotations.CafeInject;
import org.taranix.cafe.beans.annotations.CafeService;

@CafeService
public class ServiceClassInjectable {

    @CafeInject
    ServiceClass serviceClass;
}
