package org.taranix.cafe.beans.app;

import lombok.extern.slf4j.Slf4j;
import org.taranix.cafe.beans.annotations.CafeApplication;
import org.taranix.cafe.beans.annotations.CafePostInit;

import java.util.Date;

@CafeApplication
@Slf4j
public class CafeApplicationConfigurationWithPostConstructMethod {


    @CafePostInit
    void printDate(Date dateFromFactory) {
        log.info("Today is {}", dateFromFactory);
    }

    void printCliArgument(String[] cli) {
        log.info("Today is {}", cli);
    }

}
