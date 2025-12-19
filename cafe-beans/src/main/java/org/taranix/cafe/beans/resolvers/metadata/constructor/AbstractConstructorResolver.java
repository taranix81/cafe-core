package org.taranix.cafe.beans.resolvers.metadata.constructor;

import lombok.extern.slf4j.Slf4j;
import org.taranix.cafe.beans.CafeBeansFactory;
import org.taranix.cafe.beans.metadata.CafeConstructor;
import org.taranix.cafe.beans.reflection.CafeReflectionUtils;

import java.lang.reflect.Constructor;

@Slf4j
public abstract class AbstractConstructorResolver implements CafeConstructorResolver {

    @Override
    public Object resolve(CafeConstructor constructorDescriptor, CafeBeansFactory cafeBeansFactory) {
        log.debug("Resolving constructor : {} ", constructorDescriptor.getConstructor());
        Constructor<?> classConstructor = constructorDescriptor.getConstructor();
        Object[] arguments = constructorDescriptor.getRequiredTypeKeys().stream()
                .map(cafeBeansFactory::getBean)
                .toArray();

        if (constructorDescriptor.hasDependencies()) {
            log.debug("Resolved constructor's arguments: {}", arguments);
        }
        return constructorDescriptor.hasDependencies()
                ? CafeReflectionUtils.instantiate(classConstructor, arguments)
                : CafeReflectionUtils.instantiate(classConstructor);
    }
}
