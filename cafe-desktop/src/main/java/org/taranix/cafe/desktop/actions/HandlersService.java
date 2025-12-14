package org.taranix.cafe.desktop.actions;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.ShellListener;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.taranix.cafe.beans.CafeBeansFactory;
import org.taranix.cafe.beans.reflection.CafeReflectionUtils;
import org.taranix.cafe.desktop.annotations.CafeMenuItemSelectionHandler;
import org.taranix.cafe.desktop.annotations.CafeShellHandler;
import org.taranix.cafe.desktop.annotations.ShellHandlerType;
import org.taranix.cafe.desktop.components_old.forms.WidgetConfig;

import java.util.HashMap;
import java.util.Map;

@Slf4j
/**
 * Initialize in CafeDesktopApplication.class.
 * Cannot be initialized by Factory or Service - class is required during resolving classes
 */
public class HandlersService {

    private final Map<String, HandlerSignature> menuItemSelectionHandlers;

    private final CafeBeansFactory beansFactory;

    private final Map<ShellHandlerType, HandlerSignature> shellHandlers;

    public HandlersService(CafeBeansFactory beansFactory) {
        this.beansFactory = beansFactory;
        menuItemSelectionHandlers = new HashMap<>();
        shellHandlers = new HashMap<>();
    }

    public void add(CafeMenuItemSelectionHandler annotation, HandlerSignature handler) {
        HandlerSignature handler1 = menuItemSelectionHandlers.get(annotation.id());
        if (handler1 == null || annotation.primary()) {
            menuItemSelectionHandlers.put(annotation.id(), handler);
        }
    }

    public void add(CafeShellHandler annotation, HandlerSignature handler) {
        HandlerSignature handler1 = shellHandlers.get(annotation.type());
        if (handler1 == null || annotation.primary()) {
            shellHandlers.put(annotation.type(), handler);
        }
    }

    private void invoke(HandlerSignature handlerSignature, ShellEvent event) {
        if (handlerSignature.handlingMethod().getParameterCount() == 0) {
            //beansFactory.getResolvers().findMethodResolver(CafeMethodInfo)

            CafeReflectionUtils.getMethodValue(handlerSignature.handlingMethod(), handlerSignature.handlerInstance());
        }
        if (handlerSignature.handlingMethod().getParameterCount() == 1 && handlerSignature.handlingMethod().getParameterTypes()[0].equals(ShellEvent.class)) {
            CafeReflectionUtils.getMethodValue(handlerSignature.handlingMethod(), handlerSignature.handlerInstance(), event);
        }
    }

    private void invoke(HandlerSignature handlerSignature, SelectionEvent event) {
        if (handlerSignature.handlingMethod().getParameterCount() == 0) {
            CafeReflectionUtils.getMethodValue(handlerSignature.handlingMethod(), handlerSignature.handlerInstance());
        }
        if (handlerSignature.handlingMethod().getParameterCount() == 1 && handlerSignature.handlingMethod().getParameterTypes()[0].equals(SelectionEvent.class)) {
            CafeReflectionUtils.getMethodValue(handlerSignature.handlingMethod(), handlerSignature.handlerInstance(), event);
        }
    }

    public void bind(Shell shell) {
        shell.addShellListener(new ShellListener() {
            @Override
            public void shellActivated(ShellEvent shellEvent) {

            }

            @Override
            public void shellClosed(ShellEvent shellEvent) {
                HandlerSignature handler = shellHandlers.get(ShellHandlerType.Closed);
                if (handler != null) {
                    invoke(handler, shellEvent);
                }
            }

            @Override
            public void shellDeactivated(ShellEvent shellEvent) {

            }

            @Override
            public void shellDeiconified(ShellEvent shellEvent) {

            }

            @Override
            public void shellIconified(ShellEvent shellEvent) {

            }
        });
    }

    public void bind(MenuItem menuItem) {
        String id = WidgetConfig.getWidgetId(menuItem);
        if (id == null) {
            log.warn("No id for widget {}", menuItem);
            return;
        }

        HandlerSignature handler = menuItemSelectionHandlers.get(id);
        if (handler != null) {
            menuItem.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    invoke(handler, e);
                }
            });
        }
    }
}
