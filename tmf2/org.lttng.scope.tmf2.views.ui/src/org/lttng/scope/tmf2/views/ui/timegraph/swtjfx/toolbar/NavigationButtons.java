/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.tmf2.views.ui.timegraph.swtjfx.toolbar;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.Nullable;
import org.lttng.scope.tmf2.views.ui.jfx.JfxImageFactory;
import org.lttng.scope.tmf2.views.ui.timegraph.swtjfx.SwtJfxTimeGraphViewer;

import com.google.common.collect.ImmutableList;

import javafx.scene.control.Button;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.SplitMenuButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

/**
 * Class encapsulating the fowards/backwards navigation buttons, with support
 * for different "modes" of navigation.
 *
 * @author Alexandre Montplaisir
 */
class NavigationButtons {

    // ------------------------------------------------------------------------
    // Inner classes
    // ------------------------------------------------------------------------

    private static class NavigationMode {

        private final String fModeName;
        private final @Nullable Image fBackIcon;
        private final @Nullable Image fForwardIcon;

        private final Consumer<SwtJfxTimeGraphViewer> fGoBackwards;
        private final Consumer<SwtJfxTimeGraphViewer> fGoForwards;

        public NavigationMode(String modeName, String backIconPath, String forwardIconPath,
                Consumer<SwtJfxTimeGraphViewer> goBackwards, Consumer<SwtJfxTimeGraphViewer> goForwards) {
            fModeName = modeName;

            JfxImageFactory factory = JfxImageFactory.instance();
            fBackIcon = factory.getImageFromResource(backIconPath);
            fForwardIcon = factory.getImageFromResource(forwardIconPath);

            fGoBackwards = goBackwards;
            fGoForwards = goForwards;
        }

        public String getModeName() {
            return fModeName;
        }

        public @Nullable Image getBackIcon() {
            return fBackIcon;
        }

        public @Nullable Image getForwardIcon() {
            return fForwardIcon;
        }

        public void navigateBackwards(SwtJfxTimeGraphViewer viewer) {
            fGoBackwards.accept(viewer);
        }

        public void navigateForwards(SwtJfxTimeGraphViewer viewer) {
            fGoForwards.accept(viewer);
        }
    }

    private static class BackButton extends Button {

        private final SwtJfxTimeGraphViewer fViewer;

