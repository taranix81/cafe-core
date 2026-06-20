package org.taranix.cafe.desktop.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Widget;

import java.util.function.Consumer;

public class SpinnerWidgetWrapper implements CafeWidgetWrapper<Integer> {

    private final Spinner widget;

    public SpinnerWidgetWrapper(Composite parent) {
        this.widget = new Spinner(parent, SWT.BORDER);
    }

    public SpinnerWidgetWrapper(Composite parent, int style) {
        this.widget = new Spinner(parent, style);
    }

    public SpinnerWidgetWrapper(Spinner existing) {
        this.widget = existing;
    }

    @Override
    public Widget getWidget() { return widget; }

    @Override
    public Integer getValue() { return widget.getSelection(); }

    @Override
    public void setValue(Integer value) { widget.setSelection(value != null ? value : 0); }

    @Override
    public void addChangeListener(Consumer<Integer> listener) {
        widget.addModifyListener(e -> listener.accept(widget.getSelection()));
    }

    @Override
    public boolean isReadOnly() { return (widget.getStyle() & SWT.READ_ONLY) != 0; }
}
