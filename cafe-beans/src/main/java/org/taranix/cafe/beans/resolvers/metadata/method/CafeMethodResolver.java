package org.taranix.cafe.beans.resolvers.metadata.method;

import org.taranix.cafe.beans.CafeBeansFactory;
import org.taranix.cafe.beans.metadata.CafeMethodMetadata;

import java.lang.annotation.Annotation;

public interface CafeMethodResolver {

    Object resolve(Object instance, CafeMethodMetadata methodInfo, CafeBeansFactory cafeBeansFactory);

    boolean isApplicable(CafeMethodMetadata methodInfo);

    boolean supports(Class<? extends Annotation> annotation);
}
