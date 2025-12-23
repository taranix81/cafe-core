package org.taranix.cafe.beans.validation;

import org.taranix.cafe.beans.annotations.base.CafeHandlerType;
import org.taranix.cafe.beans.metadata.CafeMember;
import org.taranix.cafe.beans.metadata.CafeMetadataRegistry;
import org.taranix.cafe.beans.metadata.CafeMethod;
import org.taranix.cafe.beans.reflection.CafeAnnotationUtils;
import org.taranix.cafe.beans.repositories.typekeys.BeanTypeKey;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class CafeHandlerMethodsParameterValidator implements CafeValidator {
    @Override
    public Optional<ValidationResult> validate(CafeMetadataRegistry registry) {
        Set<Object> allInvolvedObjects = new HashSet<>();

        Set<CafeMethod> handlers = registry.allMembers().stream()
                .filter(CafeMember::isMethod)
                .map(CafeMethod.class::cast)
                .filter(cafeMethod -> CafeAnnotationUtils.hasAnnotationMarker(cafeMethod.getMethod(), CafeHandlerType.class))
                .collect(Collectors.toSet());

        for (CafeMethod handler : handlers) {
            Set<BeanTypeKey> notAllowMethodParameters = (Arrays.stream(handler.getMethodParameterTypeKeys())
                    .filter(beanTypeKey -> beanTypeKey.getRawType().equals(Object.class))
                    .collect(Collectors.toSet()));
            if (!notAllowMethodParameters.isEmpty()) {
                allInvolvedObjects.add(handler);
            }

        }
        if (allInvolvedObjects.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(ValidationResult.builder()
                .message("Handler's parameter can not by an Object")
                .objects(allInvolvedObjects)
                .build());


    }
}
