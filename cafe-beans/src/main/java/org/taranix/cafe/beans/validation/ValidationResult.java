package org.taranix.cafe.beans.validation;

import lombok.Builder;

import java.util.Set;

@Builder
public record ValidationResult(String message, Set<Object> objects) {


}
