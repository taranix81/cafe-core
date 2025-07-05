package org.taranix.cafe.graphics;

import lombok.extern.slf4j.Slf4j;
import org.taranix.cafe.beans.CafeApplication;
import org.taranix.cafe.beans.annotations.CafeInject;
import org.taranix.cafe.graphics.components.ApplicationComponent;


@Slf4j
public class CafeGUIApplication extends CafeApplication {

    @CafeInject
    private ApplicationComponent applicationComponent;

    public CafeGUIApplication(Class<?> applicationConfigClass) {
        super(applicationConfigClass);
    }

    @Override
    protected void beforeContextInit() {
        super.beforeContextInit();
        addBeanToContext(getBeansFactory());
    }

    @Override
    protected int execute(String[] strings) {
        applicationComponent.start();
        return 0;
    }


}
