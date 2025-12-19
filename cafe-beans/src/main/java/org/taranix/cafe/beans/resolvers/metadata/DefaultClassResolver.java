package org.taranix.cafe.beans.resolvers.metadata;


import org.taranix.cafe.beans.annotations.classes.CafeApplication;
import org.taranix.cafe.beans.annotations.classes.CafeService;
import org.taranix.cafe.beans.metadata.CafeClass;

import java.lang.annotation.Annotation;
import java.util.Set;

public class DefaultClassResolver extends AbstractClassResolver {


    @Override
    public boolean isApplicable(final CafeClass cafeClass) {
        return true;
    }

    @Override
    public boolean supports(Class<? extends Annotation> annotation) {
        return Set.of(CafeService.class, CafeApplication.class).contains(annotation);
    }


}
