package org.taranix.cafe.beans.resolvers.classInfo.field;

import lombok.extern.slf4j.Slf4j;
import org.taranix.cafe.beans.CafeBeansFactory;
import org.taranix.cafe.beans.CafeReflectionUtils;
import org.taranix.cafe.beans.annotations.CafeInject;
import org.taranix.cafe.beans.annotations.modifiers.CafeOptional;
import org.taranix.cafe.beans.descriptors.members.CafeFieldInfo;
import org.taranix.cafe.beans.repositories.typekeys.BeanTypeKey;

import java.lang.annotation.Annotation;

@Slf4j
public class DefaultFieldResolver implements CafeFieldResolver {
    @Override
    public void resolve(final Object instance, final CafeFieldInfo cafeFieldDescriptor, final CafeBeansFactory cafeBeansFactory) {
        log.debug("Resolving field : {} ", cafeFieldDescriptor.getField());
        BeanTypeKey fieldTypeKey = cafeFieldDescriptor.getFieldTypeKey();
        Object value = cafeFieldDescriptor.getAnnotationModifiers().contains(CafeOptional.class) ? cafeBeansFactory.getBeanOrNull(fieldTypeKey) :
                cafeBeansFactory.getBean(fieldTypeKey);

        log.debug("Resolved fields's value = {} ", value);
        CafeReflectionUtils.setFieldValue(cafeFieldDescriptor.getField(), instance, value);
    }

    @Override
    public boolean isApplicable(final CafeFieldInfo cafeFieldDescriptor) {
        return true;
    }

    @Override
    public boolean supports(Class<? extends Annotation> annotation) {
        return CafeInject.class.equals(annotation);
    }


}
