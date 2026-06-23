import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Decorations;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Widget;
import org.taranix.cafe.beans.annotations.classes.CafeService;
import org.taranix.cafe.beans.annotations.fields.CafeInject;
import org.taranix.cafe.desktop.components.menu.MenuBuilder;
import org.eclipse.swt.widgets.Composite;


public class Example1 {

    @interface CafeComponent {

        Class<?> formType() default Void.class;
    }

    @interface CafeForm {
    }

    interface View {

        Widget createWidget(org.eclipse.swt.widgets.Composite composite);
    }

    interface Component {

        void dispose();
    }

    interface Container {

        void addComponent(Component component);

        void removeComponent(Component component);

        void activateComponent(Component component);

        void dispose(Component component);
    }

    interface MenuModelConfiguration {

        MenuModel configure(MenuModel menuModel);
    }

    interface MenuModelProvider {

        MenuModel provide();
    }

    interface TableColumnsModelProvider {
        TableColumnsModel provide();
    }

    //Menu
    class MenuModel {

        private List<MenuItemModel> menuItems;

        List<MenuItemModel> getMenuItems() {
            return menuItems;
        }
    }

    class MenuItemModel {

        private String id;
        private String label;
        private MenuModel subMenu;

        String getId() {
            return id;
        }

        String getLabel() {
            return label;
        }
    }

    //Menu 1
    @CafeComponent
    class MenuBarComponent1 implements Component, View {

        @CafeInject
        private MenuModelProvider provider;

        private MenuBuilder menuBuilder;

        public Widget createWidget(Composite composite) {
            MenuModel menuModel = provider.provide();
            Menu menu = menuBuilder.build((Decorations) composite, SWT.BAR, menuModel);
            return menu;
        }

        @Override
        public void dispose() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }

    // Menu 2 - alternative approach with separate form and component
    @CafeForm
    class MenuForm implements View {

        private MenuModelProvider provider;

        public Widget createWidget(Composite composite) {
            MenuModel menuModel = provider.provide();
            Menu menu = MenuBuilder.build((Decorations) composite, SWT.BAR, menuModel);
            return menu;
        }
    }

    @CafeComponent(formType = MenuForm.class)
    class MenuBarComponent2 implements Component {

        @CafeInject
        private MenuModelProvider provider;

        private MenuBuilder menuBuilder;

        public Widget createWidget(Composite composite) {
            MenuModel menuModel = provider.provide();
            Menu menu = menuBuilder.build((Decorations) composite, SWT.BAR, menuModel);
            return menu;
        }

        @Override
        public void dispose() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }

    // Table
    class TableColumnsModel {

        private List<String> columns;

        List<String> getColumns() {
            return columns;
        }
    }

    @CafeComponent
    class TableComponentExample1 implements Component, View {

        @CafeInject
        private TableColumnsModelProvider provider;

        public Widget createWidget(Composite composite) {
            // Create and return a table widget
            Table t = new Table(composite, SWT.NONE);
            buildColumns(t);
            return t;
        }

        void buildColumns(Table table) {
            TableColumnsModel model = provider.provide();
            for (String column : model.getColumns()) {
                // Create and add columns to the table based on the model
            }
        }

        @Override
        public void dispose() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }


    // Services
    @CafeService
    class ComponentService {

        Component createComponent(Class<?> componentClass) {
            /*
                if componentClass extends View then call it to create UI widget
                Otherwise check formType and use it found a singleton to create the UI widget, then create the component instance and return it
                UI widget should store component inside UI widget using setData("component", component)
             */

            // Implementation to create a component instance
            return null;
        }
    }
}
