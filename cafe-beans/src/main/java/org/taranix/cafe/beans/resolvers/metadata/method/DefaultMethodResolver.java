package org.taranix.cafe.beans.resolvers.metadata.method;

import lombok.extern.slf4j.Slf4j;
import org.taranix.cafe.beans.CafeBeansFactory;
import org.taranix.cafe.beans.annotations.CafePostInit;
import org.taranix.cafe.beans.annotations.CafeProvider;
import org.taranix.cafe.beans.metadata.CafeMethodMetadata;
import org.taranix.cafe.beans.reflection.CafeReflectionUtils;

import java.lang.annotation.Annotation;
import java.util.Set;

@Slf4j
public class DefaultMethodResolver implements CafeMethodResolver {

    @Override
    public Object resolve(final Object instance, final CafeMethodMetadata methodInfo, final CafeBeansFactory cafeBeansFactory) {
        // Preventing invoke same method twice. First run should save result in Repository
        if (cafeBeansFactory.hasBeenExecuted(methodInfo.getMethod()) && methodInfo.isSingleton()) {
            log.debug("Method has been executed already. Skipped");
            return cafeBeansFactory.getBean(methodInfo.getMethodReturnTypeKey());
        }
        Object result = executeMethod(instance, methodInfo, cafeBeansFactory);
        cafeBeansFactory.persistSingleton(methodInfo, result);
        return result;
    }

    @Override
    public boolean isApplicable(final CafeMethodMetadata methodInfo) {
        return true;
    }

    @Override
    public boolean supports(Class<? extends Annotation> annotation) {
        return Set.of(CafeProvider.class, CafePostInit.class).contains(annotation);
    }

    protected Object executeMethod(Object instance, CafeMethodMetadata methodInfo, CafeBeansFactory cafeBeansFactory) {
        log.debug("Resolving method :{}", methodInfo.getMethod());
        Object[] arguments = getArguments(methodInfo, cafeBeansFactory);
        if (arguments.length > 0) {
            log.debug("Resolved method's arguments = {}", arguments);
        }
        return CafeReflectionUtils.getMethodValue(methodInfo.getMethod(), instance, arguments);
    }

    private Object[] getArguments(CafeMethodMetadata methodDescriptor, CafeBeansFactory cafeBeansFactory) {
        log.debug("Resolving method's arguments ");
        return methodDescriptor
                .getRequiredTypes()
                .stream()
                .filter(typeKey -> !typeKey.getType().equals(methodDescriptor.getMemberDeclaringClass()))
                .map(cafeBeansFactory::getBean)
                .toArray();
    }
}
