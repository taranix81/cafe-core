package org.taranix.cafe.beans.resolvers.metadata;

import org.taranix.cafe.beans.metadata.CafeClass;
import org.taranix.cafe.beans.resolvers.CafeBeansFactory;

import java.lang.annotation.Annotation;

public interface CafeClassResolver {

    Object resolve(CafeClass cafeClass, CafeBeansFactory processor);

    boolean isApplicable(CafeClass cafeClass);

    boolean supports(Class<? extends Annotation> annotation);
}
