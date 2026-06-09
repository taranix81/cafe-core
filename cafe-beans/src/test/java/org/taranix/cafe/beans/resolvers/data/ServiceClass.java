package org.taranix.cafe.beans.resolvers.data;

import lombok.Getter;
import org.taranix.cafe.beans.annotations.classes.CafeSingleton;

@CafeSingleton
public class ServiceClass {

    @Getter
    private int id;

    public ServiceClass withId(int id) {
        this.id = id;
        return this;
    }
}
