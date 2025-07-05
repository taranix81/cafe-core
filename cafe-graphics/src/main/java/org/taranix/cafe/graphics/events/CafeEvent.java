package org.taranix.cafe.graphics.events;

import lombok.Getter;
import org.taranix.cafe.graphics.components.Component;

import java.util.UUID;

@Getter
public abstract class CafeEvent {

    private final Class<? extends Component> target;

    private final CafeEventType type;

    private UUID targetId;

    protected CafeEvent(Class<? extends Component> target, CafeEventType type) {
        this.target = target;
        this.type = type;
    }
}
