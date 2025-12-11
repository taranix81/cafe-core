package org.taranix.cafe.desktop.components_old.application.containers;

import org.taranix.cafe.desktop.components_old.AbstractComponent;
import org.taranix.cafe.desktop.components_old.Component;

public abstract class AbstractContainer extends AbstractComponent implements Container {

    @Override
    public void addSubComponent(Component component) {
        component.setParentId(this.getId());
        getEventBus().register(component);
    }
}
