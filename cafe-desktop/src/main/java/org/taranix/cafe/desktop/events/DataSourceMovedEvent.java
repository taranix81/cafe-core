package org.taranix.cafe.desktop.events;

import org.taranix.cafe.beans.events.CafeEvent;

public class DataSourceMovedEvent extends CafeEvent {

    public static final String EVENT_TYPE = "DATA_SOURCE_MOVED";

    private final String sourceId;
    private final String newDisplayName;

    public DataSourceMovedEvent(String sourceId, String newDisplayName) {
        super(EVENT_TYPE);
        this.sourceId = sourceId;
        this.newDisplayName = newDisplayName;
    }

    public String sourceId() {
        return sourceId;
    }

    public String newDisplayName() {
        return newDisplayName;
    }
}
