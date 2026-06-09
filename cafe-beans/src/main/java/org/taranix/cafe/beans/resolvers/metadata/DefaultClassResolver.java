package org.taranix.cafe.beans.resolvers.metadata;


import org.taranix.cafe.beans.annotations.classes.CafeApplication;
import org.taranix.cafe.beans.annotations.classes.CafeService;
import org.taranix.cafe.beans.metadata.CafeClass;
import org.taranix.cafe.beans.reflection.CafeAnnotationUtils;

import java.lang.annotation.Annotation;

public class DefaultClassResolver extends AbstractClassResolver {

    @Override
    public boolean isApplicable(final CafeClass cafeClass) {
        return true;
    }

    @Override
    public boolean supports(Class<? extends Annotation> annotation) {
        return CafeAnnotationUtils.isAnnotationMarkedBy(annotation, CafeService.class)
                || annotation.equals(CafeApplication.class);
    }


}
