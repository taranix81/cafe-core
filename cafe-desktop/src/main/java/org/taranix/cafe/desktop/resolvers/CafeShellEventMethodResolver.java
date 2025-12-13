package org.taranix.cafe.desktop.resolvers;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.swt.events.ShellEvent;
import org.taranix.cafe.beans.CafeBeansFactory;
import org.taranix.cafe.beans.descriptors.members.CafeMethodInfo;
import org.taranix.cafe.beans.repositories.typekeys.BeanTypeKey;
import org.taranix.cafe.beans.resolvers.classInfo.method.CafeMethodResolver;
import org.taranix.cafe.desktop.actions.HandlerSignature;
import org.taranix.cafe.desktop.actions.HandlersService;
import org.taranix.cafe.desktop.annotations.CafeShellHandler;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Objects;

@Slf4j
public class CafeShellEventMethodResolver implements CafeMethodResolver {


    @Override
    public Object resolve(final Object instance, final CafeMethodInfo methodInfo, final CafeBeansFactory cafeBeansFactory) {
        HandlersService handlersService = (HandlersService) cafeBeansFactory.getBean(BeanTypeKey.from(HandlersService.class));

        List<CafeShellHandler> annotations = methodInfo.getAnnotations().stream()
                .filter(annotation -> annotation.annotationType().equals(CafeShellHandler.class))
                .map(CafeShellHandler.class::cast)
                .toList();


        for (CafeShellHandler cafeShellHandler : annotations) {
            handlersService.add(cafeShellHandler, HandlerSignature.builder()
                    .handlerInstance(instance)
                    .handlingMethod(methodInfo.getMethod())
                    .build());
        }

        return null;
    }

    @Override
    public boolean isApplicable(CafeMethodInfo methodInfo) {
        if (methodInfo.getMethod().getParameterCount() > 1) {
            log.warn("Method {} will be not triggered : more than 1 parameter", methodInfo.getMethod());
        }

        if (methodInfo.getMethod().getParameterCount() == 1 && methodInfo.getMethod().getParameterTypes()[0].equals(ShellEvent.class)) {
            log.warn("Method {} will be not triggered : wrong parameter type {}. Only SelectionEvent is supported", methodInfo.getMethod(), methodInfo.getMethod().getParameterTypes()[0]);
        }


        return methodInfo.getMethod().getParameterCount() == 0 ||
                (methodInfo.getMethod().getParameterCount() == 1 && methodInfo.getMethod().getParameterTypes()[0].equals(ShellEvent.class));
    }

    @Override
    public boolean supports(Class<? extends Annotation> annotation) {
        return Objects.equals(CafeShellHandler.class, annotation);
    }
}
