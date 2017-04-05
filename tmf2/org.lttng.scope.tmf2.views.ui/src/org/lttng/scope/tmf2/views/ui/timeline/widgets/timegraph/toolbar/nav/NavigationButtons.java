/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.tmf2.views.ui.timeline.widgets.timegraph.toolbar.nav;

import java.util.List;
import java.util.stream.Collectors;

import org.lttng.scope.tmf2.views.ui.timeline.widgets.timegraph.TimeGraphWidget;

import com.google.common.collect.ImmutableList;

import javafx.scene.control.Button;
import javafx.scene.control.MenuButton;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

/**
 * Class encapsulating the fowards/backwards navigation buttons, with support
 * for different "modes" of navigation.
 *
 * @author Alexandre Montplaisir
 */
public class NavigationButtons {

    // ------------------------------------------------------------------------
    // Inner classes
    // ------------------------------------------------------------------------

    private static class BackButton extends Button {

        private final TimeGraphWidget fViewer;

        public BackButton(TimeGraphWidget viewer, NavigationMode initialNavMode) {
            fViewer = viewer;
            setNavMode(initialNavMode);
        }

        public void setNavMode(NavigationMode navMode) {
            setGraphic(new ImageView(navMode.getBackIcon()));
            setOnAction(e -> {
                navMode.navigateBackwards(fViewer);
            });
        }
    }

    private static class ForwardButton extends Button {

        private final TimeGraphWidget fViewer;

        public ForwardButton(TimeGraphWidget viewer, NavigationMode initialNavMode) {
            fViewer = viewer;
            setNavMode(initialNavMode);
        }

        public void setNavMode(NavigationMode navMode) {
            setGraphic(new ImageView(navMode.getForwardIcon()));
            setOnAction(e -> {
                navMode.navigateForwards(fViewer);
            });
        }
    }

    // ------------------------------------------------------------------------
    // Pre-defined navigation modes
    // ------------------------------------------------------------------------

    private static final List<NavigationMode> NAVIGATION_MODES = ImmutableList.of(
            new NavigationModeFollowStateChanges(),
            new NavigationModeFollowEvents(),
            new NavigationModeFollowArrows(),
            new NavigationModeFollowBookmarks());

    // ------------------------------------------------------------------------
    // Class components
    // ------------------------------------------------------------------------

    private final Button fBackButton;
    private final Button fForwardButton;
    private final MenuButton fMenuButton;

    /**
     * Constructor. This will prepare the buttons, which can then be retrieved
     * with {@link #getBackButton()}, {@link #getForwardButton()} and
     * {@link #getMenuButton()}.
     *
     * @param viewer
     *            The viewer to which the buttons will belong
     */
    public NavigationButtons(TimeGraphWidget viewer) {
        List<NavigationMode> navModes = NAVIGATION_MODES;
        NavigationMode initialMode = navModes.get(0);

        BackButton backButton = new BackButton(viewer, initialMode);
        ForwardButton forwardButton = new ForwardButton(viewer, initialMode);
        MenuButton menuButton = new MenuButton();

        ToggleGroup tg = new ToggleGroup();
        List<RadioMenuItem> items = NAVIGATION_MODES.stream()
                .map(nm -> {
                    RadioMenuItem item = new RadioMenuItem();
                    item.setText(nm.getModeName());
                    item.setGraphic(new HBox(new ImageView(nm.getBackIcon()), new ImageView(nm.getForwardIcon())));
                    item.setToggleGroup(tg);
                    item.setOnAction(e -> {
                        backButton.setNavMode(nm);
                        forwardButton.setNavMode(nm);
                    });
                    return item;
                })
                .collect(Collectors.toList());

        items.get(0).setSelected(true);
        menuButton.getItems().addAll(items);

        fBackButton = backButton;
        fForwardButton = forwardButton;
        fMenuButton = menuButton;
    }

    /**
     * Get the "back" button.
     *
     * @return The back button
     */
    public Button getBackButton() {
        return fBackButton;
    }

    /**
     * Get the "forward" button.
     *
     * @return The forward button
     */
    public Button getForwardButton() {
        return fForwardButton;
    }

    /**
     * Get the drop-down menu button, which allows switching between the
     * available navigation modes.
     *
     * @return The drop-down menu button
     */
    public MenuButton getMenuButton() {
        return fMenuButton;
    }
}
