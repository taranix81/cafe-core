package org.taranix.cafe.shell.commands;

import lombok.extern.slf4j.Slf4j;
import org.taranix.cafe.shell.annotations.CafeCommand;
import org.taranix.cafe.shell.annotations.CafeCommandRun;

import java.util.UUID;

@CafeCommand(command = "u", description = "Random UUID", hasOptionalArgument = true, noOfArgs = 1)
@Slf4j
public class RandomUUIDCommand {
    @CafeCommandRun
    public UUID run() {
        UUID generated = UUID.randomUUID();
        log.info("Generated UUID:{}", generated);
        return generated;
    }


}
