package org.taranix.cafe.shell.resolvers;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.taranix.cafe.beans.metadata.CafeMethod;
import org.taranix.cafe.beans.reflection.CafeReflectionUtils;
import org.taranix.cafe.beans.resolvers.CafeBeansFactory;
import org.taranix.cafe.beans.resolvers.metadata.method.PrototypeWireMethodResolver;
import org.taranix.cafe.shell.annotations.CafeCommandRun;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Objects;


/**
 * Resolves {@code @CafeCommandRun} methods on demand. Commands are prototype-scoped — each
 * {@code @CafeCommand} class gets a fresh instance per execution, which is why this resolver
 * extends {@link PrototypeWireMethodResolver} rather than the singleton variant.
 */
@Slf4j
@NoArgsConstructor()
public class CafeCommandMethodResolver extends PrototypeWireMethodResolver {

    @Override
    public Object resolve(Object instance, CafeMethod methodInfo, CafeBeansFactory cafeBeansFactory) {
        Object result = executeMethod(instance, methodInfo, cafeBeansFactory);
        cafeBeansFactory.persist(methodInfo, result, null);
        return result;
    }

    @Override
    protected Object executeMethod(Object instance, CafeMethod methodInfo, CafeBeansFactory cafeBeansFactory) {
        Object[] arguments = Arrays.stream(methodInfo.getMethodParameterTypeKeys())
                .map(cafeBeansFactory::getBean)
                .toArray();
        return CafeReflectionUtils.getMethodValue(methodInfo.getMethod(), instance, arguments);
    }

    @Override
    public boolean isApplicable(CafeMethod methodInfo) {
        return methodInfo.hasAnnotation(CafeCommandRun.class);
    }

    @Override
    public boolean supports(Class<? extends Annotation> annotation) {
        return Objects.equals(CafeCommandRun.class, annotation);
    }
}
