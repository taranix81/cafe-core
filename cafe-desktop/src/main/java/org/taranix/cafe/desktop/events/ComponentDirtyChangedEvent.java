package org.taranix.cafe.desktop.events;

import org.taranix.cafe.beans.events.CafeEvent;

import java.util.UUID;

public class ComponentDirtyChangedEvent extends CafeEvent {

    public static final String EVENT_TYPE = "COMPONENT_DIRTY_CHANGED";

    private final UUID componentId;
    private final boolean dirty;
    private final String displayName;

    public ComponentDirtyChangedEvent(UUID componentId, boolean dirty, String displayName) {
        super(EVENT_TYPE);
        this.componentId = componentId;
        this.dirty = dirty;
        this.displayName = displayName;
    }

    public UUID componentId() {
        return componentId;
    }

    public boolean dirty() {
        return dirty;
    }

    public String displayName() {
        return displayName;
    }
}
