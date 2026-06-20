package org.taranix.cafe.desktop.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;

import java.util.function.Consumer;

public class TextWidgetWrapper implements CafeWidgetWrapper<String> {

    private final Text widget;

    public TextWidgetWrapper(Composite parent) {
        this.widget = new Text(parent, SWT.BORDER);
    }

    public TextWidgetWrapper(Composite parent, int style) {
        this.widget = new Text(parent, style);
    }

    public TextWidgetWrapper(Text existing) {
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
