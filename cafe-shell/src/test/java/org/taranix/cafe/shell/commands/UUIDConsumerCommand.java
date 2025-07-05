package org.taranix.cafe.shell.commands;

import lombok.extern.slf4j.Slf4j;
import org.taranix.cafe.shell.annotations.CafeCommand;
import org.taranix.cafe.shell.annotations.CafeCommandRun;

import java.util.List;
import java.util.UUID;

@CafeCommand(command = "c", description = "Processing UUID", noOfArgs = 1, argumentName = "argName")
@Slf4j
public class UUIDConsumerCommand {


    @CafeCommandRun
    public void run(List<UUID> uuids, CafeCommandArguments arguments) {
        log.info("{}", arguments);
        uuids.forEach(uuid -> log.info("I got {}", uuid));
    }
}
