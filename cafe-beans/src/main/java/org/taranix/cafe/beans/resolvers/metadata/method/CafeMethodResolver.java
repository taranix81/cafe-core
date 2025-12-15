package org.taranix.cafe.beans.resolvers.metadata.method;

import org.taranix.cafe.beans.CafeBeansFactory;
import org.taranix.cafe.beans.metadata.members.CafeMethodInfo;

import java.lang.annotation.Annotation;

public interface CafeMethodResolver {

    Object resolve(Object instance, CafeMethodInfo methodInfo, CafeBeansFactory cafeBeansFactory);

    boolean isApplicable(CafeMethodInfo methodInfo);

    boolean supports(Class<? extends Annotation> annotation);
}
