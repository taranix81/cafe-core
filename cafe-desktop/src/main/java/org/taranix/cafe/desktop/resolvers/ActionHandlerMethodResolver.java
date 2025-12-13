package org.taranix.cafe.desktop.resolvers;

import lombok.extern.slf4j.Slf4j;
import org.taranix.cafe.beans.CafeBeansFactory;
import org.taranix.cafe.beans.descriptors.members.CafeMethodInfo;
import org.taranix.cafe.beans.repositories.typekeys.BeanTypeKey;
import org.taranix.cafe.beans.resolvers.classInfo.method.CafeMethodResolver;
import org.taranix.cafe.desktop.actions.ActionHandlers;
import org.taranix.cafe.desktop.annotations.CafeActionHandler;

import java.lang.annotation.Annotation;
import java.util.Set;

@Slf4j
public class ActionHandlerMethodResolver implements CafeMethodResolver {


    @Override
    public Object resolve(final Object instance, final CafeMethodInfo methodInfo, final CafeBeansFactory cafeBeansFactory) {
        ActionHandlers actionHandlers = (ActionHandlers) cafeBeansFactory.getBeanOrNull(BeanTypeKey.from(ActionHandlers.class));
        if (actionHandlers == null) {
            actionHandlers = new ActionHandlers();
            cafeBeansFactory.addToRepository(actionHandlers);
        }

        return null;
    }

    @Override
    public boolean isApplicable(CafeMethodInfo methodInfo) {
        return methodInfo.getMethod().getParameterCount() < 2;
    }

    @Override
    public boolean supports(Class<? extends Annotation> annotation) {
        return Set.of(CafeActionHandler.class).contains(annotation);
    }
}
