package org.taranix.cafe.beans.converters;

import org.taranix.cafe.beans.annotations.classes.CafeService;

@CafeService
class StringToLongConverter implements CafeConverter<String, Long> {
    @Override
    public Long convert(String s) {
        return Long.parseLong(s);
    }
}
