package org.taranix.cafe.desktop.components.application;

import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;
import org.taranix.cafe.beans.annotations.fields.CafeInject;
import org.taranix.cafe.beans.annotations.fields.CafeProperty;
import org.taranix.cafe.beans.annotations.classes.CafeSingleton;
import org.taranix.cafe.desktop.components.containers.CafeComponentRegistry;
import org.taranix.cafe.desktop.components.containers.ContainerComponent;
import org.taranix.cafe.desktop.components.containers.OpenComponent;
import org.taranix.cafe.desktop.menu.MenuRegistry;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@CafeSingleton
final class DefaultApplicationComponent implements ApplicationComponent {

    @CafeProperty(name = "cafe.application.name")
    private String applicationTitle;

    @CafeInject
    private Optional<ShellLayout> shellLayout;

    @CafeInject
    private Optional<ApplicationShellConfiguration> shellConfiguration;

    @CafeInject
    private Optional<CafeComponentRegistry> componentRegistry;

    @CafeInject
    private Optional<MenuRegistry> menuRegistry;

    private Shell shell;

    @Override
    public void start() {
        show();
        while (!shell.isDisposed()) {
            if (!shell.getDisplay().readAndDispatch()) {
                shell.getDisplay().sleep();
            }
        }
    }

    @Override
    public void shutDown() {
        if (shell != null && !shell.isDisposed()) {
            shell.close();
        }
    }

    @Override
    public void show() {
        getOrCreateShell().open();
    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
        if (shell != null && !shell.isDisposed()) {
            shell.dispose();
        }
    }

    @Override
    public Widget create(Control parent) {
        throw new UnsupportedOperationException("Use start() to run the application");
    }

    @Override
    public void addComponent(Class<?> componentType) {
        componentRegistry.ifPresent(r -> r.open(componentType, null));
    }

    @Override
    public void addComponent(Class<?> componentType, String sourceId) {
        componentRegistry.ifPresent(r -> r.open(componentType, sourceId));
    }

    @Override
    public void removeComponent(UUID componentId) {
        componentRegistry.ifPresent(r -> r.close(componentId));
    }

    @Override
    public boolean isOpen(String sourceId) {
        return componentRegistry.map(r -> r.isOpen(sourceId)).orElse(false);
    }

    @Override
    public void activate(String sourceId) {
        componentRegistry.ifPresent(r -> r.setActive(sourceId));
    }

    @Override
    public void setLogicallyActive(ContainerComponent container) {
        componentRegistry.ifPresent(r -> r.setActive(
                componentRegistry.get().getOpen(container.getClass())
                        .stream().findFirst()
                        .map(OpenComponent::id)
                        .orElse(null)));
    }

    @Override
    public <T> List<T> getComponents(Class<T> componentType) {
        return List.of();
    }

    @Override
    public <T> Optional<T> getActiveComponent(Class<T> componentType) {
        return Optional.empty();
    }

    @Override
    public Set<ContainerComponent> getContainers() {
        return Set.of();
    }

    private Shell getOrCreateShell() {
        if (shell == null) {
            shell = new Shell(Display.getDefault());
            if (applicationTitle != null) {
                shell.setText(applicationTitle);
            }
            shell.setLayout(shellLayout
                    .map(ShellLayout::getLayout)
                    .orElse(new FillLayout()));
            menuRegistry.ifPresent(r -> r.buildMenuBar(shell));
            shellConfiguration.ifPresent(c -> c.configure(shell));
        }
        return shell;
    }
}
