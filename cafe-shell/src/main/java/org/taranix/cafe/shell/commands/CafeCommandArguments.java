package org.taranix.cafe.shell.commands;

import lombok.Getter;
import lombok.ToString;
import org.taranix.cafe.beans.annotations.classes.CafeSingleton;

import java.util.Map;

@CafeSingleton
@ToString
public class CafeCommandArguments {

    @Getter
    private String[] cliValues;

    @Getter
    private Map<String, Object> variables;

    public void setValues(String[] values) {
        this.cliValues = values;
    }

}
