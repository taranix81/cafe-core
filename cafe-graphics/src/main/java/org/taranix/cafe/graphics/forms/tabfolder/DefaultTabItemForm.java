package org.taranix.cafe.graphics.forms.tabfolder;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.widgets.Widget;
import org.taranix.cafe.beans.annotations.CafeService;

@CafeService
class DefaultTabItemForm implements TabItemForm {
    @Override
    public CTabItem create(Widget parent) {
        return new CTabItem((CTabFolder) parent, SWT.NONE);
    }
}
