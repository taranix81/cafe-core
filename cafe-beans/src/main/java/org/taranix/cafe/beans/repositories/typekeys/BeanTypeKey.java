package org.taranix.cafe.beans.repositories.typekeys;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.TypeUtils;
import org.taranix.cafe.beans.CafeReflectionUtils;
import org.taranix.cafe.beans.exceptions.BeanTypeKeyException;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class BeanTypeKey extends TypeKey {

    public BeanTypeKey(Type type, String typeIdentifier) {
        super(type, typeIdentifier);
    }

    public static BeanTypeKey from(Type type) {
        return new BeanTypeKey(type, StringUtils.EMPTY);
    }


    public static BeanTypeKey from(Type type, String typeIdentifier) {
        return new BeanTypeKey(type, typeIdentifier);
    }

    public static BeanTypeKey from(Class<?> cls, String typeIdentifier, Type... typeArguments) {
        return new BeanTypeKey(TypeUtils.parameterize(cls, typeArguments), typeIdentifier);
    }

    public static boolean isMatchByTypeOrGenericType(final BeanTypeKey requiredTypeKey, Set<BeanTypeKey> providedTypeKeys) {
        if (!providedTypeKeys.contains(requiredTypeKey)) {
            if (requiredTypeKey.isArray()) {
                Class<?> arrayType = ((Class<?>) requiredTypeKey.getType()).getComponentType();
                return isMatchByTypeOrGenericType(BeanTypeKey.from(arrayType, requiredTypeKey.getTypeIdentifier()), providedTypeKeys);
            }

            if (requiredTypeKey.isCollection()) {
                Type[] argumentTypes = ((ParameterizedType) requiredTypeKey.getType()).getActualTypeArguments();
                if (argumentTypes.length == 1) {
                    return isMatchByTypeOrGenericType(BeanTypeKey.from(argumentTypes[0], requiredTypeKey.getTypeIdentifier()), providedTypeKeys);
                }
                throw new BeanTypeKeyException("Collection should have one argument type : %s".formatted(requiredTypeKey.getType()));
            }
        }
        return providedTypeKeys.contains(requiredTypeKey);
    }

    private static boolean areEqualsByContent(ParameterizedType pt1, ParameterizedType pt2) {
        if (!pt1.getRawType().equals(pt2.getRawType()) || pt1.getActualTypeArguments().length != pt2.getActualTypeArguments().length) {
            return false;
        }

        for (int i = 0; i < pt1.getActualTypeArguments().length; i++) {
            if (!pt1.getActualTypeArguments()[i].equals(pt2.getActualTypeArguments()[i])) {
                return false;
            }
        }
        return true;
    }

    public Type[] getActualParameters() {
        if (isParametrizedType()) {
            return ((ParameterizedType) getType()).getActualTypeArguments();
        }
        return new Type[]{};
    }

    public boolean isParametrizedType() {
        return getType() instanceof ParameterizedType;
    }

    public boolean isList() {
        return isCollection()
                && getRawType() instanceof Class<?>
                && List.class.isAssignableFrom((Class<?>) getRawType());
    }

    public boolean isCollection() {
        return CafeReflectionUtils.isCollection(getType());
    }

    public Type getRawType() {
        if (isParametrizedType()) {
            return ((ParameterizedType) getType()).getRawType();
        }

        if (getType() instanceof Class<?>) {
            return getType();
        }

        if (isArray()) {
            return getArrayComponentType();
        }

        return null;
    }

    public boolean isArray() {
        return CafeReflectionUtils.isArray(getType());
    }

    public Type getArrayComponentType() {
        if (isArray()) {
            return ((Class<?>) getType()).componentType();
        }
        return null;
    }

    public boolean isClass() {
        return getType() instanceof Class<?>;
    }

    public boolean isSet() {
        return isCollection()
                && getRawType() instanceof Class<?>
                && Set.class.isAssignableFrom((Class<?>) getRawType());
    }

    @Override
    public int hashCode() {
        if (isParametrizedType()) {
            return Objects.hash(getTypeIdentifier(),
                    ((ParameterizedType) getType()).getRawType(),
                    Arrays.hashCode(((ParameterizedType) getType()).getActualTypeArguments())
            );
        }

        return Objects.hash(getTypeIdentifier(), getType());
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof BeanTypeKey comparable) {
            if (isParametrizedType() && comparable.isParametrizedType()) {
                return Objects.equals(getTypeIdentifier(), comparable.getTypeIdentifier())
                        && areEqualsByContent((ParameterizedType) getType(), (ParameterizedType) comparable.getType());
            }
            return Objects.equals(getTypeIdentifier(), comparable.getTypeIdentifier())
                    && Objects.equals(getType(), comparable.getType());
        }
        return false;
    }


}
