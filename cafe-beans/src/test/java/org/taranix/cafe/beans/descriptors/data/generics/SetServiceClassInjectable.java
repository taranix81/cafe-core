package org.taranix.cafe.beans.descriptors.data.generics;

import org.taranix.cafe.beans.annotations.CafeInject;
import org.taranix.cafe.beans.descriptors.data.ServiceClass;

import java.util.Set;

public class SetServiceClassInjectable {


    @CafeInject
    Set<ServiceClass> serviceClass;
}
