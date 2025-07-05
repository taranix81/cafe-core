package org.taranix.cafe.graphics.services;

import org.taranix.cafe.beans.CafeBeansFactory;
import org.taranix.cafe.beans.annotations.CafeService;
import org.taranix.cafe.beans.repositories.typekeys.BeanTypeKey;
import org.taranix.cafe.graphics.components.Component;

@CafeService
public class ComponentFactory {

    private final CafeBeansFactory beansFactory;

    public ComponentFactory(CafeBeansFactory beansFactory) {
        this.beansFactory = beansFactory;
    }


    public <T extends Component> T getOrCreate(Class<T> type) {
        return (T) beansFactory.getBean(BeanTypeKey.from(type));
    }

    public <T extends Component> T getOrCreate(Class<T> type, Class<?> arguments) {
        return (T) beansFactory.getBean(BeanTypeKey.from(type, "", arguments));
    }
}
