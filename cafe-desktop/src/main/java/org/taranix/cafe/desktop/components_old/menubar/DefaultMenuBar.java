package org.taranix.cafe.desktop.components_old.menubar;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Widget;
import org.taranix.cafe.desktop.components.ComponentFactory;
import org.taranix.cafe.desktop.components_old.events.SWTEventType;
import org.taranix.cafe.desktop.components_old.events.annotations.CafeEventHandler;
import org.taranix.cafe.desktop.components_old.events.messages.application.ApplicationCloseMessage;
import org.taranix.cafe.desktop.components_old.forms.menubar.MenuBarForm;
import org.taranix.cafe.desktop.widgets.MessageBoxService;


//@CafeViewComponent
//@CafeService
@Slf4j
class DefaultMenuBar extends MenuBar {
    @Getter(AccessLevel.PROTECTED)
    private final MessageBoxService messageBoxService;
    private final MenuBarForm menuBarForm;
    //private final ComponentFactory componentFactory;


    DefaultMenuBar(MenuBarForm menuBarForm, ComponentFactory componentFactory, MessageBoxService messageBoxService) {
        this.messageBoxService = messageBoxService;
        this.menuBarForm = menuBarForm;
        //   this.componentFactory = componentFactory;
    }

    @CafeEventHandler(eventType = SWTEventType.Selection, widgetId = "closeId")
    protected void onCloseEvent(Event event) {
        log.debug("Closing app");
        ApplicationCloseMessage message = new ApplicationCloseMessage();
        message.setSource(getId());
        sendMessage(message);
    }


    @CafeEventHandler(eventType = SWTEventType.Selection, widgetId = "menu.file.new")
    private void onNewFileMenu(Event event) {
//        FileDataSource fileDataSource = componentFactory.getOrCreate(FileDataSource.class);
//        AbstractEditor<String> editor = componentFactory.getOrCreate(AbstractEditor.class, String.class);
//        editor.setDataSource(fileDataSource);
//
//        OpenComponentMessage message = new OpenComponentMessage();
//        message.setSource(getId());
//        message.setComponent(editor);
//        sendMessage(message).setProcessed();
    }

    @Override
    protected Widget createWidget(Widget parent) {
        return null;
    }

//    @CafeEventHandler(eventType = SWTEventType.Selection, widgetId = "menu.file.open")
//    private void onOpenMenu(Event event) {
//        Path path = getMessageBoxService().showOpenFileDialog(((Menu) getWidget()).getShell());
//
//        if (path != null) {
//            FileDataSource fileDataSource = componentFactory.getOrCreate(FileDataSource.class);
//            fileDataSource.setPath(path);
//            ViewComponent viewComponent = null;
//
//            if (path.getFileName().toString().endsWith(".csv")) {
//                TableConfigDataSource tableConfigDataSource = componentFactory.getOrCreate(TableConfigDataSource.class);
//                tableConfigDataSource.setStringDataSource(fileDataSource);
//                TableViewer<TableModel> viewer = componentFactory.getOrCreate(TableViewer.class, TableModel.class);
//                viewer.setDataSource(tableConfigDataSource);
//                viewComponent = viewer;
//
//            } else {
//                AbstractEditor<String> editor = componentFactory.getOrCreate(AbstractEditor.class, String.class);
//                editor.setDataSource(fileDataSource);
//                viewComponent = editor;
//            }
//
//            OpenComponentMessage message = new OpenComponentMessage();
//            message.setSource(getId());
//            message.setComponent(viewComponent);
//            sendMessage(message).setProcessed();
//        }
//    }

    //menu.file.save
//    @CafeEventHandler(eventType = SWTEventType.Selection, widgetId = "menu.file.save")
//    private void onSaveFile() {
//        SaveComponentMessage message = new SaveComponentMessage();
//        message.setSource(getId());
//        sendMessage(message).setProcessed();
//    }
//
//    @Override
//    public Widget createWidget(Widget parent) {
//        return menuBarForm.create(parent);
//    }


}
