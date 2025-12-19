package org.taranix.cafe.shell.commands;

import lombok.Getter;
import lombok.ToString;
import org.taranix.cafe.beans.annotations.classes.CafeService;

import java.util.Map;

@CafeService
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
