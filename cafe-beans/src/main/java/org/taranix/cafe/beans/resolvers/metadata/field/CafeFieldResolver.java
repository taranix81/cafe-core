package org.taranix.cafe.beans.resolvers.metadata.field;


import org.taranix.cafe.beans.metadata.CafeField;
import org.taranix.cafe.beans.resolvers.CafeBeansFactory;

import java.lang.annotation.Annotation;


public interface CafeFieldResolver {
    void resolve(Object instance, CafeField cafeFieldDescriptor, CafeBeansFactory resolverProcessor);

    boolean isApplicable(CafeField cafeFieldDescriptor);

    boolean supports(Class<? extends Annotation> annotation);
}
