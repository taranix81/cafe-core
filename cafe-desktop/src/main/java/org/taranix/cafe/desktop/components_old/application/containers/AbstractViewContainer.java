package org.taranix.cafe.desktop.components_old.application.containers;

import lombok.Getter;
import org.eclipse.swt.widgets.Widget;
import org.taranix.cafe.desktop.components_old.Component;
import org.taranix.cafe.desktop.components_old.ViewComponent;

public abstract class AbstractViewContainer
        extends AbstractContainer
        implements Container, ViewComponent {


    @Getter
    private Widget widget;

    @Override
    public void init(Widget parent) {
        widget = createWidget(parent);
    }

    protected abstract Widget createWidget(Widget parent);

    @Override
    public void addSubComponent(Component component) {
        if (component instanceof ViewComponent viewComponent) {
            viewComponent.init(this.getWidget());
        }
        super.addSubComponent(component);
    }
}
