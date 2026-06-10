package org.taranix.cafe.beans.converters;

import lombok.extern.slf4j.Slf4j;
import org.taranix.cafe.beans.annotations.classes.CafeSingleton;

@Slf4j
@CafeSingleton
public class StringToIntegerConverter implements CafeConverter<String, Integer> {
    @Override
    public Integer convert(String s) {
        if (s == null) return null;
        try {
            return Integer.parseInt(s.trim());
        } catch (NumberFormatException e) {
            log.warn("Cannot convert '{}' to Integer", s);
        }
        return null;
    }
}
