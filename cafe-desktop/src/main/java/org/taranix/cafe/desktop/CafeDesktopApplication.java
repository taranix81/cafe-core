package org.taranix.cafe.desktop;

import org.taranix.cafe.beans.CafeApplication;
import org.taranix.cafe.beans.annotations.fields.CafeInject;
import org.taranix.cafe.desktop.components.application.ApplicationComponent;

public class CafeDesktopApplication extends CafeApplication {

    @CafeInject
    private ApplicationComponent applicationComponent;

    public CafeDesktopApplication(Class<?> applicationConfigClass) {
        super(applicationConfigClass);
    }

    @Override
    protected void beforeContextInit() {
        addBeanToContext(getBeansFactory());
        super.beforeContextInit();
    }

//    @Override
//    protected Set<CafeClassResolver> getCustomClassResolvers() {
//        return Set.of(new CafeComponentClassResolver());
//    }


    @Override
    protected int execute(String[] args) {
        applicationComponent.start();
        return 0;
    }
}
