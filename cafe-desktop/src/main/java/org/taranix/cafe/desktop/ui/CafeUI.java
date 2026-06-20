package org.taranix.cafe.desktop.ui;

import org.eclipse.swt.widgets.Display;

public final class CafeUI {

    private CafeUI() {}

    public static void run(Runnable action) {
        Display display = Display.getCurrent();
        if (display != null && !display.isDisposed()) {
            display.syncExec(action);
        } else {
            Display.getDefault().syncExec(action);
        }
    }

    public static void runAsync(Runnable action) {
        Display display = Display.getCurrent();
        if (display != null && !display.isDisposed()) {
            display.asyncExec(action);
        } else {
            Display.getDefault().asyncExec(action);
        }
    }
}
