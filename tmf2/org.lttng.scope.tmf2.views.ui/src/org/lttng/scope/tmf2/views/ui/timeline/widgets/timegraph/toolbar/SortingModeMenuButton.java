/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.tmf2.views.ui.timeline.widgets.timegraph.toolbar;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.lttng.scope.tmf2.views.core.timegraph.model.provider.ITimeGraphModelProvider;
import org.lttng.scope.tmf2.views.core.timegraph.model.provider.ITimeGraphModelProvider.SortingMode;
import org.lttng.scope.tmf2.views.ui.timeline.widgets.timegraph.TimeGraphWidget;

import javafx.scene.control.MenuButton;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.ToggleGroup;

/**
 * Menu button for selecting the sorting mode of tree entries.
 *
 * The available modes are defined by the time graph model. Only one mode can be
 * active at a time, so we are using RadioMenuItems for the menu items.
 *
 * @author Alexandre Montplaisir
 */
class SortingModeMenuButton extends MenuButton {

    public SortingModeMenuButton(TimeGraphWidget viewer) {
        ITimeGraphModelProvider provider = viewer.getControl().getModelRenderProvider();

        ToggleGroup tg = new ToggleGroup();
        List<RadioMenuItem> sortingModeItems = IntStream.range(0, provider.getSortingModes().size())
                .mapToObj(index -> {
                    SortingMode sm = provider.getSortingModes().get(index);
                    RadioMenuItem rmi = new RadioMenuItem(sm.getName());
                    rmi.setToggleGroup(tg);
                    rmi.setOnAction(e -> {
                        provider.setCurrentSortingMode(index);
                        viewer.getControl().repaintCurrentArea();
                    });
                    return rmi;
                })
                .collect(Collectors.toList());

        if (!sortingModeItems.isEmpty()) {
            /*
             * Initialize the first mode to be selected, which is what the model
             * does. This should not trigger the event handler.
             */
            sortingModeItems.get(0).setSelected(true);
        }

        setText(Messages.sfSortingModeMenuButtonName);
        getItems().addAll(sortingModeItems);

        /*
         * There is at minimum the "default" sorting mode. Don't show the list
         * if there is only that one.
         */
        if (sortingModeItems.size() <= 1) {
            setDisable(true);
        }
    }
}
