package org.taranix.cafe.beans.resolvers.metadata.method;

import org.taranix.cafe.beans.annotations.base.CafeHandlerType;
import org.taranix.cafe.beans.events.CafeHandlerSignature;
import org.taranix.cafe.beans.metadata.CafeMethod;
import org.taranix.cafe.beans.repositories.typekeys.HandlerTypeKey;
import org.taranix.cafe.beans.resolvers.CafeBeansFactory;

import java.lang.annotation.Annotation;
import java.util.Optional;

public class SingletonHandlerMethodResolver implements CafeMethodResolver {

    private final Class<? extends Annotation> annotationType;

    public SingletonHandlerMethodResolver(Class<? extends Annotation> annotationType) {
        this.annotationType = annotationType;
    }

    @Override
    public Object resolve(Object instance, CafeMethod methodInfo, CafeBeansFactory cafeBeansFactory) {
        HandlerTypeKey handlerTypeKey = HandlerTypeKey.builder()
                .handlerClassAnnotations(methodInfo.getMemberDeclaringClass().getAnnotations())
                .handlerAnnotations(methodInfo.getAnnotationsMarkedBy(CafeHandlerType.class))
                .handlerReturnTypeKey(methodInfo.getMethodReturnTypeKey())
                .handlerParameters(methodInfo.getMethodParameterTypeKeys())
                .build();

        CafeHandlerSignature handlerSignature = CafeHandlerSignature.builder()
                .handler(methodInfo)
                .instance(Optional.of(instance))
                .build();


        cafeBeansFactory.persist(handlerTypeKey, handlerSignature, methodInfo.getMethod());

        return null;
    }

    @Override
    public boolean isApplicable(CafeMethod methodInfo) {
        return methodInfo.isSingleton();
    }

    @Override
    public boolean supports(Class<? extends Annotation> annotation) {
        return annotation.equals(annotationType);
    }
}
