package org.taranix.cafe.desktop.components;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Widget;
import org.taranix.cafe.beans.annotations.classes.CafeSingleton;
import org.taranix.cafe.beans.annotations.fields.CafeInject;
import org.taranix.cafe.beans.repositories.typekeys.BeanTypeKey;
import org.taranix.cafe.beans.resolvers.CafeBeansFactory;
import org.taranix.cafe.desktop.annotations.CafeComponent;

@CafeSingleton
public class ComponentFactory {

    public static final String COMPONENT = "component";

    @CafeInject
    private CafeBeansFactory factory;

    public Widget create(Class<? extends Component> componentType, Composite parent) {
        Component component = (Component) factory.getBeanOrNull(BeanTypeKey.from(componentType));
        Form form = getForm(component, componentType);
        Widget widget = form.create(parent);
        widget.setData(COMPONENT, component);
        return widget;
    }

    private Form getForm(Component component, Class<? extends Component> componentType) {
        if (component instanceof Form form) {
            return form;
        }

        Form form = resolveForm(componentType);
        if (form != null) {
            return form;
        }

        throw new RuntimeException("Couldn't create Component's widget. " +
                "Extend component with FORM intetrface or add Form type to CafeComponent annotation");
    }

    private Form resolveForm(Class<?> componentType) {
        CafeComponent ann = componentType.getAnnotation(CafeComponent.class);
        if (ann == null || ann.form() == Form.class) return null;
        Object bean = createForm(ann.form());
        return bean instanceof Form f ? f : null;
    }

    private Form createForm(Class<? extends Form> componentType) {
        return (Form) factory.getBeanOrNull(BeanTypeKey.from(componentType));
    }
}
