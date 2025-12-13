package org.taranix.cafe.beans.resolvers.classInfo;

import lombok.extern.slf4j.Slf4j;
import org.taranix.cafe.beans.CafeBeansFactory;
import org.taranix.cafe.beans.descriptors.CafeClassInfo;
import org.taranix.cafe.beans.descriptors.members.CafeConstructorInfo;
import org.taranix.cafe.beans.descriptors.members.CafeFieldInfo;
import org.taranix.cafe.beans.descriptors.members.CafeMethodInfo;
import org.taranix.cafe.beans.exceptions.ClassResolverException;

import java.util.Objects;

@Slf4j
public abstract class AbstractClassResolver implements CafeClassResolver {

    @Override
    public Object resolve(final CafeClassInfo cafeClassInfo, final CafeBeansFactory beansFactory) {
        log.debug("Resolving class :{}", cafeClassInfo.getTypeClass());
        Object instance = resolveConstructor(beansFactory, cafeClassInfo.constructor());

        if (Objects.isNull(instance)) {
            throw new ClassResolverException("Class couldn't be instantiated :" + cafeClassInfo.getTypeClass());
        }

        cafeClassInfo.fields().forEach(cafeFieldInfo ->
                resolveField(beansFactory, instance, cafeFieldInfo)
        );

        cafeClassInfo.methods().forEach(cafeMethodInfo ->
                resolveMethod(beansFactory, instance, cafeMethodInfo)
        );
        return instance;
    }

    protected Object resolveConstructor(CafeBeansFactory cafeBeansFactory, CafeConstructorInfo descriptor) {
        return cafeBeansFactory.getResolvers().findConstructorResolver(descriptor)
                .resolve(descriptor, cafeBeansFactory);
    }

    protected void resolveMethod(CafeBeansFactory cafeBeansFactory, Object instance, CafeMethodInfo methodDescriptor) {
        cafeBeansFactory.getResolvers().findMethodResolver(methodDescriptor)
                .resolve(instance, methodDescriptor, cafeBeansFactory);
    }

    protected void resolveField(CafeBeansFactory cafeBeansFactory, Object instance, CafeFieldInfo descriptor) {
        cafeBeansFactory.getResolvers().findFieldResolver(descriptor)
                .resolve(instance, descriptor, cafeBeansFactory);

    }

}
