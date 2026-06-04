package org.taranix.cafe.beans.repositories.typekeys;

import lombok.Builder;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.taranix.cafe.beans.events.CafeHandlerSignature;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Objects;

@Getter
@Builder
public class HandlerTypeKey extends AbstractTypeKey {

    private final BeanTypeKey[] handlerParameters;
    private final BeanTypeKey handlerReturnTypeKey;
    private final Annotation[] handlerAnnotations;
    private final Annotation[] handlerClassAnnotations;

    protected HandlerTypeKey(
            BeanTypeKey[] methodParameters,
            BeanTypeKey handlerReturnTypeKey,
            Annotation[] handlerAnnotations,
            Annotation[] handlerClassAnnotations) {
        super(CafeHandlerSignature.class, StringUtils.EMPTY);
        this.handlerParameters = methodParameters;
        this.handlerReturnTypeKey = handlerReturnTypeKey;
        this.handlerAnnotations = handlerAnnotations;
        this.handlerClassAnnotations = handlerClassAnnotations;
    }


    @Override
    public int hashCode() {
        return Objects.hash(
                getType(),
                getHandlerReturnTypeKey(),
                Arrays.hashCode(getHandlerParameters()),
                Arrays.hashCode(getHandlerAnnotations()),
                Arrays.hashCode(getHandlerClassAnnotations())
        );
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
        return this.getType().equals(other.getType())
                && Objects.equals(getHandlerReturnTypeKey(), other.getHandlerReturnTypeKey())
                && Arrays.equals(this.getHandlerParameters(), other.getHandlerParameters())
                && Arrays.equals(this.getHandlerAnnotations(), other.getHandlerAnnotations())
                && Arrays.equals(this.getHandlerClassAnnotations(), other.getHandlerClassAnnotations())
                ;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (null != getHandlerReturnTypeKey()) {
            sb.append("(").append(getHandlerReturnTypeKey()).append(")");
        } else {
            sb.append("void ");
        }
        sb.append(getType().getTypeName());
        sb.append(" <");
        sb.append(StringUtils.join(handlerParameters, ","));
        sb.append(">");
        return sb.toString();
    }
}
