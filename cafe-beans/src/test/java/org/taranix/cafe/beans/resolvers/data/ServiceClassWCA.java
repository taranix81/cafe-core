package org.taranix.cafe.beans.resolvers.data;

import lombok.Getter;
import org.taranix.cafe.beans.annotations.CafeService;

import java.util.Date;

@CafeService

public class ServiceClassWCA extends ServiceClass {
    @Getter
    private final Date createDate;

    public ServiceClassWCA(final Date createDate) {
        this.createDate = createDate;
    }
}
