package org.taranix.cafe.shell.services;

import org.apache.commons.cli.Option;
import org.taranix.cafe.beans.annotations.fields.CafeInject;
import org.taranix.cafe.beans.annotations.classes.CafeService;
import org.taranix.cafe.shell.CafeShell;
import org.taranix.cafe.shell.commands.CafeCommandOptionBinding;
import org.taranix.cafe.shell.exceptions.CafeCommandBindingServiceException;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@CafeService
public class CafeCommandBindingService {

    @CafeInject
    private CafeShell cafeShell;

    /*
       CommandBinding is being created during resolving @aAfeCommand class.
       List of CommandBinding is available after all classes will be resolved.
       Cannot direct inject as ClassResolver couldn't find relation in class descriptors.
    */
    public Collection<CafeCommandOptionBinding> getCommandBindings() {
        return cafeShell.getInstances(CafeCommandOptionBinding.class);
    }

    public CafeCommandOptionBinding noOptedCommandBinding() {
        Set<CafeCommandOptionBinding> noOpted = getCommandBindings().stream()
                .filter(commandOptionBinding -> commandOptionBinding.getOptionBinding() == null)
                .collect(Collectors.toSet());

        if (noOpted.size() > 1) {
            throw new CafeCommandBindingServiceException("Handling more than one default command is not supported");
        }

        return noOpted.stream()
                .findFirst()
                .orElse(null);
    }

    public Optional<CafeCommandOptionBinding> noOptedCommandBinding(Option option) {
        return getCommandBindings()
                .stream()
                .filter(commandOptionBinding -> commandOptionBinding.getOptionBinding() != null)
                .filter(commandOptionBinding -> commandOptionBinding.getOptionBinding().getOpt().equals(option.getOpt()))
                .findFirst();
    }

}
