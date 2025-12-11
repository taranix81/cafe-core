package org.taranix.cafe.desktop;

import lombok.extern.slf4j.Slf4j;
import org.taranix.cafe.beans.CafeApplication;
import org.taranix.cafe.beans.annotations.CafeInject;
import org.taranix.cafe.beans.resolvers.classInfo.method.CafeMethodResolver;
import org.taranix.cafe.desktop.actions.HandlersService;
import org.taranix.cafe.desktop.annotations.CafeMenuItemSelectionHandler;
import org.taranix.cafe.desktop.annotations.CafeShellHandler;
import org.taranix.cafe.desktop.components.application.ApplicationComponent;
import org.taranix.cafe.desktop.components.menubar.ApplicationMenuBarComponent;
import org.taranix.cafe.desktop.resolvers.CafeMenuItemSelectionMethodResolver;
import org.taranix.cafe.desktop.resolvers.CafeShellEventMethodResolver;

import java.lang.annotation.Annotation;
import java.util.Set;


@Slf4j
public class CafeDesktopApplication extends CafeApplication {

    @CafeInject
    private ApplicationComponent applicationComponent;

    @CafeInject
    private ApplicationMenuBarComponent applicationMenuBarComponent;


    public CafeDesktopApplication(Class<?> applicationConfigClass) {
        super(applicationConfigClass);
    }

    @Override
    protected void beforeContextInit() {
        addBeanToContext(new HandlersService(getBeansFactory()));
        addBeanToContext(getBeansFactory()); //required bt Components factory
        super.beforeContextInit();
    }

    @Override
    protected Set<Class<? extends Annotation>> getCustomAnnotations() {
        return Set.of(CafeShellHandler.class, CafeMenuItemSelectionHandler.class);
    }

    @Override
    protected Set<CafeMethodResolver> getCustomMethodResolvers() {
        return Set.of(new CafeMenuItemSelectionMethodResolver(), new CafeShellEventMethodResolver());
    }

    @Override
    protected int execute(String[] strings) {
        applicationComponent.addComponent(applicationMenuBarComponent);
        applicationComponent.start();
        return 0;
    }


}
