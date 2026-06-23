package org.taranix.cafe.beans.events;

public class CafeEvent {

    private final String id;

    public CafeEvent(String id) {
        this.id = id;
    }

    public CafeEvent() {
        this("");
    }

    public String id() {
        return id;
    }
}
