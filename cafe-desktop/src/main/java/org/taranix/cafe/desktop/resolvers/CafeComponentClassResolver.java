package org.taranix.cafe.desktop.resolvers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.taranix.cafe.beans.metadata.CafeClass;
import org.taranix.cafe.beans.repositories.typekeys.BeanTypeKey;
import org.taranix.cafe.beans.resolvers.CafeBeansFactory;
import org.taranix.cafe.beans.resolvers.metadata.AbstractClassResolver;
import org.taranix.cafe.desktop.annotations.CafeComponent;
import org.taranix.cafe.desktop.components.containers.CafeComponentRegistry;

import java.lang.annotation.Annotation;

public class CafeComponentClassResolver extends AbstractClassResolver {

    private static final Logger log = LoggerFactory.getLogger(CafeComponentClassResolver.class);

    @Override
    public Object resolve(CafeClass cafeClass, CafeBeansFactory beansFactory) {
        Object instance = super.resolve(cafeClass, beansFactory);
        try {
            Object registryBean = beansFactory.getBeanOrNull(BeanTypeKey.from(CafeComponentRegistry.class));
            if (registryBean instanceof CafeComponentRegistry registry) {
                registry.registerComponentClass(cafeClass.getRootClass());
                log.debug("Registered @CafeComponent: {}", cafeClass.getRootClass().getSimpleName());
            }
        } catch (Exception e) {
            log.warn("Could not register component class {}", cafeClass.getRootClass().getSimpleName(), e);
        }
        return instance;
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
