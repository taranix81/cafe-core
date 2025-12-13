package org.taranix.cafe.beans.resolvers.classInfo.field;


import org.taranix.cafe.beans.CafeBeansFactory;
import org.taranix.cafe.beans.descriptors.members.CafeFieldInfo;

import java.lang.annotation.Annotation;


public interface CafeFieldResolver {
    void resolve(Object instance, CafeFieldInfo cafeFieldDescriptor, CafeBeansFactory resolverProcessor);

    boolean isApplicable(CafeFieldInfo cafeFieldDescriptor);

    boolean supports(Class<? extends Annotation> annotation);
}
