package org.taranix.cafe.graphics.components.menubar;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Widget;
import org.taranix.cafe.beans.annotations.CafeService;
import org.taranix.cafe.graphics.components.containers.ContainerEvent;
import org.taranix.cafe.graphics.components.editors.FileEditor;
import org.taranix.cafe.graphics.events.CafeEventType;
import org.taranix.cafe.graphics.events.SWTEventType;
import org.taranix.cafe.graphics.events.annotations.SwtEventHandler;
import org.taranix.cafe.graphics.events.messages.application.ApplicationCloseCafeOldEvent;
import org.taranix.cafe.graphics.events.messages.components.OpenComponentCafeOldEvent;
import org.taranix.cafe.graphics.events.messages.components.SaveActiveComponentCafeOldEvent;
import org.taranix.cafe.graphics.forms.menubar.MenuBarForm;
import org.taranix.cafe.graphics.services.ComponentFactory;
import org.taranix.cafe.graphics.services.MessageBoxService;

import java.nio.file.Path;


//@CafeViewComponent
@CafeService
@Slf4j
class DefaultMenuBar extends MenuBar {
    @Getter(AccessLevel.PROTECTED)
    private final MessageBoxService messageBoxService;
    private final MenuBarForm menuBarForm;
    private final ComponentFactory componentFactory;


    DefaultMenuBar(MenuBarForm menuBarForm, ComponentFactory componentFactory, MessageBoxService messageBoxService) {
        this.messageBoxService = messageBoxService;
        this.menuBarForm = menuBarForm;
        this.componentFactory = componentFactory;
    }

    @SwtEventHandler(eventType = SWTEventType.Selection, widgetId = "closeId")
    protected void onCloseEvent(Event event) {
        log.debug("Closing app");
        ApplicationCloseCafeOldEvent message = new ApplicationCloseCafeOldEvent();
        message.setSource(getId());
        sendMessage(message);
    }


    @SwtEventHandler(eventType = SWTEventType.Selection, widgetId = "menu.file.new")
    private void onNewFileMenu(Event event) {
        //FileDataProvider fileDataSource = componentFactory.getOrCreate(FileDataProvider.class);
        FileEditor editor = componentFactory.getOrCreate(FileEditor.class);

        OpenComponentCafeOldEvent message = new OpenComponentCafeOldEvent();
        message.setSource(getId());
        message.setComponent(editor);
        sendMessage(message).setProcessed();
    }

    @SwtEventHandler(eventType = SWTEventType.Selection, widgetId = "menu.file.open")
    private void onOpenMenu(Event event) {
        Path path = getMessageBoxService().showOpenFileDialog(((Menu) getWidget()).getShell());

        if (path != null) {
            FileEditor editor = componentFactory.getOrCreate(FileEditor.class);
            editor.setFile(path.toFile());
            sendMessage(new ContainerEvent(CafeEventType.Open));
        }
    }


    @SwtEventHandler(eventType = SWTEventType.Selection, widgetId = "menu.file.save")
    private void onSaveFile() {
        SaveActiveComponentCafeOldEvent message = new SaveActiveComponentCafeOldEvent();
        message.setSource(getId());
        sendMessage(message).setProcessed();
    }

    @Override
    public Widget createWidget(Widget parent) {
        return menuBarForm.create(parent);
    }


}
