package org.taranix.cafe.desktop.events;

import lombok.Getter;
import org.eclipse.swt.events.ShellEvent;
import org.taranix.cafe.beans.events.CafeEvent;

@Getter
public class CafeShellEvent extends CafeEvent {
    private final ShellEvent origin;

    public CafeShellEvent(ShellEvent origin) {
        this.origin = origin;
    }

    public static CafeShellEvent of(ShellEvent disposeEvent) {
        return new CafeShellEvent(disposeEvent);
    }
}
