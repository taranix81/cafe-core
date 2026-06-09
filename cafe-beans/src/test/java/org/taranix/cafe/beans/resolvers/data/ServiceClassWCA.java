package org.taranix.cafe.beans.resolvers.data;

import lombok.Getter;
import org.taranix.cafe.beans.annotations.classes.CafeSingleton;

import java.util.Date;

@CafeSingleton

public class ServiceClassWCA extends ServiceClass {
    @Getter
    private final Date createDate;

    public ServiceClassWCA(final Date createDate) {
        this.createDate = createDate;
    }
}
