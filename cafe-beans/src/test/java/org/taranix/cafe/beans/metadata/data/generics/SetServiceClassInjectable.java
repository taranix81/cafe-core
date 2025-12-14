package org.taranix.cafe.beans.metadata.data.generics;

import org.taranix.cafe.beans.annotations.CafeInject;
import org.taranix.cafe.beans.metadata.data.ServiceClass;

import java.util.Set;

public class SetServiceClassInjectable {


    @CafeInject
    Set<ServiceClass> serviceClass;
}
