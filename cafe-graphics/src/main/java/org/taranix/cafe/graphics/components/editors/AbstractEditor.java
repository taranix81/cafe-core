package org.taranix.cafe.graphics.components.editors;

import org.taranix.cafe.graphics.components.viewers.AbstractDataView;
import org.taranix.cafe.graphics.services.MessageBoxService;


public abstract class AbstractEditor extends AbstractDataView implements Editor {
    private final MessageBoxService messageBoxService;


    protected AbstractEditor(MessageBoxService messageBoxService) {
        this.messageBoxService = messageBoxService;
    }


    public void dispose() {
        save();
        super.dispose();
    }

//    private void saveDataBeforeDispose() {
//        if (getDataProvider() != null) {
//            if (isDirty() && (messageBoxService.showYesNoDialog(((Control) getWidget()).getShell(), "Do you want to save?", "Modified data"))) {
//                save();
//
//            }
//        }
//    }

//    protected void sendUpdate() {
//        UpdateComponentCafeOldEvent message = new UpdateComponentCafeOldEvent();
//        message.setSource(getId());
//        message.setTarget(getParentId());
//        message.setName(getDataProvider().getName());
//        message.setDirty(isDirty());
//        sendMessage(message).setProcessed();
//    }


}
