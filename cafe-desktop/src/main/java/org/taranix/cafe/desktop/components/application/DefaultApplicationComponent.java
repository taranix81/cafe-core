package org.taranix.cafe.desktop.components.application;

import lombok.Getter;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.*;
import org.taranix.cafe.beans.annotations.fields.CafeInject;
import org.taranix.cafe.beans.annotations.modifiers.CafeOptional;
import org.taranix.cafe.beans.annotations.fields.CafeProperty;
import org.taranix.cafe.beans.annotations.classes.CafeService;
import org.taranix.cafe.desktop.actions.HandlersService;
import org.taranix.cafe.desktop.components.Component;
import org.taranix.cafe.desktop.components.containers.ContainerComponent;
import org.taranix.cafe.desktop.components.menubar.ApplicationMenuBarComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@CafeService
final class DefaultApplicationComponent implements ApplicationComponent {

    private final HandlersService handlersService;
    private Shell shell;
    private List<Component> componentList = new ArrayList<>();
    @Getter
    @CafeProperty(name = "cafe.application.name")
    private String applicationTitle;
    @CafeInject
    @CafeOptional
    private ShellLayout shellLayout;

    DefaultApplicationComponent(HandlersService handlersService) {
        this.handlersService = handlersService;
    }

    @Override
    public void start() {
        getShell().open();
        while (!getShell().isDisposed()) {
            if (!getShell().getDisplay().readAndDispatch()) {
                getShell().getDisplay().sleep();
            }
        }
    }

    @Override
    public void shutDown() {
        if (!getShell().isDisposed()) {
            getShell().close();
        }
    }

    @Override
    public ContainerComponent getActiveContainer() {
        return null;
    }

    @Override
    public Set<ContainerComponent> getContainers() {
        return null;
    }

    @Override
    public <T extends Component> Set<T> getComponent(Class<T> componentType) {
        return null;
    }

    @Override
    public void addComponent(Component component) {
        if (componentList.contains(component)) {
            return;
        }
        componentList.add(component);
        Widget widget = component.create(getShell());
        if (component instanceof ApplicationMenuBarComponent && widget instanceof Menu menu) {
            getShell().setMenuBar(menu);
        }
    }

    @Override
    public boolean removeComponent(Component component) {
        return componentList.remove(component);
    }

    @Override
    public Component selected() {
        return null;
    }

    private Shell getShell() {
        if (shell == null) {
            shell = new Shell(Display.getCurrent());
            shell.setText(applicationTitle);
            shell.setLayout(Optional.ofNullable(shellLayout)
                    .map(ShellLayout::getLayout)
                    .orElse(new FillLayout())
            );
            handlersService.bind(shell);
        }
        return shell;
    }

    @Override
    public Widget create(Control parent) {
        throw new RuntimeException("Don't use the method to create a Shell");
    }
}
