package org.taranix.cafe.desktop.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Widget;

import java.util.function.Consumer;

public class ProgressBarWidgetWrapper implements CafeWidgetWrapper<Integer> {

    private final ProgressBar widget;

    public ProgressBarWidgetWrapper(Composite parent) {
        this.widget = new ProgressBar(parent, SWT.HORIZONTAL | SWT.SMOOTH);
    }

    public ProgressBarWidgetWrapper(Composite parent, int style) {
        this.widget = new ProgressBar(parent, style);
    }

    public ProgressBarWidgetWrapper(ProgressBar existing) {
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
        // read-only — no change notifications
    }

    @Override
    public boolean isReadOnly() { return true; }
}
