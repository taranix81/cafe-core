package org.taranix.cafe.beans.repositories.beans;


import lombok.Builder;
import lombok.Getter;

import java.lang.reflect.Executable;

@Builder
@Getter
public class BeanRepositoryEntry {
    private Object value;
    private Executable source;
    private boolean primary;
}
