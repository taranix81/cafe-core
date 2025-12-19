package org.taranix.cafe.beans.resolvers.metadata;

import lombok.extern.slf4j.Slf4j;
import org.taranix.cafe.beans.CafeBeansFactory;
import org.taranix.cafe.beans.exceptions.ClassResolverException;
import org.taranix.cafe.beans.metadata.CafeClass;
import org.taranix.cafe.beans.metadata.CafeConstructor;
import org.taranix.cafe.beans.metadata.CafeField;
import org.taranix.cafe.beans.metadata.CafeMethod;

import java.util.Objects;

@Slf4j
public abstract class AbstractClassResolver implements CafeClassResolver {

    @Override
    public Object resolve(final CafeClass cafeClass, final CafeBeansFactory beansFactory) {
        log.debug("Resolving class :{}", cafeClass.getRootClass());
        Object instance = resolveConstructor(beansFactory, cafeClass.getConstructor());

        if (Objects.isNull(instance)) {
            throw new ClassResolverException("Class couldn't be instantiated :" + cafeClass.getRootClass());
        }

        cafeClass.getFields().forEach(cafeFieldInfo ->
                resolveField(beansFactory, instance, cafeFieldInfo)
        );

        cafeClass.getMethods().forEach(cafeMethodInfo ->
                resolveMethod(beansFactory, instance, cafeMethodInfo)
        );
        return instance;
    }

    protected Object resolveConstructor(CafeBeansFactory cafeBeansFactory, CafeConstructor descriptor) {
        return cafeBeansFactory.getResolvers().findConstructorResolver(descriptor)
                .resolve(descriptor, cafeBeansFactory);
    }

    protected void resolveMethod(CafeBeansFactory cafeBeansFactory, Object instance, CafeMethod methodDescriptor) {
        cafeBeansFactory.getResolvers().findMethodResolver(methodDescriptor)
                .resolve(instance, methodDescriptor, cafeBeansFactory);
    }

    protected void resolveField(CafeBeansFactory cafeBeansFactory, Object instance, CafeField descriptor) {
        cafeBeansFactory.getResolvers().findFieldResolver(descriptor)
                .resolve(instance, descriptor, cafeBeansFactory);

    }

}
