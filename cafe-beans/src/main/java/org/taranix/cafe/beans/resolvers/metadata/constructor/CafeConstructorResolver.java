package org.taranix.cafe.beans.resolvers.metadata.constructor;

import org.taranix.cafe.beans.CafeBeansFactory;
import org.taranix.cafe.beans.metadata.CafeConstructor;
import org.taranix.cafe.beans.metadata.CafeMember;

import java.lang.annotation.Annotation;

public interface CafeConstructorResolver {

    Object resolve(CafeConstructor descriptor, CafeBeansFactory cafeBeansFactory);

    boolean isApplicable(CafeMember descriptor);

    boolean supports(Class<? extends Annotation> annotation);
}
