package org.taranix.cafe.beans.resolvers.types;

import lombok.extern.slf4j.Slf4j;
import org.taranix.cafe.beans.CafeBeansFactory;
import org.taranix.cafe.beans.exceptions.ArrayTypeResolverException;
import org.taranix.cafe.beans.repositories.typekeys.BeanTypeKey;

import java.lang.reflect.Array;
import java.lang.reflect.ParameterizedType;
import java.util.Collection;

@Slf4j
public class ArrayBeansResolver implements CafeBeanResolver {
    @Override
    public Object resolve(BeanTypeKey typeKey, CafeBeansFactory beansFactory) {
        return resolveOrNull(typeKey, beansFactory);
    }

    @Override
    public Object resolveOrNull(BeanTypeKey typeKey, CafeBeansFactory beansFactory) {
        log.debug("Resolving array :{}", typeKey);
        BeanTypeKey collectionType = BeanTypeKey.from(Collection.class, typeKey.getTypeIdentifier(), typeKey.getArrayComponentType());
        Object result = beansFactory.getResolvers().findBeanTypekeyResolver(collectionType)
                .resolve(collectionType, beansFactory);

        if (result instanceof Collection<?>) {
            Collection<Object> items = (Collection<Object>) result;
            return convertCollectionToArray(typeKey.getArrayComponentType(), items);
        } else if (result != null) {
            throw new ArrayTypeResolverException("Cannot convert %s to array".formatted(result));
        }
        return null;
    }

    @Override
    public boolean isApplicable(BeanTypeKey typeKey) {
        return typeKey.isArray();
    }

    private <T> T[] convertCollectionToArray(T type, Collection<T> collection) {
        if (type instanceof Class<?>) {
            return collection.toArray(value -> (T[]) Array.newInstance((Class<T>) type, value));
        }
        return collection.toArray(value -> (T[]) Array.newInstance((Class<T>) ((ParameterizedType) type).getRawType(), value));

    }

}