        public BackButton(SwtJfxTimeGraphViewer viewer, NavigationMode initialNavMode) {
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

    private static class ForwardButton extends SplitMenuButton {

        private final SwtJfxTimeGraphViewer fViewer;

        public ForwardButton(SwtJfxTimeGraphViewer viewer, NavigationMode initialNavMode) {
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

    private static class NavigationModeMenuItem extends RadioMenuItem {

        public NavigationModeMenuItem(NavigationMode nm) {
            setText(nm.getModeName());
            setGraphic(new HBox(new ImageView(nm.getBackIcon()), new ImageView(nm.getForwardIcon())));
        }
    }

    // ------------------------------------------------------------------------
    // Pre-defined navigation modes
    // ------------------------------------------------------------------------

    private static final String FOLLOW_STATECHANGE_BACK_ICON_PATH = "/icons/toolbar/nav_statechange_back.gif"; //$NON-NLS-1$
    private static final String FOLLOW_STATECHANGE_FWD_ICON_PATH = "/icons/toolbar/nav_statechange_fwd.gif"; //$NON-NLS-1$
    private static final String FOLLOW_EVENT_BACK_ICON_PATH = "/icons/toolbar/nav_event_back.gif"; //$NON-NLS-1$
    private static final String FOLLOW_EVENT_FWD_ICON_PATH = "/icons/toolbar/nav_event_fwd.gif"; //$NON-NLS-1$
    private static final String FOLLOW_ARROW_BACK_ICON_PATH = "/icons/toolbar/nav_arrow_back.gif"; //$NON-NLS-1$
    private static final String FOLLOW_ARROW_FWD_ICON_PATH = "/icons/toolbar/nav_arrow_fwd.gif"; //$NON-NLS-1$
    private static final String FOLLOW_BOOKMARK_BACK_ICON_PATH = "/icons/toolbar/nav_bookmark_back.gif"; //$NON-NLS-1$
    private static final String FOLLOW_BOOKMARK_FWD_ICON_PATH = "/icons/toolbar/nav_bookmark_fwd.gif"; //$NON-NLS-1$

    private static final NavigationMode FOLLOW_STATE_CHANGES_MODE =
            new NavigationMode(requireNonNull(Messages.sfFollowStateChangesNavModeName),
                    FOLLOW_STATECHANGE_BACK_ICON_PATH,
                    FOLLOW_STATECHANGE_FWD_ICON_PATH,
                    v ->   {
                        System.out.println("Follow state changes backwards");
                    },
                    v -> {
                        System.out.println("Follow state changes forwards");
                    });

    private static final NavigationMode FOLLOW_EVENTS_MODE =
            new NavigationMode(requireNonNull(Messages.sfFollowEventsNavModeName),
                    FOLLOW_EVENT_BACK_ICON_PATH,
                    FOLLOW_EVENT_FWD_ICON_PATH,
                    v -> {
                        System.out.println("Follow events backwards");
                    },
                    v -> {
                        System.out.println("Follow events forwards");
                    });

    private static final NavigationMode FOLLOW_ARROWS_MODE =
            new NavigationMode(requireNonNull(Messages.sfFollowArrowsNavModeName),
                    FOLLOW_ARROW_BACK_ICON_PATH,
                    FOLLOW_ARROW_FWD_ICON_PATH,
                    v ->   {
                        System.out.println("Follow arrows backwards");
                    },
                    v -> {
                        System.out.println("Follow arrows forwards");
                    });

    private static final NavigationMode FOLLOW_BOOKMARKS_MODE =
            new NavigationMode(requireNonNull(Messages.sfFollowBookmarksNavModeName),
                    FOLLOW_BOOKMARK_BACK_ICON_PATH,
                    FOLLOW_BOOKMARK_FWD_ICON_PATH,
                    v ->   {
                        System.out.println("Follow bookmarks backwards");
                    },
                    v -> {
                        System.out.println("Follow bookmarks forwards");
                    });


    private static final List<NavigationMode> NAVIGATION_MODES = ImmutableList.of(
            FOLLOW_STATE_CHANGES_MODE,
            FOLLOW_EVENTS_MODE,
            FOLLOW_ARROWS_MODE,
            FOLLOW_BOOKMARKS_MODE);

    // ------------------------------------------------------------------------
    // Class components
    // ------------------------------------------------------------------------

    private final Button fBackButton;
    private final SplitMenuButton fForwardButton;

    public NavigationButtons(SwtJfxTimeGraphViewer viewer) {
        List<NavigationMode> navModes = NAVIGATION_MODES;
        NavigationMode initialMode = navModes.get(0);

        BackButton backButton = new BackButton(viewer, initialMode);
        ForwardButton forwardButton = new ForwardButton(viewer, initialMode);

        ToggleGroup tg = new ToggleGroup();
        List<RadioMenuItem> items = NAVIGATION_MODES.stream()
                .map(nm -> {
                    NavigationModeMenuItem item = new NavigationModeMenuItem(nm);
                    item.setToggleGroup(tg);
                    item.setOnAction(e -> {
                        backButton.setNavMode(nm);
                        forwardButton.setNavMode(nm);
                    });
                    return item;
                })
                .collect(Collectors.toList());

        items.get(0).setSelected(true);
        forwardButton.getItems().addAll(items);

        fBackButton = backButton;
        fForwardButton = forwardButton;
    }

    public Button getBackButton() {
        return fBackButton;
    }

    public SplitMenuButton getForwardButton() {
        return fForwardButton;
    }
}
