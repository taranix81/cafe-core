package org.taranix.cafe.beans.resolvers.metadata;

import org.taranix.cafe.beans.CafeBeansFactory;
import org.taranix.cafe.beans.metadata.CafeClassMetadata;

import java.lang.annotation.Annotation;

public interface CafeClassResolver {

    Object resolve(CafeClassMetadata cafeClassMetadata, CafeBeansFactory processor);

    boolean isApplicable(CafeClassMetadata cafeClassMetadata);

    boolean supports(Class<? extends Annotation> annotation);
}
