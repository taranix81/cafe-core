package org.taranix.cafe.beans.repositories.typekeys;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Type;

@Getter
public abstract class TypeKey {

    private final Type type;
    private final String typeIdentifier;

    protected TypeKey(Type type, String typeIdentifier) {
        this.type = type;
        this.typeIdentifier = typeIdentifier;
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (StringUtils.isNotBlank(typeIdentifier)) {
            sb.append("(").append(typeIdentifier).append(")");
        }
        sb.append(type.getTypeName());
        return sb.toString();
    }


}
