package org.taranix.cafe.beans.converters;

import lombok.extern.slf4j.Slf4j;
import org.taranix.cafe.beans.annotations.classes.CafeService;

@Slf4j
@CafeService
public class StringToIntegerConverter implements CafeConverter<String, Integer> {
    @Override
    public Integer convert(String s) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            log.warn("Cannot convert {} to Integer", s);
        }
        return null;
    }
}
