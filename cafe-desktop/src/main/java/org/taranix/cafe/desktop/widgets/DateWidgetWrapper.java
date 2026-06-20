package org.taranix.cafe.desktop.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Widget;

import java.time.LocalDate;
import java.util.function.Consumer;

public class DateWidgetWrapper implements CafeWidgetWrapper<LocalDate> {

    private final DateTime widget;

    public DateWidgetWrapper(Composite parent) {
        this.widget = new DateTime(parent, SWT.DATE | SWT.BORDER);
    }

    public DateWidgetWrapper(DateTime existing) {
        this.widget = existing;
    }

    @Override
    public Widget getWidget() { return widget; }

    @Override
    public LocalDate getValue() {
        return LocalDate.of(widget.getYear(), widget.getMonth() + 1, widget.getDay());
    }

    @Override
    public void setValue(LocalDate value) {
        if (value == null) return;
        widget.setDate(value.getYear(), value.getMonthValue() - 1, value.getDayOfMonth());
    }

    @Override
    public void addChangeListener(Consumer<LocalDate> listener) {
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
