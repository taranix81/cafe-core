package org.taranix.cafe.desktop.components_old.editors;

import org.eclipse.swt.widgets.Control;
import org.taranix.cafe.desktop.components_old.Component;
import org.taranix.cafe.desktop.components_old.ViewComponent;
import org.taranix.cafe.desktop.components_old.events.messages.components.UpdateComponentMessage;
import org.taranix.cafe.desktop.components_old.viewers.AbstractViewer;
import org.taranix.cafe.desktop.widgets.MessageBoxService;


public abstract class AbstractEditor<TData> extends AbstractViewer<TData> implements Editor<TData>, ViewComponent, Component {
    private final MessageBoxService messageBoxService;


    protected AbstractEditor(MessageBoxService messageBoxService) {
        this.messageBoxService = messageBoxService;
    }


    public void dispose() {
        saveDataBeforeDispose();
        super.dispose();
    }

    private void saveDataBeforeDispose() {
        if (getDataSource() != null) {
            if (isDirty()) {
                if (messageBoxService.showYesNoDialog(((Control) getWidget()).getShell(), "Do you want to save?", "Modified data")) {
                    save();
                }
            }
        }
    }

    protected void sendUpdate() {
        UpdateComponentMessage message = new UpdateComponentMessage();
        message.setSource(getId());
        message.setTarget(getParentId());
        message.setName(getDataSource().getName());
        message.setDirty(isDirty());
        sendMessage(message).setProcessed();
    }


}
