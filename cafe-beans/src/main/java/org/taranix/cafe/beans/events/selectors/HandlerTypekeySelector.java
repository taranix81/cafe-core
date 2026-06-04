package org.taranix.cafe.beans.events.selectors;

import org.taranix.cafe.beans.repositories.typekeys.HandlerTypeKey;

import java.lang.annotation.Annotation;

public interface HandlerTypekeySelector {


    boolean isMatch(HandlerTypeKey handlerTypeKey, Class<? extends Annotation> methodAnnotation, Object... parameters);
}

