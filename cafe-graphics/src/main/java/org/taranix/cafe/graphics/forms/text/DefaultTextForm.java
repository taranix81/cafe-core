package org.taranix.cafe.graphics.forms.text;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;
import org.taranix.cafe.beans.annotations.CafeService;

@CafeService
class DefaultTextForm implements TextForm {
    @Override
    public Text create(Widget parent) {
        return new Text((Composite) parent, SWT.MULTI | SWT.BORDER);
    }
}
