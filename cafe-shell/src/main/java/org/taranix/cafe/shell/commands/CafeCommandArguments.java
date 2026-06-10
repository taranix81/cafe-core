package org.taranix.cafe.shell.commands;

import lombok.Getter;
import lombok.ToString;
import org.taranix.cafe.beans.annotations.classes.CafeSingleton;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@CafeSingleton
@ToString
public class CafeCommandArguments {

    private String[] cliValues = new String[0];

    @Getter
    private Map<String, Object> variables = new HashMap<>();

    public void setValues(String[] values) {
        this.cliValues = values != null ? values : new String[0];
    }

    public boolean hasArguments() {
        return cliValues.length > 0;
    }

    public boolean isEmpty() {
        return cliValues.length == 0;
    }

    public Optional<String[]> getValues() {
        return cliValues.length > 0 ? Optional.of(cliValues) : Optional.empty();
    }

    public Optional<String> getValue(int index) {
        return index >= 0 && index < cliValues.length ? Optional.of(cliValues[index]) : Optional.empty();
    }

    public <T> Optional<T> getVariable(Class<T> type) {
        Object value = variables.get(type.getSimpleName());
        return type.isInstance(value) ? Optional.of(type.cast(value)) : Optional.empty();
    }

    public void reset() {
        this.cliValues = new String[0];
        this.variables = new HashMap<>();
    }

}
