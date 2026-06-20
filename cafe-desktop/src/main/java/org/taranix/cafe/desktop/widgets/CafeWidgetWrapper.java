package org.taranix.cafe.desktop.widgets;

import org.eclipse.swt.widgets.Widget;

import java.util.function.Consumer;

public interface CafeWidgetWrapper<T> {

    Widget getWidget();

    T getValue();

    void setValue(T value);

    void addChangeListener(Consumer<T> listener);

    boolean isReadOnly();
}
