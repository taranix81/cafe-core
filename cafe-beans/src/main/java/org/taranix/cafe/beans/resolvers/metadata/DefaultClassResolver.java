package org.taranix.cafe.beans.resolvers.metadata;


import org.taranix.cafe.beans.annotations.CafeApplication;
import org.taranix.cafe.beans.annotations.CafeFactory;
import org.taranix.cafe.beans.annotations.CafeService;
import org.taranix.cafe.beans.metadata.CafeClassMetadata;

import java.lang.annotation.Annotation;
import java.util.Set;

public class DefaultClassResolver extends AbstractClassResolver {


    @Override
    public boolean isApplicable(final CafeClassMetadata cafeClassMetadata) {
        return true;
    }

    @Override
    public boolean supports(Class<? extends Annotation> annotation) {
        return Set.of(CafeFactory.class, CafeService.class, CafeApplication.class).contains(annotation);
    }


}
