package org.taranix.cafe.beans.resolvers.types;

import org.taranix.cafe.beans.repositories.typekeys.BeanTypeKey;
import org.taranix.cafe.beans.resolvers.CafeBeansFactory;

import java.lang.reflect.Type;
import java.util.Optional;

public class OptionalBeanTypeResolver implements CafeBeanTypeResolver {

    @Override
    public boolean isApplicable(BeanTypeKey typeKey) {
        return typeKey.isOptional();
    }

    @Override
    public Object resolve(BeanTypeKey typeKey, CafeBeansFactory beansFactory) {
        Type innerType = typeKey.getActualParameters()[0];
        Object bean = beansFactory.getBeanOrNull(BeanTypeKey.from(innerType));
        return Optional.ofNullable(bean);
    }

    @Override
    public Object resolveOrNull(BeanTypeKey typeKey, CafeBeansFactory beansFactory) {
        return resolve(typeKey, beansFactory);
    }
}
