package org.taranix.cafe.beans.resolvers.data;

import lombok.Getter;
import org.taranix.cafe.beans.annotations.fields.CafeInject;
import org.taranix.cafe.beans.annotations.classes.CafeService;

import java.util.List;
import java.util.Set;

@CafeService
@Getter
public class InjectableCollectionsServiceClass {

    @CafeInject
    private ServiceClass[] serviceClassArray;

    @CafeInject
    private List<ServiceClass> serviceClassList;

    @CafeInject
    private Set<ServiceClass> serviceClassSet;

}
