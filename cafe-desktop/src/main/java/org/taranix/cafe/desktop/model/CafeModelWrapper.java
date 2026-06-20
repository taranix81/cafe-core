package org.taranix.cafe.desktop.model;

import java.util.List;

public class CafeModelWrapper<T> {

    private final T model;
    private final CafeModelInspector inspector;

    public CafeModelWrapper(T model) {
        this.model = model;
        this.inspector = new CafeModelInspector();
    }

    public T getModel() {
        return model;
    }

    public List<CafePropertyDescriptor> getProperties() {
        return inspector.getProperties(model.getClass());
    }

    public Object getValue(String propertyName) {
        return inspector.getValue(model, propertyName);
    }

    public void setValue(String propertyName, Object value) {
        inspector.setValue(model, propertyName, value);
    }
}
