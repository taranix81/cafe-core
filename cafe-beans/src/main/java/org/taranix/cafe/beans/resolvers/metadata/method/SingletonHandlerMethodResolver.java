package org.taranix.cafe.beans.resolvers.metadata.method;

import org.taranix.cafe.beans.events.EventHub;
import org.taranix.cafe.beans.metadata.CafeMethod;
import org.taranix.cafe.beans.repositories.typekeys.BeanTypeKey;
import org.taranix.cafe.beans.resolvers.CafeBeansFactory;

import java.lang.annotation.Annotation;

public class SingletonHandlerMethodResolver implements CafeMethodResolver {

    private final Class<? extends Annotation> annotationType;

    public SingletonHandlerMethodResolver(Class<? extends Annotation> annotationType) {
        this.annotationType = annotationType;
    }

    @Override
    public Object resolve(Object instance, CafeMethod methodInfo, CafeBeansFactory cafeBeansFactory) {
        Object hub = cafeBeansFactory.getBeanOrNull(BeanTypeKey.from(EventHub.class));
        if (hub instanceof EventHub eventHub) {
            eventHub.register(instance);
        }
        return null;
    }

    @Override
    public boolean isApplicable(CafeMethod methodInfo) {
        return true;
    }

    @Override
    public boolean supports(Class<? extends Annotation> annotation) {
        return annotation.equals(annotationType);
    }
}
