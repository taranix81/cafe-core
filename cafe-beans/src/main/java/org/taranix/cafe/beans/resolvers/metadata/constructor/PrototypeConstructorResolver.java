package org.taranix.cafe.beans.resolvers.metadata.constructor;

import lombok.extern.slf4j.Slf4j;
import org.taranix.cafe.beans.metadata.CafeMember;

import java.lang.annotation.Annotation;

@Slf4j
public class PrototypeConstructorResolver extends AbstractConstructorResolver {

    @Override
    public boolean isApplicable(final CafeMember descriptor) {
        return descriptor.isPrototype();
    }

    @Override
    public boolean supports(Class<? extends Annotation> annotation) {
        return true;
    }


}
