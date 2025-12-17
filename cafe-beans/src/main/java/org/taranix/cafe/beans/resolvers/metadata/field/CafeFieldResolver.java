package org.taranix.cafe.beans.resolvers.metadata.field;


import org.taranix.cafe.beans.CafeBeansFactory;
import org.taranix.cafe.beans.metadata.CafeFieldMetadata;

import java.lang.annotation.Annotation;


public interface CafeFieldResolver {
    void resolve(Object instance, CafeFieldMetadata cafeFieldDescriptor, CafeBeansFactory resolverProcessor);

    boolean isApplicable(CafeFieldMetadata cafeFieldDescriptor);

    boolean supports(Class<? extends Annotation> annotation);
}
