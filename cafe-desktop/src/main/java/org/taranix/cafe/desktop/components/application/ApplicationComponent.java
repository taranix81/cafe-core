package org.taranix.cafe.desktop.components.application;

import org.taranix.cafe.desktop.components.Component;
import org.taranix.cafe.desktop.components.containers.ContainerComponent;

import java.util.Set;

public interface ApplicationComponent extends ContainerComponent {

    void start();

    void shutDown();

    ContainerComponent getActiveContainer();

    Set<ContainerComponent> getContainers();

    <T extends Component> Set<T> getComponent(Class<T> componentType);
}
