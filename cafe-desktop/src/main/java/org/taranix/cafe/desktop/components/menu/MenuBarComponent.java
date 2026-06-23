package org.taranix.cafe.desktop.components.menu;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.*;
import org.taranix.cafe.beans.annotations.fields.CafeInject;
import org.taranix.cafe.beans.annotations.methods.CafeHandler;
import org.taranix.cafe.beans.events.EventHub;
import org.taranix.cafe.desktop.annotations.CafeComponent;
import org.taranix.cafe.desktop.components.Component;
import org.taranix.cafe.desktop.components.Form;
import org.taranix.cafe.desktop.components.application.ApplicationComponent;
import org.taranix.cafe.desktop.components.menu.model.MenuBuilder;
import org.taranix.cafe.desktop.components.menu.model.PropertiesMenuModel;
import org.taranix.cafe.desktop.events.CafeMenuEvent;

import java.util.Optional;

import static org.taranix.cafe.desktop.components.ComponentFactory.COMPONENT;

@CafeComponent
public final class MenuBarComponent implements Form, Component {

    @CafeInject
    private Optional<PropertiesMenuModel> propertiesMenuModel;

    @CafeInject
    private EventHub eventHub;

    @Override
    public Widget create(Composite parent) {
        Menu bar = propertiesMenuModel
                .map(PropertiesMenuModel::getMenuBarModel)
                .map(model -> new MenuBuilder(eventHub).build((Shell) parent, model))
                .orElseGet(() -> new Menu((Decorations) parent, SWT.BAR));
        ((Decorations) parent).setMenuBar(bar);
        bar.setData(COMPONENT, this);
        bar.addDisposeListener(de -> {
            this.dispose();
        });
        return bar;
    }

    @CafeHandler
    void onSelection(CafeMenuEvent cafeMenuEvent) {
        eventHub.send(cafeMenuEvent, ApplicationComponent.class);
    }

    @Override
    public void dispose() {
        // do not call widge dispose as flow is SWT widget(dispose) -> Component(dispose)
    }
}
