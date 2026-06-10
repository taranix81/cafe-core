package org.taranix.cafe.shell.examples.web;

import org.taranix.cafe.beans.annotations.classes.CafeApplication;
import org.taranix.cafe.shell.CafeShell;

@CafeApplication
public class WebClientApplication {

    public static void main(String[] args) {
        CafeShell shell = new CafeShell(WebClientApplication.class);
        System.exit(shell.run(args));
    }
}
