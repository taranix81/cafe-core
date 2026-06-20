package org.taranix.cafe.desktop.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Widget;

import java.util.function.Consumer;

public class RadioButtonWidgetWrapper implements CafeWidgetWrapper<Boolean> {

    private final Button widget;

    public RadioButtonWidgetWrapper(Composite parent) {
        this.widget = new Button(parent, SWT.RADIO);
    }

    public RadioButtonWidgetWrapper(Button existing) {
        this.widget = existing;
    }

    @Override
    public Widget getWidget() { return widget; }

    @Override
    public Boolean getValue() { return widget.getSelection(); }

    @Override
    public void setValue(Boolean value) { widget.setSelection(Boolean.TRUE.equals(value)); }

    @Override
    public void addChangeListener(Consumer<Boolean> listener) {
        widget.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                listener.accept(widget.getSelection());
            }
        });
    }

    @Override
    public boolean isReadOnly() { return false; }
}
