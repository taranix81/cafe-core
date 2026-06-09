package org.taranix.cafe.beans.events.selectors;

import org.apache.commons.lang3.reflect.TypeUtils;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.taranix.cafe.beans.annotations.classes.CafeSingleton;
import org.taranix.cafe.beans.reflection.CafeTypesUtils;
import org.taranix.cafe.beans.repositories.typekeys.BeanTypeKey;
import org.taranix.cafe.beans.repositories.typekeys.HandlerTypeKey;

import java.lang.annotation.Annotation;
import java.util.Arrays;

@CafeSingleton
public class CafeHandlerSelector implements HandlerTypekeySelector {

    public CafeHandlerSelector() {
    }

    private static BeanTypeKey @NonNull [] getBeanTypeKeys(Object[] parameters) {
        return Arrays.stream(parameters)
                .map(p -> BeanTypeKey.from(p.getClass()))
                .toArray(BeanTypeKey[]::new);
    }

    private static boolean areEquals(BeanTypeKey[] referenceParams, BeanTypeKey[] inputParams) {
        if (referenceParams.length != inputParams.length) {
            return false;
        }

        for (int i = 0; i < referenceParams.length; i++) {
            if (!CafeTypesUtils.isTypeCompatible(referenceParams[i].getType(), inputParams[i].getType())) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean isMatch(HandlerTypeKey handlerTypeKey, Class<? extends Annotation> methodAnnotation, Object... parameters) {
        boolean anyMatch = Arrays.stream(handlerTypeKey.getHandlerAnnotations())
                .anyMatch(annotation -> TypeUtils.equals(annotation.annotationType(), methodAnnotation));
        BeanTypeKey[] parameterTypeKeys = getBeanTypeKeys(parameters);
        return anyMatch && areEquals(handlerTypeKey.getHandlerParameters(), parameterTypeKeys);
    }


}

