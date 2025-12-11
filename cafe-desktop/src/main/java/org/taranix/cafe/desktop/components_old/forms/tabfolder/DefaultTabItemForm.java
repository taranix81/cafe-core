package org.taranix.cafe.desktop.components_old.forms.tabfolder;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.widgets.Widget;

//@CafeService
class DefaultTabItemForm implements TabItemForm {
    @Override
    public CTabItem create(Widget parent) {
        return new CTabItem((CTabFolder) parent, SWT.NONE);
    }
}
