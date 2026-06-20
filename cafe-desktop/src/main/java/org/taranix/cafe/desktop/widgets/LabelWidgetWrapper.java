package org.taranix.cafe.desktop.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Widget;

import java.util.function.Consumer;

public class LabelWidgetWrapper implements CafeWidgetWrapper<String> {

    private final Label widget;

    public LabelWidgetWrapper(Composite parent) {
        this.widget = new Label(parent, SWT.NONE);
    }

    public LabelWidgetWrapper(Label existing) {
        this.widget = existing;
    }

    @Override
    public Widget getWidget() { return widget; }

    @Override
    public String getValue() { return widget.getText(); }

    @Override
    public void setValue(String value) { widget.setText(value != null ? value : ""); }

    @Override
    public void addChangeListener(Consumer<String> listener) {
        // labels are read-only — no change notifications
    }

    @Override
    public boolean isReadOnly() { return true; }
}
