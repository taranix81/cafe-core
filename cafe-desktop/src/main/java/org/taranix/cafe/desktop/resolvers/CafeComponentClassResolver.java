package org.taranix.cafe.desktop.resolvers;

import org.taranix.cafe.beans.metadata.CafeClass;
import org.taranix.cafe.beans.resolvers.CafeBeansFactory;
import org.taranix.cafe.beans.resolvers.metadata.AbstractClassResolver;
import org.taranix.cafe.desktop.annotations.CafeComponent;

import java.lang.annotation.Annotation;

/**
 * Ensures @CafeComponent classes are DI-managed (instantiated, injected, post-init called).
 * Component ownership is tracked by ApplicationComponent via registerContainer(), not a registry.
 */
public class CafeComponentClassResolver extends AbstractClassResolver {

    @Override
    public Object resolve(CafeClass cafeClass, CafeBeansFactory beansFactory) {
        return super.resolve(cafeClass, beansFactory);
    }

    @Override
    public boolean isApplicable(CafeClass cafeClass) {
        return cafeClass.getRootClass().isAnnotationPresent(CafeComponent.class);
    }

    @Override
    public boolean supports(Class<? extends Annotation> annotation) {
        return CafeComponent.class.equals(annotation);
    }
}
