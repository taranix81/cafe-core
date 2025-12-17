package org.taranix.cafe.beans.resolvers.metadata.constructor;

import org.taranix.cafe.beans.CafeBeansFactory;
import org.taranix.cafe.beans.metadata.CafeConstructorMetadata;
import org.taranix.cafe.beans.metadata.CafeMemberMetadata;

import java.lang.annotation.Annotation;

public interface CafeConstructorResolver {

    Object resolve(CafeConstructorMetadata descriptor, CafeBeansFactory cafeBeansFactory);

    boolean isApplicable(CafeMemberMetadata descriptor);

    boolean supports(Class<? extends Annotation> annotation);
}
