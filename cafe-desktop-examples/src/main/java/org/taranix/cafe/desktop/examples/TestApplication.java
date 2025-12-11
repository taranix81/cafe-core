package org.taranix.cafe.desktop.examples;

import org.taranix.cafe.beans.annotations.CafeApplication;
import org.taranix.cafe.desktop.CafeDesktopApplication;

@CafeApplication
public class TestApplication {

    public static void main(String[] args) {

        CafeDesktopApplication cafeDesktopApplication = new CafeDesktopApplication(TestApplication.class);
        cafeDesktopApplication.run(args);
    }
}
