package org.taranix.cafe.desktop.components_old.viewers;

import lombok.Getter;
import org.eclipse.swt.widgets.Widget;
import org.taranix.cafe.desktop.components_old.AbstractComponent;
import org.taranix.cafe.desktop.components_old.ViewComponent;

public abstract class AbstractViewComponent extends AbstractComponent implements ViewComponent {

    @Getter
    private Widget widget;


    @Override
    public void init(Widget parent) {
        widget = createWidget(parent);
    }

    protected abstract Widget createWidget(Widget parent);

}
