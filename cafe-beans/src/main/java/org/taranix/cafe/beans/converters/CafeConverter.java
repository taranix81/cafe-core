package org.taranix.cafe.beans.converters;

public interface CafeConverter<TSource, TTarget> {
    TTarget convert(TSource source);
}
