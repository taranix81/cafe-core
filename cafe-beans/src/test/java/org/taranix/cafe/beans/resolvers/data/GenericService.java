package org.taranix.cafe.beans.resolvers.data;

import lombok.Getter;
import org.taranix.cafe.beans.annotations.fields.CafeInject;

@Getter
public class GenericService<T> {

    @CafeInject
    private T value;
}
