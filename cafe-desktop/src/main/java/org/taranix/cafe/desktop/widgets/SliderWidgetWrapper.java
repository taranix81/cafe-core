package org.taranix.cafe.desktop.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Slider;
import org.eclipse.swt.widgets.Widget;

import java.util.function.Consumer;

public class SliderWidgetWrapper implements CafeWidgetWrapper<Integer> {

    private final Slider widget;

    public SliderWidgetWrapper(Composite parent) {
        this.widget = new Slider(parent, SWT.HORIZONTAL);
    }

    public SliderWidgetWrapper(Composite parent, int style) {
        this.widget = new Slider(parent, style);
    }

    public SliderWidgetWrapper(Slider existing) {
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
