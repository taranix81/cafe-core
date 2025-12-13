package org.taranix.cafe.beans.resolvers.classInfo.constructor;

import org.taranix.cafe.beans.CafeBeansFactory;
import org.taranix.cafe.beans.descriptors.members.CafeConstructorInfo;
import org.taranix.cafe.beans.descriptors.members.CafeMemberInfo;

import java.lang.annotation.Annotation;

public interface CafeConstructorResolver {

    Object resolve(CafeConstructorInfo descriptor, CafeBeansFactory cafeBeansFactory);

    boolean isApplicable(CafeMemberInfo descriptor);

    boolean supports(Class<? extends Annotation> annotation);
}
