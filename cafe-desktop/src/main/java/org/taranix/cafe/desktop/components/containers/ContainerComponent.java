package org.taranix.cafe.desktop.components.containers;

import org.taranix.cafe.desktop.components.Component;

public interface ContainerComponent extends Component {

    void addComponent(Component component);

    boolean removeComponent(Component component);

    Component selected();
}
