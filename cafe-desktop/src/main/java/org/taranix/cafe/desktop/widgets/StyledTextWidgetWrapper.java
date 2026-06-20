package org.taranix.cafe.desktop.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Widget;

import java.util.function.Consumer;

public class StyledTextWidgetWrapper implements CafeWidgetWrapper<String> {

    private final StyledText widget;

    public StyledTextWidgetWrapper(Composite parent) {
        this.widget = new StyledText(parent, SWT.BORDER | SWT.MULTI);
    }

    public StyledTextWidgetWrapper(Composite parent, int style) {
        this.widget = new StyledText(parent, style);
    }

    public StyledTextWidgetWrapper(StyledText existing) {
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
        widget.addModifyListener(e -> listener.accept(widget.getText()));
    }

    @Override
    public boolean isReadOnly() { return !widget.getEditable(); }
}
