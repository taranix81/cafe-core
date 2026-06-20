package org.taranix.cafe.desktop.model;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.SuperCall;
import net.bytebuddy.implementation.bind.annotation.This;
import net.bytebuddy.matcher.ElementMatchers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.taranix.cafe.beans.annotations.classes.CafeSingleton;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

@CafeSingleton
public class ByteBuddyModelProxyFactory implements ModelProxyFactory {

    private static final Logger log = LoggerFactory.getLogger(ByteBuddyModelProxyFactory.class);
    static final String LISTENER_FIELD = "$changeListener";

    private final Map<Class<?>, Class<?>> proxyCache = new ConcurrentHashMap<>();

    @Override
    @SuppressWarnings("unchecked")
    public <T> T createProxy(T model, ModelPropertyChangeListener listener) {
        Objects.requireNonNull(model, "model");
        T proxy = createProxy((Class<T>) model.getClass(), listener);
        copyState(model, proxy);
        return proxy;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T createProxy(Class<T> modelClass, ModelPropertyChangeListener listener) {
        Objects.requireNonNull(modelClass, "modelClass");
        Class<? extends T> proxyClass = (Class<? extends T>) proxyCache
                .computeIfAbsent(modelClass, this::buildProxyClass);
        try {
            T proxy = proxyClass.getDeclaredConstructor().newInstance();
            Field f = SetterInterceptor.listenerFieldCache.get(proxyClass);
            f.set(proxy, listener);
            return proxy;
        } catch (Exception e) {
            throw new RuntimeException("Cannot create proxy for " + modelClass.getName(), e);
        }
    }

    private Class<?> buildProxyClass(Class<?> modelClass) {
        try {
            Class<?> proxyClass = new ByteBuddy()
                    .subclass(modelClass)
                    .defineField(LISTENER_FIELD, ModelPropertyChangeListener.class, Visibility.PRIVATE)
                    .method(ElementMatchers.isSetter())
                    .intercept(MethodDelegation.to(SetterInterceptor.class))
                    .make()
                    .load(modelClass.getClassLoader())
                    .getLoaded();
            Field f = proxyClass.getDeclaredField(LISTENER_FIELD);
            f.setAccessible(true);
            SetterInterceptor.listenerFieldCache.put(proxyClass, f);
            return proxyClass;
        } catch (Exception e) {
            throw new RuntimeException("Cannot build proxy class for " + modelClass.getName(), e);
        }
    }

    private void copyState(Object source, Object target) {
        Class<?> cls = source.getClass();
        while (cls != null && cls != Object.class) {
            for (Field f : cls.getDeclaredFields()) {
                if (Modifier.isStatic(f.getModifiers()) || f.isSynthetic()) continue;
                try {
                    f.setAccessible(true);
                    Field targetField = findField(target.getClass(), f.getName());
                    if (targetField != null && !targetField.getName().equals(LISTENER_FIELD)) {
                        targetField.setAccessible(true);
                        targetField.set(target, f.get(source));
                    }
                } catch (Exception e) {
                    log.warn("Cannot copy field {} from {}", f.getName(), cls.getSimpleName(), e);
                }
            }
            cls = cls.getSuperclass();
        }
    }

    private Field findField(Class<?> cls, String name) {
        while (cls != null) {
            try { return cls.getDeclaredField(name); }
            catch (NoSuchFieldException ignored) {}
            cls = cls.getSuperclass();
        }
        return null;
    }

    public static class SetterInterceptor {

        static final Map<Class<?>, Field> listenerFieldCache = new ConcurrentHashMap<>();

        @RuntimeType
        public static Object intercept(
                @This Object target,
                @SuperCall Callable<?> superCall,
                @Origin Method method,
                @AllArguments Object[] args) throws Exception {
            Object result = superCall.call();
            try {
                Field listenerField = listenerFieldCache.get(target.getClass());
                if (listenerField != null) {
                    ModelPropertyChangeListener listener =
                            (ModelPropertyChangeListener) listenerField.get(target);
                    if (listener != null) {
                        listener.propertyChanged(derivePropertyName(method.getName()),
                                args.length > 0 ? args[0] : null);
                    }
                }
            } catch (Exception e) {
                LoggerFactory.getLogger(SetterInterceptor.class)
                        .error("Error firing property change for {}.{}", target.getClass().getSimpleName(), method.getName(), e);
            }
            return result;
        }

        private static String derivePropertyName(String setterName) {
            if (setterName.startsWith("set") && setterName.length() > 3) {
                return Character.toLowerCase(setterName.charAt(3)) + setterName.substring(4);
            }
            return setterName;
        }
    }
}
