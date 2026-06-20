package org.taranix.cafe.desktop.events;

public record CafeEvent(String eventType, Object source, Object payload) {

    public static CafeEvent of(String eventType, Object source) {
        return new CafeEvent(eventType, source, null);
    }

    public static CafeEvent of(String eventType, Object source, Object payload) {
        return new CafeEvent(eventType, source, payload);
    }
}
