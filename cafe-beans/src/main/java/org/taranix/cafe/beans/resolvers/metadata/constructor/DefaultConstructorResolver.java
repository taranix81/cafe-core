package org.taranix.cafe.beans.resolvers.metadata.constructor;

import lombok.extern.slf4j.Slf4j;
import org.taranix.cafe.beans.CafeBeansFactory;
import org.taranix.cafe.beans.metadata.CafeConstructorMetadata;
import org.taranix.cafe.beans.metadata.CafeMemberMetadata;
import org.taranix.cafe.beans.reflection.CafeReflectionUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;

@Slf4j
public class DefaultConstructorResolver implements CafeConstructorResolver {


    @Override
    public Object resolve(CafeConstructorMetadata constructorDescriptor, CafeBeansFactory cafeBeansFactory) {
        log.debug("Resolving constructor : {} ", constructorDescriptor.getConstructor());
        Constructor<?> classConstructor = constructorDescriptor.getConstructor();
        Object[] arguments = constructorDescriptor.getRequiredTypes().stream()
                .map(cafeBeansFactory::getBean)
                .toArray();

        if (constructorDescriptor.hasDependencies()) {
            log.debug("Resolved constructor's arguments: {}", arguments);
        }
        Object instance = constructorDescriptor.hasDependencies()
                ? CafeReflectionUtils.instantiate(classConstructor, arguments)
                : CafeReflectionUtils.instantiate(classConstructor);
        cafeBeansFactory.persistSingleton(constructorDescriptor, instance);
        return instance;
    }

    @Override
    public boolean isApplicable(final CafeMemberMetadata descriptor) {
        return descriptor.isConstructor();
    }

    @Override
    public boolean supports(Class<? extends Annotation> annotation) {
        return true;
    }


}
