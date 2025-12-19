package org.taranix.cafe.beans.resolvers.metadata.method;

import lombok.extern.slf4j.Slf4j;
import org.taranix.cafe.beans.CafeBeansFactory;
import org.taranix.cafe.beans.annotations.base.CafeWirerType;
import org.taranix.cafe.beans.metadata.CafeMethod;
import org.taranix.cafe.beans.reflection.CafeAnnotationUtils;
import org.taranix.cafe.beans.reflection.CafeReflectionUtils;

import java.lang.annotation.Annotation;

@Slf4j
public class PrototypeWireMethodResolver implements CafeMethodResolver {

    @Override
    public Object resolve(final Object instance, final CafeMethod methodInfo, final CafeBeansFactory cafeBeansFactory) {
        return executeMethod(instance, methodInfo, cafeBeansFactory);
    }

    @Override
    public boolean isApplicable(final CafeMethod methodInfo) {
        return methodInfo.isPrototype();
    }

    @Override
    public boolean supports(Class<? extends Annotation> annotation) {
        return CafeAnnotationUtils.isAnnotationMarkedBy(annotation, CafeWirerType.class);
    }

    protected Object executeMethod(Object instance, CafeMethod methodInfo, CafeBeansFactory cafeBeansFactory) {
        log.debug("Resolving method :{}", methodInfo.getMethod());
        Object[] arguments = getArguments(methodInfo, cafeBeansFactory);
        if (arguments.length > 0) {
            log.debug("Resolved method's arguments = {}", arguments);
        }
        return CafeReflectionUtils.getMethodValue(methodInfo.getMethod(), instance, arguments);
    }

    private Object[] getArguments(CafeMethod methodMetadata, CafeBeansFactory cafeBeansFactory) {
        log.debug("Resolving method's arguments ");
        return methodMetadata
                .getRequiredTypeKeys()
                .stream()
                .filter(typeKey -> !typeKey.getType().equals(methodMetadata.getMemberDeclaringClass()))
                .map(cafeBeansFactory::getBean)
                .toArray();
    }
}
