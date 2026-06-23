package org.taranix.cafe.desktop.components.containers.ctabfolder;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabFolder2Adapter;
import org.eclipse.swt.custom.CTabFolderEvent;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Widget;
import org.taranix.cafe.beans.annotations.fields.CafeInject;
import org.taranix.cafe.beans.annotations.methods.CafeHandler;
import org.taranix.cafe.beans.events.EventHub;
import org.taranix.cafe.desktop.annotations.CafeComponent;
import org.taranix.cafe.desktop.components.Component;
import org.taranix.cafe.desktop.components.ComponentFactory;
import org.taranix.cafe.desktop.components.Form;
import org.taranix.cafe.desktop.components.containers.ContainerComponent;
import org.taranix.cafe.desktop.events.CafeMenuEvent;
import org.taranix.cafe.desktop.widgets.MessageBoxService;

import java.util.Optional;

import static org.taranix.cafe.desktop.components.ComponentFactory.COMPONENT;

/**
 * A ContainerComponent backed by SWT CTabFolder.
 * Registers itself with ApplicationComponent on init so the application
 * owns the full component hierarchy without a global registry.
 * <p>
 * Routing rules:
 * - CafeMenuEvent             → active child form only (IDE command routing)
 * - CafeEvent                 → all child forms (broadcast)
 * - ComponentDirtyChangedEvent → updates matching tab's title prefix
 */

@Slf4j
@CafeComponent
public class CTabFolderContainer implements ContainerComponent, Form {

    @CafeInject
    private EventHub eventHub;
    @CafeInject
    private ComponentFactory factory;
    private CTabFolder tabFolder;

    @CafeInject
    private MessageBoxService messageBoxService;

    @CafeInject
    private Optional<CTabFolderFileOperations> folderFileOperations;

    @Override
    public Widget create(Composite parent) {
        tabFolder = new CTabFolder(parent, SWT.BORDER | SWT.CLOSE);
        tabFolder.setSimple(false);
        tabFolder.setData(COMPONENT, this);

        tabFolder.addCTabFolder2Listener(new CTabFolder2Adapter() {
            @Override
            public void close(CTabFolderEvent event) {
                if (event.item instanceof CTabItem tabItem) {
                    Object o = tabItem.getData(COMPONENT);
                    if (o instanceof Component c) {
                        c.dispose();
                    }
                }
            }
        });

        tabFolder.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                updateActiveTab();
            }
        });
        return tabFolder;
    }


    @Override
    public void dispose() {
    }

    @CafeHandler
    void onMenuEvent(CafeMenuEvent event) {
        String menuID = event.menuId();
        log.debug("Received menu event : {}", menuID);

        if ("file.new".equals(menuID)) {
            folderFileOperations.ifPresentOrElse(
                    e -> e.newFile(this),
                    () -> messageBoxService.showWarningDialog(tabFolder.getShell(), "No configuration for menu id : " + menuID, "Missing configuration")
            );
            return;
        }

        if ("file.open".equals(menuID)) {
            folderFileOperations.ifPresentOrElse(e -> e.open(this),
                    () -> messageBoxService.showWarningDialog(tabFolder.getShell(), "No configuration for menu id : " + menuID, "Missing configuration")
            );
            return;
        }

        //others
        routeToActiveTab(event);
    }

    private void routeToActiveTab(CafeMenuEvent event) {
        Optional.ofNullable(getActiveComponent())
                .ifPresent(component -> eventHub.send(event, component));
    }


    @Override
    public Component getActiveComponent() {
        if (tabFolder == null || tabFolder.isDisposed()) {
            return null;
        }
        CTabItem selected = tabFolder.getSelection();
        if (selected == null) {
            log.debug("No active component found");
            return null;
        }
        Object data = selected.getControl().getData(COMPONENT);
        log.debug("Active component : {}", data);
        return data instanceof Component c ? c : null;
    }

    public <T extends Component> T openTab(String title, Class<T> componentType) {
        if (tabFolder == null || tabFolder.isDisposed()) {
            return null;
        }
        Widget w = factory.create(componentType, tabFolder);
        if (w instanceof Control control) {
            CTabItem tabItem = new CTabItem(tabFolder, SWT.CLOSE);
            tabItem.setText(title);
            tabItem.setControl(control);
            tabFolder.setSelection(tabItem);
            return (T) w.getData(COMPONENT);
        } else {
            log.debug("Widget not an instance of Control");
            w.dispose();
        }
        return null;


    }


    // ── Helpers ───────────────────────────────────────────────────────────
    private void updateActiveTab() {
        if (tabFolder == null || tabFolder.isDisposed()) return;
        CTabItem selected = tabFolder.getSelection();
        log.debug("Selects : {}", selected);
    }


//    Set<Component> getComponents() {
//        return Arrays.stream(tabFolder.getItems())
//                .map(cTabItem -> cTabItem.getData("component"))
//                .filter(o -> o instanceof Component)
//                .map(Component.class::cast)
//                .collect(Collectors.toSet());
//    }

}
