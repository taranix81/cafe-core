package org.taranix.cafe.desktop.components_old.application.containers;


import lombok.extern.slf4j.Slf4j;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Widget;
import org.taranix.cafe.desktop.components_old.Component;
import org.taranix.cafe.desktop.components_old.ViewComponent;
import org.taranix.cafe.desktop.components_old.editors.AbstractEditor;
import org.taranix.cafe.desktop.components_old.events.SWTEventType;
import org.taranix.cafe.desktop.components_old.events.annotations.CafeEventHandler;
import org.taranix.cafe.desktop.components_old.events.annotations.CafeMessageHandler;
import org.taranix.cafe.desktop.components_old.events.messages.components.OpenComponentMessage;
import org.taranix.cafe.desktop.components_old.events.messages.components.SaveComponentMessage;
import org.taranix.cafe.desktop.components_old.events.messages.components.UpdateComponentMessage;
import org.taranix.cafe.desktop.components_old.forms.WidgetConfig;
import org.taranix.cafe.desktop.components_old.forms.tabfolder.TabFolderForm;
import org.taranix.cafe.desktop.components_old.forms.tabfolder.TabItemForm;

import java.util.Optional;
import java.util.UUID;

@Slf4j
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
    public void addSubComponent(Component subComponent) {
        super.addSubComponent(subComponent);
        placeComponentInContainer(subComponent);
        getEventBus().refresh(this);
        subComponent.postInit();
    }

    @CafeEventHandler(eventType = SWTEventType.Dispose)
    private void onTabItemDispose(Event event) {
        Widget widget = event.widget;
        if (widget instanceof CTabItem tabItem) {
            Control tabItemWidget = tabItem.getControl();
            WidgetConfig.getComponent(tabItemWidget).dispose();
        }
    }

    @CafeMessageHandler
    private void onComponentUpdate(UpdateComponentMessage message) {
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
        if (subComponent instanceof ViewComponent viewComponent) {
            CTabItem tabItem = tabItemForm.create(getWidget());
            tabItem.setControl((Control) viewComponent.getWidget());
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

    @CafeMessageHandler
    protected void onOpenComponent(OpenComponentMessage message) {
        ViewComponent subComponent = message.getComponent();
        addSubComponent(subComponent);
    }

    @CafeMessageHandler
    protected void saveComponent(SaveComponentMessage message) {
        Optional.ofNullable(activeComponent())
                .filter(component -> component instanceof AbstractEditor<?>)
                .map(component -> (AbstractEditor<?>) component)
                .ifPresent(AbstractEditor::save);
    }

}
