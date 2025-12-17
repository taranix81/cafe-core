package org.taranix.cafe.beans.resolvers.metadata;

import lombok.extern.slf4j.Slf4j;
import org.taranix.cafe.beans.CafeBeansFactory;
import org.taranix.cafe.beans.exceptions.ClassResolverException;
import org.taranix.cafe.beans.metadata.CafeClassMetadata;
import org.taranix.cafe.beans.metadata.CafeConstructorMetadata;
import org.taranix.cafe.beans.metadata.CafeFieldMetadata;
import org.taranix.cafe.beans.metadata.CafeMethodMetadata;

import java.util.Objects;

@Slf4j
public abstract class AbstractClassResolver implements CafeClassResolver {

    @Override
    public Object resolve(final CafeClassMetadata cafeClassMetadata, final CafeBeansFactory beansFactory) {
        log.debug("Resolving class :{}", cafeClassMetadata.getRootClass());
        Object instance = resolveConstructor(beansFactory, cafeClassMetadata.getConstructor());

        if (Objects.isNull(instance)) {
            throw new ClassResolverException("Class couldn't be instantiated :" + cafeClassMetadata.getRootClass());
        }

        cafeClassMetadata.getFields().forEach(cafeFieldInfo ->
                resolveField(beansFactory, instance, cafeFieldInfo)
        );

        cafeClassMetadata.getMethods().forEach(cafeMethodInfo ->
                resolveMethod(beansFactory, instance, cafeMethodInfo)
        );
        return instance;
    }

    protected Object resolveConstructor(CafeBeansFactory cafeBeansFactory, CafeConstructorMetadata descriptor) {
        return cafeBeansFactory.getResolvers().findConstructorResolver(descriptor)
                .resolve(descriptor, cafeBeansFactory);
    }

    protected void resolveMethod(CafeBeansFactory cafeBeansFactory, Object instance, CafeMethodMetadata methodDescriptor) {
        cafeBeansFactory.getResolvers().findMethodResolver(methodDescriptor)
                .resolve(instance, methodDescriptor, cafeBeansFactory);
    }

    protected void resolveField(CafeBeansFactory cafeBeansFactory, Object instance, CafeFieldMetadata descriptor) {
        cafeBeansFactory.getResolvers().findFieldResolver(descriptor)
                .resolve(instance, descriptor, cafeBeansFactory);

    }

}
