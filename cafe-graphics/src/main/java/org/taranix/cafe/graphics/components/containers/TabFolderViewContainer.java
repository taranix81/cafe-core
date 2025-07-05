package org.taranix.cafe.graphics.components.containers;


import lombok.extern.slf4j.Slf4j;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Widget;
import org.taranix.cafe.beans.annotations.CafeService;
import org.taranix.cafe.graphics.components.Component;
import org.taranix.cafe.graphics.components.View;
import org.taranix.cafe.graphics.components.editors.Editor;
import org.taranix.cafe.graphics.events.SWTEventType;
import org.taranix.cafe.graphics.events.annotations.CafeEventHandler;
import org.taranix.cafe.graphics.events.annotations.SwtEventHandler;
import org.taranix.cafe.graphics.events.messages.components.OpenComponentCafeOldEvent;
import org.taranix.cafe.graphics.events.messages.components.SaveActiveComponentCafeOldEvent;
import org.taranix.cafe.graphics.events.messages.components.UpdateComponentCafeOldEvent;
import org.taranix.cafe.graphics.forms.WidgetConfig;
import org.taranix.cafe.graphics.forms.tabfolder.TabFolderForm;
import org.taranix.cafe.graphics.forms.tabfolder.TabItemForm;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@CafeService
class TabFolderViewContainer extends AbstractViewContainer implements Container {


    private final TabFolderForm tabFolderForm;
    private final TabItemForm tabItemForm;


    protected TabFolderViewContainer(TabFolderForm tabFolderForm, TabItemForm tabItemForm) {
        this.tabFolderForm = tabFolderForm;
        this.tabItemForm = tabItemForm;
    }

    private CTabFolder tabFolder() {
        return (CTabFolder) getWidget();
    }

    @Override
    public Widget createWidget(Widget parent) {
        return tabFolderForm.create(parent);
    }

    @Override
    protected void addSubComponent(Component subComponent) {
        super.addSubComponent(subComponent);
        placeComponentInContainer(subComponent);
        getEventBus().refresh(this);
        subComponent.postInit();
    }


    @SwtEventHandler(eventType = SWTEventType.Dispose)
    private void onTabItemDispose(Event event) {
        Widget widget = event.widget;
        if (widget instanceof CTabItem tabItem) {
            Control tabItemWidget = tabItem.getControl();
            WidgetConfig.getComponent(tabItemWidget).dispose();
        }
    }

    @CafeEventHandler
    private void onComponentUpdate(UpdateComponentCafeOldEvent message) {
        findItemByComponentId(message.getSource())
                .ifPresent(tabItem -> {
                    String caption = message.isDirty() ? message.getName() + " *" : message.getName();
                    if (!caption.equals(tabItem.getText())) {
                        tabItem.setText(caption);
                    }
                });
    }

    private Optional<CTabItem> findItemByComponentId(UUID source) {
        for (CTabItem item : tabFolder().getItems()) {
            Component component = WidgetConfig.getComponent(item.getControl());
            if (component.getId().equals(source)) {
                return Optional.of(item);
            }
        }
        return Optional.empty();
    }

    private Component activeComponent() {
        return Optional.ofNullable(tabFolder().getSelection())
                .map(CTabItem::getControl)
                .map(WidgetConfig::getComponent)
                .orElse(null);


    }

    private void placeComponentInContainer(Component subComponent) {
        if (subComponent instanceof View view) {
            CTabItem tabItem = tabItemForm.create(getWidget());
            tabItem.setControl((Control) view.getWidget());
            tabFolder().setSelection(tabItem);
        }
    }

    @Override
    public void dispose() {
        for (CTabItem tabItem : tabFolder().getItems()) {
            tabItem.dispose();
        }
        super.dispose();
    }

    @CafeEventHandler
    protected void onOpenComponent(OpenComponentCafeOldEvent message) {
        View subComponent = message.getComponent();
        addSubComponent(subComponent);
    }

    @CafeEventHandler
    protected void saveComponent(SaveActiveComponentCafeOldEvent message) {
        Optional.ofNullable(activeComponent())
                .filter(Editor.class::isInstance)
                .map(Editor.class::cast)
                .ifPresent(Editor::save);
    }

}
