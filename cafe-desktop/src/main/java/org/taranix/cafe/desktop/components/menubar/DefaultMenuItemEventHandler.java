package org.taranix.cafe.desktop.components.menubar;

import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Display;
import org.taranix.cafe.beans.annotations.CafeService;
import org.taranix.cafe.desktop.annotations.CafeMenuItemSelectionHandler;
import org.taranix.cafe.desktop.components.application.ApplicationComponent;
import org.taranix.cafe.desktop.widgets.MessageBoxService;

@CafeService
class DefaultMenuItemEventHandler {

    private final ApplicationComponent applicationComponent;

    private final MessageBoxService messageBoxService;

    DefaultMenuItemEventHandler(ApplicationComponent applicationComponent, MessageBoxService messageBoxService) {
        this.applicationComponent = applicationComponent;
        this.messageBoxService = messageBoxService;
    }

    @CafeMenuItemSelectionHandler(id = "menu.file.quit")
    void handleQuit(SelectionEvent event) {
        applicationComponent.shutDown();
    }

    @CafeMenuItemSelectionHandler(id = "menu.file.open")
    void handleOpen(SelectionEvent event) {
        messageBoxService.showWarningDialog(Display.getCurrent().getActiveShell(), "menu.file.open", "Not implemented yet");
    }

    @CafeMenuItemSelectionHandler(id = "menu.file.new")
    void handleNew(SelectionEvent event) {
        messageBoxService.showWarningDialog(Display.getCurrent().getActiveShell(), "menu.file.new", "Not implemented yet");
    }

    @CafeMenuItemSelectionHandler(id = "menu.file.save")
    void handleSave(SelectionEvent event) {
        messageBoxService.showWarningDialog(Display.getCurrent().getActiveShell(), "menu.file.save", "Not implemented yet");
    }

    @CafeMenuItemSelectionHandler(id = "menu.file.save.all")
    void handleSaveAll(SelectionEvent event) {
        messageBoxService.showWarningDialog(Display.getCurrent().getActiveShell(), "menu.file.save.all", "Not implemented yet");
    }

    @CafeMenuItemSelectionHandler(id = "menu.file.save.as")
    void handleSaveAs(SelectionEvent event) {
        messageBoxService.showWarningDialog(Display.getCurrent().getActiveShell(), "menu.file.save.as", "Not implemented yet");
    }

    @CafeMenuItemSelectionHandler(id = "menu.file.properties")
    void handleProperties(SelectionEvent event) {
        messageBoxService.showWarningDialog(Display.getCurrent().getActiveShell(), "menu.file.properties", "Not implemented yet");
    }
}
