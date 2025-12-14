package org.taranix.cafe.beans.resolvers.classInfo;

import org.taranix.cafe.beans.CafeBeansFactory;
import org.taranix.cafe.beans.metadata.CafeClassInfo;

import java.lang.annotation.Annotation;

public interface CafeClassResolver {

    Object resolve(CafeClassInfo cafeClassInfo, CafeBeansFactory processor);

    boolean isApplicable(CafeClassInfo cafeClassInfo);

    boolean supports(Class<? extends Annotation> annotation);
}
