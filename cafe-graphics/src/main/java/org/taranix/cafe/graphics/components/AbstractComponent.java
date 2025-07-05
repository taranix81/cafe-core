package org.taranix.cafe.graphics.components;

import lombok.Getter;
import lombok.Setter;
import org.taranix.cafe.beans.annotations.CafeInject;
import org.taranix.cafe.graphics.events.CafeEvent;
import org.taranix.cafe.graphics.events.EventBus;
import org.taranix.cafe.graphics.events.messages.CafeOldEvent;

import java.util.UUID;

public abstract class AbstractComponent implements Component {

    private final UUID id = UUID.randomUUID();
    @CafeInject
    @Getter
    private EventBus eventBus;

    @Getter
    @Setter
    private UUID parentId;

    @Override
    public UUID getId() {
        return id;
    }

    public void postInit() {
    }

    public void dispose() {
        eventBus.unregister(this);
    }

    protected CafeOldEvent sendMessage(CafeOldEvent cafeOldEvent) {
        return eventBus.sendMessage(cafeOldEvent);
    }

    protected CafeEvent sendMessage(CafeEvent cafeEvent) {
        return eventBus.sendMessage(cafeEvent);
    }
}
