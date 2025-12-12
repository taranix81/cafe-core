package org.taranix.cafe.beans.resolvers.classInfo;

import org.taranix.cafe.beans.CafeBeansFactory;
import org.taranix.cafe.beans.descriptors.CafeClassDescriptor;

import java.lang.annotation.Annotation;

public interface CafeClassResolver {

    Object resolve(CafeClassDescriptor cafeClassDescriptor, CafeBeansFactory processor);

    boolean isApplicable(CafeClassDescriptor cafeClassDescriptor);

    boolean supports(Class<? extends Annotation> annotation);
}
