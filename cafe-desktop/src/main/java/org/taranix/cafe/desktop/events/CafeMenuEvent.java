package org.taranix.cafe.desktop.events;

public record CafeMenuEvent(String menuId, String actionId, Object source) {

    public static CafeMenuEvent of(String menuId, String actionId, Object source) {
        return new CafeMenuEvent(menuId, actionId, source);
    }

    public static CafeMenuEvent action(String actionId) {
        return new CafeMenuEvent(null, actionId, null);
    }
}
