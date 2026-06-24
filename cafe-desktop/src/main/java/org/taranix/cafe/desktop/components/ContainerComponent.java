package org.taranix.cafe.desktop.components;

import java.util.Set;

public interface ContainerComponent extends Component {

    Set<Component> getComponents();

    Component getActiveComponent();

}
