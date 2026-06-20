package org.taranix.cafe.desktop.model;

import org.taranix.cafe.desktop.annotations.CafeModelProperty;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class CafeModelInspector {

    public List<CafePropertyDescriptor> getProperties(Class<?> modelClass) {
        List<CafePropertyDescriptor> result = new ArrayList<>();
        for (Field f : modelClass.getDeclaredFields()) {
            if (f.isAnnotationPresent(CafeModelProperty.class)) {
                CafeModelProperty ann = f.getAnnotation(CafeModelProperty.class);
                String name = ann.value().isEmpty() ? f.getName() : ann.value();
                boolean readOnly = hasSetter(modelClass, f) == null;
                result.add(new CafePropertyDescriptor(name, f.getType(), readOnly));
            }
        }
        for (Method m : modelClass.getDeclaredMethods()) {
            if (m.isAnnotationPresent(CafeModelProperty.class) && isGetter(m)) {
                CafeModelProperty ann = m.getAnnotation(CafeModelProperty.class);
                String name = ann.value().isEmpty() ? derivePropertyName(m) : ann.value();
                boolean readOnly = findSetter(modelClass, name, m.getReturnType()) == null;
                result.add(new CafePropertyDescriptor(name, m.getReturnType(), readOnly));
            }
        }
        return result;
    }

    public Object getValue(Object model, String propertyName) {
        try {
            Method getter = findGetter(model.getClass(), propertyName);
            if (getter != null) {
                getter.setAccessible(true);
                return getter.invoke(model);
            }
            Field field = findField(model.getClass(), propertyName);
            if (field != null) {
                field.setAccessible(true);
                return field.get(model);
            }
        } catch (Exception e) {
            throw new RuntimeException("Cannot read property '" + propertyName + "' from " + model.getClass(), e);
        }
        throw new IllegalArgumentException("Property '" + propertyName + "' not found on " + model.getClass());
    }

    public void setValue(Object model, String propertyName, Object value) {
        try {
            Method setter = findSetter(model.getClass(), propertyName, value == null ? Object.class : value.getClass());
            if (setter != null) {
                setter.setAccessible(true);
                setter.invoke(model, value);
                return;
            }
            Field field = findField(model.getClass(), propertyName);
            if (field != null) {
                field.setAccessible(true);
                field.set(model, value);
                return;
            }
        } catch (Exception e) {
            throw new RuntimeException("Cannot write property '" + propertyName + "' on " + model.getClass(), e);
        }
        throw new IllegalArgumentException("Property '" + propertyName + "' not found on " + model.getClass());
    }

    private boolean isGetter(Method m) {
        return (m.getName().startsWith("get") || m.getName().startsWith("is"))
                && m.getParameterCount() == 0
                && m.getReturnType() != void.class;
    }

    private String derivePropertyName(Method getter) {
        String name = getter.getName();
        if (name.startsWith("get") && name.length() > 3) {
            return Character.toLowerCase(name.charAt(3)) + name.substring(4);
        }
        if (name.startsWith("is") && name.length() > 2) {
            return Character.toLowerCase(name.charAt(2)) + name.substring(3);
        }
        return name;
    }

    private Method hasSetter(Class<?> cls, Field field) {
        return findSetter(cls, field.getName(), field.getType());
    }

    private Method findSetter(Class<?> cls, String propertyName, Class<?> valueType) {
        String setterName = "set" + Character.toUpperCase(propertyName.charAt(0)) + propertyName.substring(1);
        for (Method m : cls.getMethods()) {
            if (m.getName().equals(setterName) && m.getParameterCount() == 1) {
                return m;
            }
        }
        return null;
    }

    private Method findGetter(Class<?> cls, String propertyName) {
        String cap = Character.toUpperCase(propertyName.charAt(0)) + propertyName.substring(1);
        for (String prefix : new String[]{"get", "is"}) {
            try { return cls.getMethod(prefix + cap); } catch (NoSuchMethodException ignored) {}
        }
        return null;
    }

    private Field findField(Class<?> cls, String propertyName) {
        Class<?> c = cls;
        while (c != null) {
            try { return c.getDeclaredField(propertyName); } catch (NoSuchFieldException ignored) {}
            c = c.getSuperclass();
        }
        return null;
    }
}
