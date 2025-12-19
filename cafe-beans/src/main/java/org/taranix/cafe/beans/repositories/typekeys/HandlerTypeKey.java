package org.taranix.cafe.beans.repositories.typekeys;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.taranix.cafe.beans.annotations.modifiers.CafeName;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Objects;

public class HandlerTypeKey extends TypeKey {

    @Getter
    private final BeanTypeKey[] parameters;

    @Getter
    private final Annotation handlerAnnotation;

    protected HandlerTypeKey(Annotation annotation, String typeIdentifier, BeanTypeKey[] parameters) {
        super(annotation.annotationType(), typeIdentifier);
        this.parameters = parameters;
        handlerAnnotation = null;
    }

    public static HandlerTypeKey from(Annotation annotation, BeanTypeKey... handlerParameters) {
        return new HandlerTypeKey(annotation, StringUtils.EMPTY, handlerParameters);
    }

    public static HandlerTypeKey from(Annotation annotation, CafeName cafeName, BeanTypeKey... handlerParameters) {
        String handlerName = cafeName != null ? cafeName.value() : StringUtils.EMPTY;
        return new HandlerTypeKey(annotation, handlerName, handlerParameters);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getTypeIdentifier(), getHandlerAnnotation(), Arrays.hashCode(getParameters()));
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
        return Arrays.equals(this.parameters, other.parameters) &&
                this.getType().equals(other.getType()) &&
                this.getTypeIdentifier().equals(other.getTypeIdentifier());
    }
}
