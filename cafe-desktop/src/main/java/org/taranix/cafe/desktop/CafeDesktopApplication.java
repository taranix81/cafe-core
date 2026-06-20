package org.taranix.cafe.desktop;

import org.taranix.cafe.beans.CafeApplication;
import org.taranix.cafe.beans.annotations.fields.CafeInject;
import org.taranix.cafe.beans.resolvers.metadata.CafeClassResolver;
import org.taranix.cafe.beans.resolvers.metadata.method.CafeMethodResolver;
import org.taranix.cafe.desktop.components.application.ApplicationComponent;
import org.taranix.cafe.desktop.resolvers.ActionHandlerMethodResolver;
import org.taranix.cafe.desktop.resolvers.CafeComponentClassResolver;
import org.taranix.cafe.desktop.resolvers.CafeMenuItemSelectionMethodResolver;
import org.taranix.cafe.desktop.resolvers.CafeShellEventMethodResolver;

import java.util.Set;

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

    @Override
    protected Set<CafeClassResolver> getCustomClassResolvers() {
        return Set.of(new CafeComponentClassResolver());
    }

    @Override
    protected Set<CafeMethodResolver> getCustomMethodResolvers() {
        return Set.of(new CafeMenuItemSelectionMethodResolver(), new CafeShellEventMethodResolver(), new ActionHandlerMethodResolver());
    }

    @Override
    protected int execute(String[] args) {
        applicationComponent.start();
        return 0;
    }
}
