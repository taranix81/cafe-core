package org.taranix.cafe.beans.converters;

import lombok.extern.slf4j.Slf4j;
import org.taranix.cafe.beans.annotations.classes.CafeService;

@Slf4j
@CafeService
public class StringToBooleanConverter implements CafeConverter<String, Boolean> {
    @Override
    public Boolean convert(String s) {
        return Boolean.parseBoolean(s);
    }
}
