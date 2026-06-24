package org.taranix.cafe.desktop.components.containers.ctabfolder;

import lombok.Getter;
import org.taranix.cafe.beans.events.CafeEvent;
import org.taranix.cafe.desktop.components.Component;

@Getter
public class CafeTabItemEvent implements CafeEvent {
    private final String title;
    private final Component component;


    protected CafeTabItemEvent(String title, Component component) {
        this.title = title;
        this.component = component;
    }

    public static CafeTabItemEvent of(String title, Component component) {
        return new CafeTabItemEvent(title, component);
    }
}
