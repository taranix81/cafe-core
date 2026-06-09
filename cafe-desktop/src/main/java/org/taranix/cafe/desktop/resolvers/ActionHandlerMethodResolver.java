package org.taranix.cafe.desktop.resolvers;

import lombok.extern.slf4j.Slf4j;
import org.taranix.cafe.beans.metadata.CafeMethod;
import org.taranix.cafe.beans.repositories.typekeys.BeanTypeKey;
import org.taranix.cafe.beans.resolvers.CafeBeansFactory;
import org.taranix.cafe.beans.resolvers.metadata.method.CafeMethodResolver;
import org.taranix.cafe.desktop.actions.Action;
import org.taranix.cafe.desktop.actions.ActionHandlers;
import org.taranix.cafe.desktop.actions.HandlerSignature;
import org.taranix.cafe.desktop.annotations.CafeActionHandler;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Set;

@Slf4j
public class ActionHandlerMethodResolver implements CafeMethodResolver {

    @Override
    public Object resolve(final Object instance, final CafeMethod methodInfo, final CafeBeansFactory cafeBeansFactory) {
        Method method = methodInfo.getMethod();
        if (method.getParameterCount() != 1 || !Action.class.isAssignableFrom(method.getParameterTypes()[0])) {
            return null;
        }
        ActionHandlers actionHandlers = (ActionHandlers) cafeBeansFactory.getBean(BeanTypeKey.from(ActionHandlers.class));
        @SuppressWarnings("unchecked")
        Class<? extends Action> actionType = (Class<? extends Action>) method.getParameterTypes()[0];
        actionHandlers.add(actionType, HandlerSignature.builder()
                .handlerInstance(instance)
                .handlingMethod(method)
                .build());
        return null;
    }

    @Override
    public boolean isApplicable(CafeMethod methodInfo) {
        Method method = methodInfo.getMethod();
        if (method.getParameterCount() > 1) {
            log.warn("Method {} will not be registered: @CafeActionHandler supports at most one Action parameter", method);
            return false;
        }
        if (method.getParameterCount() == 1 && !Action.class.isAssignableFrom(method.getParameterTypes()[0])) {
            log.warn("Method {} will not be registered: parameter type {} does not extend Action", method, method.getParameterTypes()[0].getSimpleName());
            return false;
        }
        return true;
    }

    @Override
    public boolean supports(Class<? extends Annotation> annotation) {
        return Set.of(CafeActionHandler.class).contains(annotation);
    }
}
