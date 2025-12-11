package org.taranix.cafe.desktop.components_old.editors;

import com.google.common.base.Objects;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;
import org.taranix.cafe.desktop.components_old.events.SWTEventType;
import org.taranix.cafe.desktop.components_old.events.annotations.CafeEventHandler;
import org.taranix.cafe.desktop.components_old.forms.text.TextForm;
import org.taranix.cafe.desktop.widgets.MessageBoxService;

//@CafeService(scope = Scope.Prototype)
class TextEditor extends AbstractEditor<String> {

    private final TextForm textForm;
    private String initData;

    TextEditor(TextForm textForm, MessageBoxService messageBoxService) {
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
    @CafeEventHandler(eventType = SWTEventType.Modify)
    protected void onTextChanged(Event event) {
        sendUpdate();
    }

    @Override
    public boolean isDirty() {
        return !Objects.equal(initData, text().getText());
    }

    @Override
    public void save() {
        if (getDataSource().write(text().getText())) {
            read();
        }
    }

    @Override
    public void read() {
        initData = getDataSource().read();
        text().setText(initData);
        if (getDataSource().getName() != null) {
            sendUpdate();
        }
    }
}
