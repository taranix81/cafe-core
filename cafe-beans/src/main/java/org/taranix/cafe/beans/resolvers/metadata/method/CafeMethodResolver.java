package org.taranix.cafe.beans.resolvers.metadata.method;

import org.taranix.cafe.beans.metadata.CafeMethod;
import org.taranix.cafe.beans.resolvers.CafeBeansFactory;

import java.lang.annotation.Annotation;

public interface CafeMethodResolver {

    Object resolve(Object instance, CafeMethod methodInfo, CafeBeansFactory cafeBeansFactory);

    boolean isApplicable(CafeMethod methodInfo);

    boolean supports(Class<? extends Annotation> annotation);
}
