package org.taranix.cafe.beans.resolvers.metadata.method;

import lombok.extern.slf4j.Slf4j;
import org.taranix.cafe.beans.CafeBeansFactory;
import org.taranix.cafe.beans.annotations.base.CafeWirerType;
import org.taranix.cafe.beans.metadata.CafeMethod;
import org.taranix.cafe.beans.reflection.CafeAnnotationUtils;
import org.taranix.cafe.beans.reflection.CafeReflectionUtils;

import java.lang.annotation.Annotation;

@Slf4j
public class SingletonWireMethodResolver implements CafeMethodResolver {
    @Override
    public Object resolve(Object instance, CafeMethod methodInfo, CafeBeansFactory cafeBeansFactory) {
        // Preventing invoke same method twice. First run should save result in Repository
        if (cafeBeansFactory.hasBeenExecuted(methodInfo.getMethod())) {
            return cafeBeansFactory.getBean(methodInfo.getMethodReturnTypeKey());
        }
        Object result = executeMethod(instance, methodInfo, cafeBeansFactory);
        cafeBeansFactory.persist(methodInfo, result);
        return result;
    }

    @Override
    public boolean isApplicable(CafeMethod methodInfo) {
        return methodInfo.isSingleton();
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
