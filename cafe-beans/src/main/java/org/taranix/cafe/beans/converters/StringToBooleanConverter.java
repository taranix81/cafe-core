package org.taranix.cafe.beans.converters;

import lombok.extern.slf4j.Slf4j;
import org.taranix.cafe.beans.annotations.classes.CafeSingleton;

@Slf4j
@CafeSingleton
public class StringToBooleanConverter implements CafeConverter<String, Boolean> {
    @Override
    public Boolean convert(String s) {
        if (s == null) return null;
        Boolean value = Boolean.parseBoolean(s.trim());
        if (!value && !s.trim().equalsIgnoreCase("false")) {
            log.warn("Cannot convert '{}' to Boolean — defaulting to false", s);
        }
        return value;
    }
}
