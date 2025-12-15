package org.taranix.cafe.shell;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.taranix.cafe.shell.app.TestShellApplication;

class CafeShellTests {

    @Test
    @DisplayName("Should create instance of CafeShell")
    @Disabled
    void shouldCreateApp() {
        //given
        CafeShell cafeShell = new CafeShell(TestShellApplication.class);
        //when-then
        Assertions.assertNotNull(cafeShell);
    }


    @Test
    @DisplayName("Should show help description as default action")
    @Disabled
    void shouldPrintHelp() {
        //given
        CafeShell cafeShell = new CafeShell(TestShellApplication.class);
        //when
        int result = cafeShell.run(null);
        // -then
        Assertions.assertEquals(0, result);
    }

}
