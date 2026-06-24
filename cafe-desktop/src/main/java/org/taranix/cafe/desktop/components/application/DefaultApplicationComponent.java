package org.taranix.cafe.desktop.components.application;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
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
import java.util.Set;
import java.util.stream.Collectors;

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
        log.trace("Received menu item event :{} ", event.getMenuId());
        String menuId = event.getMenuId();
        if ("file.exit".equals(menuId)) {
            shell.close();
            return;
        }
        routeToActiveComponent(event);
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

    }


    @Override
    public Set<Component> getComponents() {
        if (shell != null && !shell.isDisposed()) {
            return Arrays.stream(shell.getChildren())
                    .map(control -> control.getData(COMPONENT))
                    .filter(Component.class::isInstance)
                    .map(Component.class::cast)
                    .collect(Collectors.toSet());
        }
        return Set.of();
    }

    @Override
    public Component getActiveComponent() {
        Control focused = shell.getDisplay().getFocusControl();
        while (focused != null && !focused.isDisposed()) {
            if (focused.getParent() == shell) {
                Object data = focused.getData(COMPONENT);
                if (data instanceof Component c) {
                    log.trace("Active component : {}", c);
                    return c;
                }
                break;
            }
            focused = focused.getParent();
        }
        return null;
    }


    @Override
    public void dispose() {
        // Nothing to do here
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
                    e.doit = messageBoxService.showYesNoDialog(shell, "Do you want to close ?", "Closing");
                    // Need to call dispose components before Shell entered into Dispose state
                    if (e.doit) {
                        getComponents().forEach(Component::dispose);
                    }
                }
            });
        }
        return shell;
    }
}
