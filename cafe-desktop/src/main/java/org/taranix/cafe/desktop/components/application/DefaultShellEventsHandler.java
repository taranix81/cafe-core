package org.taranix.cafe.desktop.components.application;

import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.widgets.Shell;
import org.taranix.cafe.beans.annotations.CafeProperty;
import org.taranix.cafe.beans.annotations.CafeService;
import org.taranix.cafe.desktop.annotations.CafeShellHandler;
import org.taranix.cafe.desktop.annotations.ShellHandlerType;
import org.taranix.cafe.desktop.widgets.MessageBoxService;

@CafeService
class DefaultShellEventsHandler {


    private final MessageBoxService messageBoxService;

    @CafeProperty(name = "cafe.application.shell.promptOnQuit")
    private Boolean promptOnQuit;

    DefaultShellEventsHandler(MessageBoxService messageBoxService) {
        this.messageBoxService = messageBoxService;
    }


    @CafeShellHandler(type = ShellHandlerType.Closed)
    void shellClosed(ShellEvent shellEvent) {
        if (Boolean.TRUE.equals(promptOnQuit)) {
            boolean continueClosing = messageBoxService.showYesNoDialog((Shell) shellEvent.widget, "Do you want to close application", "Application close");
            if (!continueClosing) {
                shellEvent.doit = false;
                return;
            }
        }

//        if (!actionBus.broadcast(ClosingAction.builder().build()).isContinueClosing()) {
//            shellEvent.doit = false;
//            return;
//        }
//        shellEvent.doit = true;
//        actionBus.broadcast(SaveAction.builder().build());
    }

}
