package org.taranix.cafe.beans.resolvers.data;

import lombok.Getter;
import org.taranix.cafe.beans.annotations.classes.CafeSingleton;

import java.util.List;

@CafeSingleton
public class ServiceClassWithCollectionWCA {

    @Getter
    private final List<ServiceClass> children;


    public ServiceClassWithCollectionWCA(final List<ServiceClass> children) {
        this.children = children;
    }
}
