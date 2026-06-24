package org.taranix.cafe.desktop.components.application;

import org.taranix.cafe.desktop.components.Component;
import org.taranix.cafe.desktop.components.ContainerComponent;

import java.util.Optional;

/**
 * Root of the component hierarchy in a cafe-desktop application.
 *
 * <p>{@code ApplicationComponent} owns the top-level {@link ContainerComponent}s
 * (e.g. a tab folder, a split pane) and acts as the single entry point for
 * application lifecycle and cross-container navigation. The hierarchy is built
 * at runtime: each container self-registers via {@link #registerContainer} during
 * its {@code @CafePostInit} phase, so no global registry is required.
 *
 * <pre>
 *   ApplicationComponent
 *     └─ CTabFolderContainer   (registered via registerContainer)
 *          ├─ TextEditorTab    (child form – owned by the container)
 *          └─ TextEditorTab
 * </pre>
 *
 * <p>Extends {@link ContainerComponent}: all component-management methods
 * ({@code addComponent}, {@code removeComponent}, {@code isOpen}, {@code activate})
 * delegate to the registered child containers.
 */
public interface ApplicationComponent extends ContainerComponent {

    /**
     * Starts the SWT event loop and blocks until the application window is closed.
     * Call this from the main thread after DI initialisation is complete.
     */
    void start();

    /**
     * Closes the application window and exits the event loop started by {@link #start()}.
     */
    void shutDown();

    /**
     * Returns the active component of the given type from the active container,
     * if one exists.
     *
     * @return an {@link Optional} containing the active component, or empty
     */
    Component getActiveComponent();


}
