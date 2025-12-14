package org.taranix.cafe.shell.resolvers;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.taranix.cafe.beans.CafeBeansFactory;
import org.taranix.cafe.beans.metadata.members.CafeMethodInfo;
import org.taranix.cafe.beans.resolvers.classInfo.method.DefaultMethodResolver;
import org.taranix.cafe.shell.annotations.CafeCommandRun;

import java.lang.annotation.Annotation;
import java.util.Objects;


@Slf4j
@NoArgsConstructor()
public class CafeCommandMethodResolver extends DefaultMethodResolver {

    @Override
    public Object resolve(Object instance, CafeMethodInfo methodInfo, CafeBeansFactory cafeBeansFactory) {
        //We want to execute method more than once and we always persist result
        Object result = executeMethod(instance, methodInfo, cafeBeansFactory);
        cafeBeansFactory.persistAny(methodInfo, result, null);
        return result;
    }

    @Override
    public boolean supports(Class<? extends Annotation> annotation) {
        return Objects.equals(CafeCommandRun.class, annotation);
    }
}
