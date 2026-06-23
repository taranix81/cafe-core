package org.taranix.cafe.desktop.examples.notebook;

import org.taranix.cafe.beans.annotations.classes.CafeApplication;
import org.taranix.cafe.desktop.CafeDesktopApplication;

/**
 * MDI text editor POC — Notepad++ style.
 * Run this class to launch the Notebook application.
 */
@CafeApplication
public class NotebookApplication {

    public static void main(String[] args) {
        new CafeDesktopApplication(NotebookApplication.class).run(args);
    }
}
