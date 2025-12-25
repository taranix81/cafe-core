package org.taranix.cafe.beans.resolvers.metadata.method;

import org.apache.commons.lang3.StringUtils;
import org.taranix.cafe.beans.annotations.base.CafeHandlerType;
import org.taranix.cafe.beans.annotations.modifiers.CafeName;
import org.taranix.cafe.beans.metadata.CafeMethod;
import org.taranix.cafe.beans.reflection.CafeAnnotationUtils;
import org.taranix.cafe.beans.repositories.typekeys.HandlerTypeKey;
import org.taranix.cafe.beans.resolvers.CafeBeansFactory;

import java.lang.annotation.Annotation;
import java.util.Optional;
import java.util.Set;

public class SingletonHandlerMethodResolver implements CafeMethodResolver {
    @Override
    public Object resolve(Object instance, CafeMethod methodInfo, CafeBeansFactory cafeBeansFactory) {
        CafeName cafeNameAnnotation = methodInfo.getAnnotation(CafeName.class);
        Set<Annotation> annotations = methodInfo.getAnnotationsMarkedBy(CafeHandlerType.class);

        String handlerId = Optional.ofNullable(cafeNameAnnotation)
                .map(CafeName::value)
                .orElse(StringUtils.EMPTY);

        for (Annotation annotation : annotations) {
            HandlerTypeKey handlerTypeKey = HandlerTypeKey.from(annotation.annotationType(), handlerId, methodInfo.getMethodParameterTypeKeys());
            cafeBeansFactory.persist(handlerTypeKey, instance, methodInfo.getMethod());
        }
        return null;
    }

    @Override
    public boolean isApplicable(CafeMethod methodInfo) {
        return methodInfo.isSingleton();
    }

    @Override
    public boolean supports(Class<? extends Annotation> annotation) {
        return CafeAnnotationUtils.isAnnotationMarkedBy(annotation, CafeHandlerType.class);

    }
}
