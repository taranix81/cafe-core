package org.taranix.cafe.shell.configuration;

import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.taranix.cafe.beans.annotations.CafeFactory;
import org.taranix.cafe.beans.annotations.CafeProvider;

@CafeFactory
public class CafeShellFactory {

    @CafeProvider
    CommandLineParser getCommandLineParser() {
        return new DefaultParser();
    }

    @CafeProvider
    Options getOptions() {
        return new Options();
    }

    @CafeProvider
    HelpFormatter getHelpFormatter() {
        return new HelpFormatter();
    }
}
