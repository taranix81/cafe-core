package org.taranix.cafe.beans.metadata.data;

import org.taranix.cafe.beans.annotations.CafeInject;

import java.util.List;

public class ListServiceClassInjectable {


    @CafeInject
    List<ServiceClass> serviceClass;
}
