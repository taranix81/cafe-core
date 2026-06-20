package org.taranix.cafe.desktop.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Widget;

import java.util.Arrays;
import java.util.function.Consumer;

public class ListWidgetWrapper implements CafeWidgetWrapper<java.util.List<String>> {

    private final List widget;

    public ListWidgetWrapper(Composite parent) {
        this.widget = new List(parent, SWT.BORDER | SWT.MULTI);
    }

    public ListWidgetWrapper(Composite parent, int style) {
        this.widget = new List(parent, style);
    }

    public ListWidgetWrapper(List existing) {
        this.widget = existing;
    }

    @Override
    public Widget getWidget() { return widget; }

    @Override
    public java.util.List<String> getValue() {
        return Arrays.asList(widget.getSelection());
    }

    @Override
    public void setValue(java.util.List<String> value) {
        widget.deselectAll();
        if (value == null) return;
        String[] items = widget.getItems();
        for (String v : value) {
            for (int i = 0; i < items.length; i++) {
                if (items[i].equals(v)) { widget.select(i); break; }
            }
        }
    }

    @Override
    public void addChangeListener(Consumer<java.util.List<String>> listener) {
        widget.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                listener.accept(Arrays.asList(widget.getSelection()));
            }
        });
    }

    @Override
    public boolean isReadOnly() { return false; }
}
