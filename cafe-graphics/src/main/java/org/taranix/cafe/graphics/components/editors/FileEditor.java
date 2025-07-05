package org.taranix.cafe.graphics.components.editors;

import com.google.common.base.Objects;
import lombok.Getter;
import lombok.Setter;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;
import org.taranix.cafe.beans.annotations.CafeService;
import org.taranix.cafe.beans.annotations.Scope;
import org.taranix.cafe.graphics.components.containers.ContainerEvent;
import org.taranix.cafe.graphics.events.CafeEventType;
import org.taranix.cafe.graphics.events.SWTEventType;
import org.taranix.cafe.graphics.events.annotations.SwtEventHandler;
import org.taranix.cafe.graphics.forms.text.TextForm;
import org.taranix.cafe.graphics.services.MessageBoxService;
import org.taranix.cafe.graphics.services.file.FileServiceEvent;

import java.io.File;

@CafeService(scope = Scope.Prototype)
public
class FileEditor extends AbstractEditor {

    private final TextForm textForm;
    private String initData;

    @Getter
    @Setter
    private File file;


    FileEditor(TextForm textForm, MessageBoxService messageBoxService) {
        super(messageBoxService);
        this.textForm = textForm;
    }


    @Override
    public Widget createWidget(Widget parent) {
        return textForm.create(parent);
    }

    private Text text() {
        return (Text) getWidget();
    }

    /*
     SWT Event handlers
     */
    //Inform parent that Text has been changed
    @SwtEventHandler(eventType = SWTEventType.Modify)
    protected void onTextChanged(Event event) {
        sendMessage(new ContainerEvent(CafeEventType.Refresh));
    }


    @Override
    public boolean isDirty() {
        return !Objects.equal(initData, text().getText());
    }

    @Override
    public void save() {
        sendMessage(FileServiceEvent.write(file, text().getText()));
        sendMessage(new ContainerEvent(CafeEventType.Refresh));
    }


    @Override
    public void read() {
        FileServiceEvent event = (FileServiceEvent) sendMessage(FileServiceEvent.read(file));
        initData = event.getData();
    }


}
