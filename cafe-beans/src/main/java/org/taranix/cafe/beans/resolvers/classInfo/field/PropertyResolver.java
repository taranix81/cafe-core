package org.taranix.cafe.beans.resolvers.classInfo.field;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.StringUtils;
import org.taranix.cafe.beans.CafeBeansFactory;
import org.taranix.cafe.beans.CafeReflectionUtils;
import org.taranix.cafe.beans.annotations.CafeProperty;
import org.taranix.cafe.beans.converters.CafeConverter;
import org.taranix.cafe.beans.descriptors.members.CafeFieldInfo;
import org.taranix.cafe.beans.repositories.typekeys.BeanTypeKey;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

@Slf4j
public class PropertyResolver implements CafeFieldResolver {
    @Override
    public void resolve(Object instance, CafeFieldInfo cafeFieldDescriptor, CafeBeansFactory resolverProcessor) {
        CafeProperty cafePropertyAnnotation = cafeFieldDescriptor.getAnnotation(CafeProperty.class);
        String propertyName = cafePropertyAnnotation.name();
        Type targetType = cafeFieldDescriptor.getFieldTypeKey().getType();
        Object rawPropertyValue = resolverProcessor.getProperty(propertyName);
        Object propertyValue = convert(targetType, rawPropertyValue, resolverProcessor);
        log.debug("Setting property {} '{}' = {}", cafeFieldDescriptor.getField(), propertyName, propertyValue);
        CafeReflectionUtils.setFieldValue(cafeFieldDescriptor.getField(), instance, propertyValue);
    }

    @Override
    public boolean isApplicable(CafeFieldInfo cafeFieldDescriptor) {
        return true;
    }

    @Override
    public boolean supports(Class<? extends Annotation> annotation) {
        return CafeProperty.class.equals(annotation);
    }

    private Object convert(Type fieldType, Object propertyValue, CafeBeansFactory resolverProcessor) {
        if (fieldType instanceof Class<?> clz) {
            if (String.class.isAssignableFrom(clz)) {
                return propertyValue;
            }
            if (clz.isAssignableFrom(propertyValue.getClass())) {
                return propertyValue;
            }
            if (!clz.equals(String.class)) {
                CafeConverter converter = (CafeConverter) resolverProcessor.getBean(BeanTypeKey.from(CafeConverter.class, StringUtils.EMPTY, String.class, fieldType));
                return converter.convert(propertyValue);
            }

        }
        throw new NotImplementedException();
    }
}
