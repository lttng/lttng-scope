/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.views.timeline.widgets.timegraph.toolbar;

import com.efficios.jabberwocky.views.timegraph.model.provider.ITimeGraphModelProvider;
import com.efficios.jabberwocky.views.timegraph.model.provider.ITimeGraphModelProvider.FilterMode;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.MenuButton;
import org.lttng.scope.views.timeline.widgets.timegraph.TimeGraphWidget;

import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Menu-button for listing the filter modes.
 *
 * The available filter modes come from the time graph model. More than one mode
 * (or none) can be active at the same time, so we are using CheckMenuItems for
 * the menu items.
 *
 * @author Alexandre Montplaisir
 */
class FilterModeMenuButton extends MenuButton {

    public FilterModeMenuButton(TimeGraphWidget viewer) {
        ITimeGraphModelProvider provider = viewer.getControl().getRenderProvider();

        Collection<CheckMenuItem> filterModeItems = IntStream.range(0, provider.getFilterModes().size())
                .mapToObj(index -> {
                    FilterMode fm = provider.getFilterModes().get(index);
                    CheckMenuItem cmi = new CheckMenuItem(fm.getName());
                    cmi.setOnAction(e -> {
                        if (cmi.isSelected()) {
                            /* Mode was enabled */
                            provider.enableFilterMode(index);
                        } else {
                            /* Mode was disabled */
                            provider.disableFilterMode(index);
                        }
                        viewer.getControl().repaintCurrentArea();
                    });
                    return cmi;
                })
                .collect(Collectors.toList());

        setText(Messages.sfFilterModeMenuButtonName);
        getItems().addAll(filterModeItems);

        if (filterModeItems.isEmpty()) {
            setDisable(true);
        }

        /* TODO Re-enable once filter modes are implemented and working */
        setDisable(true);
    }
}
