package org.taranix.cafe.desktop.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Widget;

import java.util.function.Consumer;

public class CLabelWidgetWrapper implements CafeWidgetWrapper<String> {

    private final CLabel widget;

    public CLabelWidgetWrapper(Composite parent) {
        this.widget = new CLabel(parent, SWT.NONE);
    }

    public CLabelWidgetWrapper(CLabel existing) {
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
        // read-only — no change notifications
    }

    @Override
    public boolean isReadOnly() { return true; }
}
