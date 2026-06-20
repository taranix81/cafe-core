package org.taranix.cafe.desktop.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Widget;

import java.util.function.Consumer;

public class ComboWidgetWrapper implements CafeWidgetWrapper<String> {

    private final Combo widget;

    public ComboWidgetWrapper(Composite parent) {
        this.widget = new Combo(parent, SWT.BORDER);
    }

    public ComboWidgetWrapper(Composite parent, int style) {
        this.widget = new Combo(parent, style);
    }

    public ComboWidgetWrapper(Combo existing) {
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
        widget.addModifyListener(e -> listener.accept(widget.getText()));
    }

    @Override
    public boolean isReadOnly() { return (widget.getStyle() & SWT.READ_ONLY) != 0; }
}
