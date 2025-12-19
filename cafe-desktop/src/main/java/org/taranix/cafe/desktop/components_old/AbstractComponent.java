package org.taranix.cafe.desktop.components_old;

import lombok.Getter;
import lombok.Setter;
import org.taranix.cafe.beans.annotations.fields.CafeInject;
import org.taranix.cafe.desktop.components_old.events.EventBus;
import org.taranix.cafe.desktop.components_old.events.messages.Message;

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

    protected Message sendMessage(Message message) {
        return eventBus.sendMessage(message);
    }
}
