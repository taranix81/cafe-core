package org.taranix.cafe.desktop.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Widget;

import java.util.function.Consumer;

public class LinkWidgetWrapper implements CafeWidgetWrapper<String> {

    private final Link widget;

    public LinkWidgetWrapper(Composite parent) {
        this.widget = new Link(parent, SWT.NONE);
    }

    public LinkWidgetWrapper(Link existing) {
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
        // read-only — no change notifications
    }

    @Override
    public boolean isReadOnly() { return true; }
}
