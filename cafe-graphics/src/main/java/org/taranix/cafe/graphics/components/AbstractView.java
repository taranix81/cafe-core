package org.taranix.cafe.graphics.components;

import lombok.Getter;
import org.eclipse.swt.widgets.Widget;

public abstract class AbstractView extends AbstractComponent implements View {

    @Getter
    private Widget widget;


    @Override
    public void init(Widget parent) {
        widget = createWidget(parent);
    }

    protected abstract Widget createWidget(Widget parent);

}
