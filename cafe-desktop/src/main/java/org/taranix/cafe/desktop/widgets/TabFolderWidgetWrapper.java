package org.taranix.cafe.desktop.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.Widget;

import java.util.function.Consumer;

public class TabFolderWidgetWrapper implements CafeWidgetWrapper<Integer> {

    private final TabFolder widget;

    public TabFolderWidgetWrapper(Composite parent) {
        this.widget = new TabFolder(parent, SWT.TOP);
    }

    public TabFolderWidgetWrapper(Composite parent, int style) {
        this.widget = new TabFolder(parent, style);
    }

    public TabFolderWidgetWrapper(TabFolder existing) {
        this.widget = existing;
    }

    @Override
    public Widget getWidget() { return widget; }

    @Override
    public Integer getValue() { return widget.getSelectionIndex(); }

    @Override
    public void setValue(Integer value) {
        if (value != null && value >= 0 && value < widget.getItemCount()) {
            widget.setSelection(value);
        }
    }

    @Override
    public void addChangeListener(Consumer<Integer> listener) {
        widget.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                listener.accept(widget.getSelectionIndex());
            }
        });
    }

    @Override
    public boolean isReadOnly() { return false; }
}
