package org.taranix.cafe.desktop.components.application;

import org.taranix.cafe.desktop.components.containers.ContainerComponent;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface ApplicationComponent extends ContainerComponent {

    void start();

    void shutDown();

    void setLogicallyActive(ContainerComponent container);

    <T> List<T> getComponents(Class<T> componentType);

    <T> Optional<T> getActiveComponent(Class<T> componentType);

    Set<ContainerComponent> getContainers();
}
