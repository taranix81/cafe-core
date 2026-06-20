package org.taranix.cafe.desktop.widgets;

import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

public interface CafeTreeRenderer<T> {

    void renderColumns(Tree tree);

    void renderItem(TreeItem item, T value);

    boolean hasChildren(T value);
}
