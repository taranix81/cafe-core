package org.taranix.cafe.shell.commands;

import lombok.Builder;
import lombok.Getter;
import org.taranix.cafe.beans.metadata.CafeMethodMetadata;
import org.taranix.cafe.beans.repositories.typekeys.BeanTypeKey;

@Getter
@Builder
public class CafeCommandRuntime {

    private Object commandInstance;
    private CafeMethodMetadata executor;
    private String[] arguments;

    public BeanTypeKey commandTypeKey() {
        return executor.getParentTypeKey();
        //getDeclaringClassTypeKey();
        // getCafeClassMetadata().getTypeKey();
    }
}
