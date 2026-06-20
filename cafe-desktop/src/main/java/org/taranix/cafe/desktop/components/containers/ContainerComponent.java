package org.taranix.cafe.desktop.components.containers;

import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Widget;

import java.util.UUID;

public interface ContainerComponent {

    void addComponent(Class<?> componentType);

    void addComponent(Class<?> componentType, String sourceId);

    void removeComponent(UUID componentId);

    boolean isOpen(String sourceId);

    void activate(String sourceId);

    Widget create(Control parent);

    void show();

    void hide();

    void dispose();
}
