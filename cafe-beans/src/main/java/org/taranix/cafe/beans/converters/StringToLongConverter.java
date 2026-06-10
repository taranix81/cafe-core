package org.taranix.cafe.beans.converters;

import lombok.extern.slf4j.Slf4j;
import org.taranix.cafe.beans.annotations.classes.CafeSingleton;

@Slf4j
@CafeSingleton
class StringToLongConverter implements CafeConverter<String, Long> {
    @Override
    public Long convert(String s) {
        if (s == null) return null;
        try {
            return Long.parseLong(s.trim());
        } catch (NumberFormatException e) {
            log.warn("Cannot convert '{}' to Long", s);
        }
        return null;
    }
}
