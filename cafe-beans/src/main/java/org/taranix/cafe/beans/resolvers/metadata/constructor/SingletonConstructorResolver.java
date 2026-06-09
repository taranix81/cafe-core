package org.taranix.cafe.beans.resolvers.metadata.constructor;

import lombok.extern.slf4j.Slf4j;
import org.taranix.cafe.beans.metadata.CafeConstructor;
import org.taranix.cafe.beans.metadata.CafeMember;
import org.taranix.cafe.beans.repositories.typekeys.BeanTypeKey;
import org.taranix.cafe.beans.resolvers.CafeBeansFactory;

import java.lang.annotation.Annotation;

@Slf4j
public class SingletonConstructorResolver extends AbstractConstructorResolver {

    @Override
    public Object resolve(CafeConstructor constructorDescriptor, CafeBeansFactory cafeBeansFactory) {
        BeanTypeKey typeKey = constructorDescriptor.getParentTypeKey();
        Class<?> declaringClass = constructorDescriptor.getConstructor().getDeclaringClass();
        if (cafeBeansFactory.isResolved(typeKey)) {
            Object existing = cafeBeansFactory.getAllResolved(typeKey).stream()
                    .filter(obj -> obj.getClass() == declaringClass)
                    .findFirst()
                    .orElse(null);
            if (existing != null) {
                return existing;
            }
        }
        Object instance = super.resolve(constructorDescriptor, cafeBeansFactory);
        cafeBeansFactory.persist(constructorDescriptor, instance);
        return instance;
    }

    @Override
    public boolean isApplicable(final CafeMember descriptor) {
        return descriptor.isSingleton();
    }

    @Override
    public boolean supports(Class<? extends Annotation> annotation) {
        return true;
    }


}
