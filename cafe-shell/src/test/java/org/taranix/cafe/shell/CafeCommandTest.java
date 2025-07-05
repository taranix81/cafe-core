package org.taranix.cafe.shell;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.taranix.cafe.shell.app.TestShellApplication;

import java.util.List;
import java.util.stream.Stream;

class CafeCommandTest {

    private static Stream<Arguments> correctCommandArguments() {
        return Stream.of(
                Arguments.of(List.of("no_opted_command")),
                Arguments.of(List.of("-u")),
                Arguments.of(List.of("-u", "-u")),
                Arguments.of(List.of("-u", "-u", "-c", "2")),
                Arguments.of(List.of("-c", "1", "-u")),
                Arguments.of(List.of("-c", "1", "-u", "-c", "3"))
        );
    }

    private static Stream<Arguments> wrongCommandArguments() {
        return Stream.of(
                Arguments.of(List.of("-x")),
                Arguments.of(List.of("-c"))
        );
    }


    @DisplayName("Should execute commands without exception")
    @ParameterizedTest
    @MethodSource("correctCommandArguments")
    void shouldExecuteSuccessfullyCommands(List<String> arguments) {
        //given
        CafeShell cafeShell = new CafeShell(TestShellApplication.class);
        //when
        int result = cafeShell.run(arguments.toArray(new String[]{}));
        // then
        Assertions.assertNotNull(cafeShell);
        Assertions.assertEquals(0, result);
    }


    @DisplayName("Should failed execution by parser exception")
    @ParameterizedTest
    @MethodSource("wrongCommandArguments")
    void shouldFailExecutionByParserException(List<String> arguments) {
        //given
        CafeShell cafeShell = new CafeShell(TestShellApplication.class);
        //when
        int result = cafeShell.run(arguments.toArray(new String[]{}));
        // then
        Assertions.assertNotNull(cafeShell);
        Assertions.assertEquals(2, result);

    }
}
