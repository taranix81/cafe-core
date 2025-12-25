package org.taranix.cafe.beans.repositories.typekeys;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Objects;

public class HandlerTypeKey extends AbstractTypeKey {

    @Getter
    private final BeanTypeKey[] parameters;


    protected HandlerTypeKey(Class<? extends Annotation> annotationType, String typeIdentifier, BeanTypeKey[] parameters) {
        super(annotationType, StringUtils.defaultIfEmpty(typeIdentifier, StringUtils.EMPTY));
        this.parameters = parameters;

    }

    public static HandlerTypeKey from(Class<? extends Annotation> annotationType, BeanTypeKey... handlerParameters) {
        return new HandlerTypeKey(annotationType, StringUtils.EMPTY, handlerParameters);
    }

    public static HandlerTypeKey from(Class<? extends Annotation> annotationType, String handlerId, BeanTypeKey... handlerParameters) {
        return new HandlerTypeKey(annotationType, handlerId, handlerParameters);
    }

    public Class<? extends Annotation> getAnnotationType() {
        if (getType() instanceof Class<?> clazz) {
            if (clazz.isAnnotation()) {
                return (Class<? extends Annotation>) clazz;
            }
        }
        return null;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getTypeIdentifier(), getType(), Arrays.hashCode(getParameters()));
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        final HandlerTypeKey other = (HandlerTypeKey) obj;
        return this.getType().equals(other.getType()) && Arrays.equals(this.getParameters(), other.getParameters()) &&
                this.getTypeIdentifier().equals(other.getTypeIdentifier());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (StringUtils.isNotBlank(getTypeIdentifier())) {
            sb.append("(").append(getTypeIdentifier()).append(")");
        }
        sb.append(getType().getTypeName());
        sb.append(" <");
        sb.append(StringUtils.join(parameters));
        sb.append(">");
        return sb.toString();
    }
}
