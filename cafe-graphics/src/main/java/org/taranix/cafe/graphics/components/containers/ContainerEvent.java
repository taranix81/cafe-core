package org.taranix.cafe.graphics.components.containers;

import org.taranix.cafe.graphics.events.CafeEvent;
import org.taranix.cafe.graphics.events.CafeEventType;

public class ContainerEvent extends CafeEvent {

    public ContainerEvent(CafeEventType type) {
        super(Container.class, type);
    }


}
