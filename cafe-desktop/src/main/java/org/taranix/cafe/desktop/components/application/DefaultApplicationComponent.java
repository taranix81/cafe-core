package org.taranix.cafe.desktop.components.application;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;
import org.taranix.cafe.beans.annotations.classes.CafeSingleton;
import org.taranix.cafe.beans.annotations.fields.CafeInject;
import org.taranix.cafe.beans.annotations.fields.CafeProperty;
import org.taranix.cafe.beans.annotations.methods.CafeHandler;
import org.taranix.cafe.beans.events.EventHub;
import org.taranix.cafe.desktop.components.Component;
import org.taranix.cafe.desktop.components.ComponentFactory;
import org.taranix.cafe.desktop.components.application.extensions.ApplicationComponentConfigure;
import org.taranix.cafe.desktop.components.menu.MenuBarComponent;
import org.taranix.cafe.desktop.events.CafeMenuEvent;
import org.taranix.cafe.desktop.widgets.MessageBoxService;

import java.util.Arrays;
import java.util.Optional;

import static org.taranix.cafe.desktop.components.ComponentFactory.COMPONENT;


@Slf4j
@CafeSingleton
final class DefaultApplicationComponent implements ApplicationComponent {

    @CafeProperty(name = "cafe.application.name")
    private String applicationTitle;

    @CafeInject
    private EventHub eventHub;

    @CafeInject
    private ComponentFactory componentFactory;

    @CafeInject
    private Optional<ApplicationComponentConfigure> applicationComponentConfigure;

    @CafeInject
    private MessageBoxService messageBoxService;

    private Shell shell;

    // ── ApplicationComponent ─────────────────────────────────────────


    @CafeHandler
    void onMenuItem(CafeMenuEvent event) {
        String menuId = event.menuId();
        if ("file.exit".equals(menuId)) {
            event.getOrigin().doit = onClose();
            return;
        }
        routeToActiveComponent(event);
    }

    private boolean onClose() {
        boolean result = messageBoxService.showYesNoDialog(shell, "Do you want to exit", "Quit");
        if (result) {
            shutDown();
        }
        return result;
    }

    private void routeToActiveComponent(CafeMenuEvent event) {
        Component activeComponent = getActiveComponent();
        if (activeComponent != null) {
            eventHub.send(event, activeComponent);
        }
    }

    @Override
    public void start() {
        shell = getOrCreateShell();
        shell.open();
        while (!shell.isDisposed()) {
            if (!shell.getDisplay().readAndDispatch()) {
                shell.getDisplay().sleep();
            }
        }
    }

    @Override
    public void shutDown() {
        if (shell != null) {
            Arrays.stream(shell.getChildren()).forEach(Widget::dispose);
            if (!shell.isDisposed()) {
                shell.dispose();
            }
        }
    }


    @Override
    public Component getActiveComponent() {
        Control activeControl = Arrays.stream(shell.getChildren())
                .filter(Control::isFocusControl)
                .findFirst()
                .orElse(null);

        if (activeControl != null) {
            Object storedComponent = activeControl.getData(COMPONENT);
            if (storedComponent instanceof Component component) {
                return component;
            }
        }
        return null;
    }


    @Override
    public void dispose() {

    }


    // ── Internals ────────────────────────────────────────────────────

    private Shell getOrCreateShell() {
        if (shell == null) {
            shell = new Shell(Display.getDefault());
            if (applicationTitle != null) {
                shell.setText(applicationTitle);
            }
            shell.setLayout(new FillLayout());

            componentFactory.create(MenuBarComponent.class, shell);
            applicationComponentConfigure.ifPresent(config ->
            {
                config.configure(shell);
                config.configure(this);
            });

            shell.addShellListener(new ShellAdapter() {
                @Override
                public void shellClosed(ShellEvent e) {
                    e.doit = onClose();
                }
            });
        }
        return shell;
    }
}
