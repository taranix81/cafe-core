package org.taranix.cafe.shell.commands;

import lombok.Builder;
import lombok.Getter;
import org.apache.commons.cli.Option;
import org.taranix.cafe.beans.metadata.members.CafeMethodInfo;

import java.util.Optional;

@Getter
@Builder
public class CafeCommandOptionBinding {

    private Object commandInstance;
    private CafeMethodInfo executor;
    private Option optionBinding;

    public CafeCommandRuntime asCommandRuntime(String[] args) {
        return CafeCommandRuntime.builder()
                .commandInstance(this.commandInstance)
                .executor(this.executor)
                .arguments(Optional.ofNullable(args).orElse(new String[]{}))
                .build();
    }
}
