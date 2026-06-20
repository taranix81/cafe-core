package org.taranix.cafe.desktop.actions;

import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.ShellListener;
import org.eclipse.swt.widgets.Shell;
import org.taranix.cafe.beans.annotations.classes.CafeSingleton;
import org.taranix.cafe.beans.reflection.CafeReflectionUtils;
import org.taranix.cafe.desktop.annotations.CafeMenuItemSelectionHandler;
import org.taranix.cafe.desktop.annotations.CafeShellHandler;
import org.taranix.cafe.desktop.annotations.ShellHandlerType;

import java.util.HashMap;
import java.util.Map;

@CafeSingleton
public class HandlersService {

    private final Map<String, HandlerSignature> menuItemSelectionHandlers;
    private final Map<ShellHandlerType, HandlerSignature> shellHandlers;

    public HandlersService() {
        menuItemSelectionHandlers = new HashMap<>();
        shellHandlers = new HashMap<>();
    }

    public void add(CafeMenuItemSelectionHandler annotation, HandlerSignature handler) {
        HandlerSignature existing = menuItemSelectionHandlers.get(annotation.id());
        if (existing == null || annotation.primary()) {
            menuItemSelectionHandlers.put(annotation.id(), handler);
        }
    }

    public void add(CafeShellHandler annotation, HandlerSignature handler) {
        HandlerSignature existing = shellHandlers.get(annotation.type());
        if (existing == null || annotation.primary()) {
            shellHandlers.put(annotation.type(), handler);
        }
    }

    public void bind(Shell shell) {
        shell.addShellListener(new ShellListener() {
            @Override public void shellActivated(ShellEvent e) {}
            @Override public void shellDeactivated(ShellEvent e) {}
            @Override public void shellDeiconified(ShellEvent e) {}
            @Override public void shellIconified(ShellEvent e) {}

            @Override
            public void shellClosed(ShellEvent e) {
                HandlerSignature handler = shellHandlers.get(ShellHandlerType.Closed);
                if (handler != null) invoke(handler, e);
            }
        });
    }

    private void invoke(HandlerSignature sig, ShellEvent event) {
        if (sig.handlingMethod().getParameterCount() == 0) {
            CafeReflectionUtils.getMethodValue(sig.handlingMethod(), sig.handlerInstance());
        } else if (sig.handlingMethod().getParameterCount() == 1
                && sig.handlingMethod().getParameterTypes()[0].equals(ShellEvent.class)) {
            CafeReflectionUtils.getMethodValue(sig.handlingMethod(), sig.handlerInstance(), event);
        }
    }
}
