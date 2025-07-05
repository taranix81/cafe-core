package org.taranix.cafe.graphics.components.containers;

import org.taranix.cafe.graphics.components.AbstractComponent;
import org.taranix.cafe.graphics.components.Component;

public abstract class AbstractContainer extends AbstractComponent implements Container {


    protected void addSubComponent(Component component) {
        component.setParentId(this.getId());
        getEventBus().register(component);
    }
}
