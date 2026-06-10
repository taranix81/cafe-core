package org.taranix.cafe.shell.commands;

import lombok.Getter;
import lombok.ToString;
import org.taranix.cafe.beans.annotations.classes.CafeSingleton;

import java.util.HashMap;
import java.util.Map;

@CafeSingleton
@ToString
public class CafeCommandArguments {

    @Getter
    private String[] cliValues = new String[0];

    @Getter
    private Map<String, Object> variables = new HashMap<>();

    public void setValues(String[] values) {
        this.cliValues = values != null ? values : new String[0];
    }

    public void reset() {
        this.cliValues = new String[0];
        this.variables = new HashMap<>();
    }

}
