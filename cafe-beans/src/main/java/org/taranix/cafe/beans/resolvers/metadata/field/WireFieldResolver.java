package org.taranix.cafe.beans.resolvers.metadata.field;

import lombok.extern.slf4j.Slf4j;
import org.taranix.cafe.beans.CafeBeansFactory;
import org.taranix.cafe.beans.annotations.base.CafeWirerType;
import org.taranix.cafe.beans.annotations.modifiers.CafeOptional;
import org.taranix.cafe.beans.metadata.CafeField;
import org.taranix.cafe.beans.reflection.CafeAnnotationUtils;
import org.taranix.cafe.beans.reflection.CafeReflectionUtils;
import org.taranix.cafe.beans.repositories.typekeys.BeanTypeKey;

import java.lang.annotation.Annotation;

@Slf4j
public class WireFieldResolver implements CafeFieldResolver {
    @Override
    public void resolve(final Object instance, final CafeField cafeField, final CafeBeansFactory cafeBeansFactory) {
        log.debug("Resolving field : {} ", cafeField.getField());
        BeanTypeKey fieldTypeKey = cafeField.getFieldTypeKey();
        Object value = cafeField.getAnnotationModifiers().contains(CafeOptional.class) ? cafeBeansFactory.getBeanOrNull(fieldTypeKey) :
                cafeBeansFactory.getBean(fieldTypeKey);

        log.debug("Resolved fields's value = {} ", value);
        CafeReflectionUtils.setFieldValue(cafeField.getField(), instance, value);
    }

    @Override
    public boolean isApplicable(final CafeField cafeFieldDescriptor) {
        return true;
    }

    @Override
    public boolean supports(Class<? extends Annotation> annotation) {
        return CafeAnnotationUtils.isAnnotationMarkedBy(annotation, CafeWirerType.class);
    }


}
