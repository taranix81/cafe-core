package org.taranix.cafe.graphics.components.application;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;
import org.taranix.cafe.beans.annotations.CafeInject;
import org.taranix.cafe.beans.annotations.CafeOptional;
import org.taranix.cafe.beans.annotations.CafeProperty;
import org.taranix.cafe.beans.annotations.CafeService;
import org.taranix.cafe.graphics.components.AbstractView;
import org.taranix.cafe.graphics.components.ApplicationComponent;
import org.taranix.cafe.graphics.components.Component;
import org.taranix.cafe.graphics.components.View;
import org.taranix.cafe.graphics.components.containers.AbstractViewContainer;
import org.taranix.cafe.graphics.components.menubar.MenuBar;
import org.taranix.cafe.graphics.events.EventBus;
import org.taranix.cafe.graphics.events.SWTEventType;
import org.taranix.cafe.graphics.events.annotations.CafeEventHandler;
import org.taranix.cafe.graphics.events.annotations.SwtEventHandler;
import org.taranix.cafe.graphics.events.messages.CafeOldEvent;
import org.taranix.cafe.graphics.events.messages.application.ApplicationCloseCafeOldEvent;
import org.taranix.cafe.graphics.events.messages.application.ApplicationShutdownCafeOldEvent;
import org.taranix.cafe.graphics.forms.shell.ShellForm;
import org.taranix.cafe.graphics.services.MessageBoxService;


@CafeService
@Slf4j
class DefaultApplication extends AbstractView implements ApplicationComponent {

    private final ShellForm form;
    @CafeInject
    private MessageBoxService messageBoxService;
    @CafeProperty(name = "cafe.application.promptOnQuit")
    private Boolean promptOnQuit;

    @Getter
    @CafeProperty(name = "cafe.application.name")
    private String applicationTitle;
    private Shell shell;

    @CafeInject
    @CafeOptional
    private MenuBar menuBarViewComponent;
    @CafeInject
    @CafeOptional
    private AbstractViewContainer abstractViewContainer;
    @CafeInject
    private EventBus eventBus;

    DefaultApplication(ShellForm form) {
        this.form = form;
    }


    @SwtEventHandler(eventType = SWTEventType.Close)
    private void onShellClosing(org.eclipse.swt.widgets.Event event) {
        if (!canClose()) {
            event.doit = false;
            return;
        }
        ApplicationShutdownCafeOldEvent message = new ApplicationShutdownCafeOldEvent();
        message.setTarget(getParentId());
        CafeOldEvent m = sendMessage(message);
        m.setProcessed();
    }


    @CafeEventHandler
    private void onClosing(ApplicationCloseCafeOldEvent message) {
        if (canClose()) {
            ApplicationShutdownCafeOldEvent shutdownMessage = new ApplicationShutdownCafeOldEvent();
            shutdownMessage.setTarget(getParentId());
            sendMessage(shutdownMessage).setProcessed();
            shell.dispose();
        }
    }

    @CafeEventHandler
    void onCloseComponents(ApplicationShutdownCafeOldEvent message) {
        log.debug("Closing components");
        if (abstractViewContainer != null) {
            abstractViewContainer.dispose();
        }

        menuBarViewComponent.dispose();
    }

    private boolean canClose() {
        return !promptOnQuit || messageBoxService.showYesNoDialog(shell, "Do you want to quit?", "Closing application");
    }


    @Override
    public Widget getWidget() {
        return shell;
    }

    @Override
    protected Widget createWidget(Widget parent) {
        shell = form.create(null);
        shell.setText(applicationTitle);
        return shell;
    }


    @Override
    public void start() {
        init(null);
        getEventBus().register(this);

        if (menuBarViewComponent != null) {
            initialize(menuBarViewComponent);
        }

        if (abstractViewContainer != null) {
            initialize(abstractViewContainer);
        }

        runInShell(shell);
    }

    private void initialize(Component component) {
        component.setParentId(this.getId());
        if (component instanceof View view) {
            view.init(this.getWidget());
        }
        component.setParentId(this.getId());
        getEventBus().register(component);
    }

    private void runInShell(Shell shell) {
        shell.open();
        while (!shell.isDisposed()) {
            if (!shell.getDisplay().readAndDispatch()) {
                shell.getDisplay().sleep();
            }
        }
    }

}
