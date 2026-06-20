package org.taranix.cafe.desktop.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Widget;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class TableWidgetWrapper<T> implements CafeWidgetWrapper<List<T>> {

    private final Table widget;
    private final CafeTableRenderer<T> renderer;
    private List<T> items = new ArrayList<>();

    public TableWidgetWrapper(Composite parent, CafeTableRenderer<T> renderer) {
        this.widget = new Table(parent, SWT.BORDER | SWT.FULL_SELECTION);
        this.renderer = renderer;
        renderer.renderColumns(widget);
    }

    public TableWidgetWrapper(Composite parent, int style, CafeTableRenderer<T> renderer) {
        this.widget = new Table(parent, style);
        this.renderer = renderer;
        renderer.renderColumns(widget);
    }

    @Override
    public Widget getWidget() { return widget; }

    @Override
    public List<T> getValue() {
        int[] indices = widget.getSelectionIndices();
        List<T> selected = new ArrayList<>(indices.length);
        for (int i : indices) { if (i < items.size()) selected.add(items.get(i)); }
        return selected;
    }

    @Override
    public void setValue(List<T> value) {
        this.items = value != null ? new ArrayList<>(value) : new ArrayList<>();
        widget.removeAll();
        for (int i = 0; i < items.size(); i++) {
            TableItem item = new TableItem(widget, SWT.NONE);
            renderer.renderItem(item, items.get(i), i);
        }
    }

    @Override
    public void addChangeListener(Consumer<List<T>> listener) {
        widget.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                listener.accept(getValue());
            }
        });
    }

    @Override
    public boolean isReadOnly() { return false; }

    public List<T> getAllItems() { return new ArrayList<>(items); }
}
