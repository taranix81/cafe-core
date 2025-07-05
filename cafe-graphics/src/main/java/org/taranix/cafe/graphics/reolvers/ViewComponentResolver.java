package org.taranix.cafe.graphics.reolvers;

import org.taranix.cafe.beans.descriptors.CafeClassInfo;
import org.taranix.cafe.beans.resolvers.classInfo.AbstractClassResolver;
import org.taranix.cafe.graphics.annotations.CafeComponent;

import java.lang.annotation.Annotation;

public class ViewComponentResolver extends AbstractClassResolver {
    @Override
    public boolean isApplicable(CafeClassInfo cafeClassInfo) {
        return true;
    }

    @Override
    public boolean supports(Class<? extends Annotation> annotation) {
        return annotation == CafeComponent.class;
    }
}
