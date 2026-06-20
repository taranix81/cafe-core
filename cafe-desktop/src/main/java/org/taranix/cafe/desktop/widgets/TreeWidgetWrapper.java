package org.taranix.cafe.desktop.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Widget;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class TreeWidgetWrapper<T> implements CafeWidgetWrapper<List<T>> {

    private final Tree widget;
    private final CafeTreeRenderer<T> renderer;

    public TreeWidgetWrapper(Composite parent, CafeTreeRenderer<T> renderer) {
        this.widget = new Tree(parent, SWT.BORDER);
        this.renderer = renderer;
        renderer.renderColumns(widget);
    }

    public TreeWidgetWrapper(Composite parent, int style, CafeTreeRenderer<T> renderer) {
        this.widget = new Tree(parent, style);
        this.renderer = renderer;
        renderer.renderColumns(widget);
    }

    @Override
    public Widget getWidget() { return widget; }

    @Override
    public List<T> getValue() {
        TreeItem[] selected = widget.getSelection();
        List<T> result = new ArrayList<>(selected.length);
        for (TreeItem item : selected) {
            @SuppressWarnings("unchecked")
            T data = (T) item.getData();
            if (data != null) result.add(data);
        }
        return result;
    }

    @Override
    public void setValue(List<T> values) {
        widget.removeAll();
        if (values == null) return;
        for (T value : values) {
            TreeItem item = new TreeItem(widget, SWT.NONE);
            item.setData(value);
            renderer.renderItem(item, value);
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
}
