package org.taranix.cafe.desktop.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Widget;

import java.time.LocalTime;
import java.util.function.Consumer;

public class TimeWidgetWrapper implements CafeWidgetWrapper<LocalTime> {

    private final DateTime widget;

    public TimeWidgetWrapper(Composite parent) {
        this.widget = new DateTime(parent, SWT.TIME | SWT.BORDER);
    }

    public TimeWidgetWrapper(DateTime existing) {
        this.widget = existing;
    }

    @Override
    public Widget getWidget() { return widget; }

    @Override
    public LocalTime getValue() {
        return LocalTime.of(widget.getHours(), widget.getMinutes(), widget.getSeconds());
    }

    @Override
    public void setValue(LocalTime value) {
        if (value == null) return;
        widget.setTime(value.getHour(), value.getMinute(), value.getSecond());
    }

    @Override
    public void addChangeListener(Consumer<LocalTime> listener) {
        widget.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                listener.accept(getValue());
            }
        });
    }

    @Override
    public boolean isReadOnly() { return false; }
}
