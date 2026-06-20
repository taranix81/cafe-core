package org.taranix.cafe.desktop.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.LocationAdapter;
import org.eclipse.swt.browser.LocationEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Widget;

import java.util.function.Consumer;

public class BrowserWidgetWrapper implements CafeWidgetWrapper<String> {

    private final Browser widget;

    public BrowserWidgetWrapper(Composite parent) {
        this.widget = new Browser(parent, SWT.NONE);
    }

    public BrowserWidgetWrapper(Composite parent, int style) {
        this.widget = new Browser(parent, style);
    }

    public BrowserWidgetWrapper(Browser existing) {
        this.widget = existing;
    }

    @Override
    public Widget getWidget() { return widget; }

    @Override
    public String getValue() { return widget.getUrl(); }

    @Override
    public void setValue(String value) {
        if (value != null) widget.setUrl(value);
    }

    @Override
    public void addChangeListener(Consumer<String> listener) {
        widget.addLocationListener(new LocationAdapter() {
            @Override
            public void changed(LocationEvent event) {
                listener.accept(event.location);
            }
        });
    }

    @Override
    public boolean isReadOnly() { return true; }
}
