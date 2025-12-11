package org.taranix.cafe.desktop.actions;

import lombok.Builder;

import java.lang.reflect.Method;

@Builder
public record HandlerSignature(Method handlingMethod, Object handlerInstance) {
}
