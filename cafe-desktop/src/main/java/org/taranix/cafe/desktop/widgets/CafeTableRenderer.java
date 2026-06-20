package org.taranix.cafe.desktop.widgets;

import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

public interface CafeTableRenderer<T> {

    void renderColumns(Table table);

    void renderItem(TableItem item, T value, int index);
}
