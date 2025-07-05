package org.taranix.cafe.beans.converters;

import lombok.extern.slf4j.Slf4j;
import org.taranix.cafe.beans.annotations.CafeService;

@Slf4j
@CafeService
public class StringToDoubleConverter implements CafeConverter<String, Double> {
    @Override
    public Double convert(String s) {
        try {
            return Double.parseDouble(s);
        } catch (NumberFormatException e) {
            log.warn("Cannot convert {} to Double", s);
        }
        return null;
    }
}
