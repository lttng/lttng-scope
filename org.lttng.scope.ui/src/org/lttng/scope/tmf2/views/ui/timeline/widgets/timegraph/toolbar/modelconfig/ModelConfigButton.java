/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.tmf2.views.ui.timeline.widgets.timegraph.toolbar.modelconfig;

import org.lttng.scope.tmf2.views.ui.jfx.JfxImageFactory;
import org.lttng.scope.tmf2.views.ui.jfx.JfxUtils;
import org.lttng.scope.tmf2.views.ui.timeline.widgets.timegraph.TimeGraphWidget;

import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * Button to open the legend mapping states to colors.
 *
 * @author Alexandre Montplaisir
 */
public class ModelConfigButton extends Button {

    private static final String LEGEND_ICON_PATH = "/icons/toolbar/legend.gif"; //$NON-NLS-1$

    /**
     * Constructor
     *
     * @param widget
     *            The time graph widget to which this toolbar button is
     *            associated.
     */
    public ModelConfigButton(TimeGraphWidget widget) {
        Image icon = JfxImageFactory.instance().getImageFromResource(LEGEND_ICON_PATH);
        setGraphic(new ImageView(icon));
        setTooltip(new Tooltip(Messages.modelConfigButtonName));

        setOnAction(e -> {
            Dialog<?> dialog = new ModelConfigDialog(widget);
            dialog.show();
            JfxUtils.centerDialogOnScreen(dialog, ModelConfigButton.this);
        });
    }

}
