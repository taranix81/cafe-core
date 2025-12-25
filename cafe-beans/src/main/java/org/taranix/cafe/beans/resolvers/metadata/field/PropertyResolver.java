package org.taranix.cafe.beans.resolvers.metadata.field;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.StringUtils;
import org.taranix.cafe.beans.annotations.base.CafePropertyType;
import org.taranix.cafe.beans.annotations.fields.CafeProperty;
import org.taranix.cafe.beans.converters.CafeConverter;
import org.taranix.cafe.beans.metadata.CafeField;
import org.taranix.cafe.beans.reflection.CafeAnnotationUtils;
import org.taranix.cafe.beans.reflection.CafeReflectionUtils;
import org.taranix.cafe.beans.repositories.typekeys.BeanTypeKey;
import org.taranix.cafe.beans.resolvers.CafeBeansFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

@Slf4j
public class PropertyResolver implements CafeFieldResolver {
    @Override
    public void resolve(Object instance, CafeField cafeField, CafeBeansFactory cafeBeansFactory) {
        CafeProperty cafePropertyAnnotation = cafeField.getAnnotation(CafeProperty.class);
        String propertyName = cafePropertyAnnotation.name();
        Type targetType = cafeField.getFieldTypeKey().getType();
        Object rawPropertyValue = cafeBeansFactory.getProperty(propertyName);
        Object propertyValue = convert(targetType, rawPropertyValue, cafeBeansFactory);
        log.debug("Setting property {} '{}' = {}", cafeField.getField(), propertyName, propertyValue);
        CafeReflectionUtils.setFieldValue(cafeField.getField(), instance, propertyValue);
    }

    @Override
    public boolean isApplicable(CafeField cafeField) {
        return true;
    }

    @Override
    public boolean supports(Class<? extends Annotation> annotation) {
        return CafeAnnotationUtils.isAnnotationMarkedBy(annotation, CafePropertyType.class);
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
