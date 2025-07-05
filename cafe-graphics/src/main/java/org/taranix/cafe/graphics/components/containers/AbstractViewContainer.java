package org.taranix.cafe.graphics.components.containers;

import lombok.Getter;
import org.eclipse.swt.widgets.Widget;
import org.taranix.cafe.graphics.components.Component;
import org.taranix.cafe.graphics.components.View;

public abstract class AbstractViewContainer extends AbstractContainer
        implements Container, View {


    @Getter
    private Widget widget;

    @Override
    public void init(Widget parent) {
        widget = createWidget(parent);
    }

    protected abstract Widget createWidget(Widget parent);

    @Override
    protected void addSubComponent(Component component) {
        if (component instanceof View view) {
            view.init(this.getWidget());
        }
        super.addSubComponent(component);
    }
}
