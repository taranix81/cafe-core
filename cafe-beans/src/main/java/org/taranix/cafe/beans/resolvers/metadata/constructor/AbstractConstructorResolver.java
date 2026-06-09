package org.taranix.cafe.beans.resolvers.metadata.constructor;

import lombok.extern.slf4j.Slf4j;
import org.taranix.cafe.beans.metadata.CafeConstructor;
import org.taranix.cafe.beans.reflection.CafeReflectionUtils;
import org.taranix.cafe.beans.resolvers.CafeBeansFactory;

import java.lang.reflect.Constructor;

@Slf4j
public abstract class AbstractConstructorResolver implements CafeConstructorResolver {

    @Override
    public Object resolve(CafeConstructor constructorDescriptor, CafeBeansFactory cafeBeansFactory) {
        Constructor<?> classConstructor = constructorDescriptor.getConstructor();
        Object[] arguments = constructorDescriptor.getRequiredTypeKeys().stream()
                .map(cafeBeansFactory::getBean)
                .toArray();
        log.debug("Resolving constructor: {} with {} argument(s)", classConstructor, arguments.length);
        return CafeReflectionUtils.instantiate(classConstructor, arguments);
    }
}
