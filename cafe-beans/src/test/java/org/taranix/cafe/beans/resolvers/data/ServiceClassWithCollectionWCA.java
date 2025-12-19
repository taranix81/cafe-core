package org.taranix.cafe.beans.resolvers.data;

import lombok.Getter;
import org.taranix.cafe.beans.annotations.classes.CafeService;

import java.util.List;

@CafeService
public class ServiceClassWithCollectionWCA {

    @Getter
    private final List<ServiceClass> children;


    public ServiceClassWithCollectionWCA(final List<ServiceClass> children) {
        this.children = children;
    }
}
