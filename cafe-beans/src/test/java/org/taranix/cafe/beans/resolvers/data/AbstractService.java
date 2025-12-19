package org.taranix.cafe.beans.resolvers.data;

import org.taranix.cafe.beans.annotations.fields.CafeInject;
import org.taranix.cafe.beans.annotations.classes.CafeService;

@CafeService
public abstract class AbstractService<T> {

    @CafeInject
    private AbstractServiceDependency someDependency;

    public void doSomething(T t) {
    }
}
