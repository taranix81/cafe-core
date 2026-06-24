package org.taranix.cafe.desktop.events;

import lombok.Getter;
import org.eclipse.swt.events.SelectionEvent;
import org.taranix.cafe.beans.events.CafeEvent;

@Getter
public class CafeMenuEvent implements CafeEvent {

    private final SelectionEvent origin;

    private final String menuId;


    protected CafeMenuEvent(String menuId, SelectionEvent origin) {
        this.origin = origin;
        this.menuId = menuId;
    }

    public static CafeMenuEvent of(String menuId, SelectionEvent origin) {
        return new CafeMenuEvent(menuId, origin);
    }

}
