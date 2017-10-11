/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.views.timeline.widgets.timegraph.toolbar.debugopts;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.lttng.scope.views.jfx.JfxImageFactory;
import org.lttng.scope.views.jfx.JfxUtils;
import org.lttng.scope.views.timeline.DebugOptions;
import org.lttng.scope.views.timeline.widgets.timegraph.TimeGraphWidget;

/**
 * Button to open the debug options dialog.
 *
 * @author Alexandre Montplaisir
 */
public class DebugOptionsButton extends Button {

    private static final String CONFIG_ICON_PATH = "/icons/toolbar/config.gif"; //$NON-NLS-1$

    /*
     * Since the button is more persistent than the dialog, track state like the
     * last-selected tab in here.
     */
    private final IntegerProperty fLastSelectedDialogTab = new SimpleIntegerProperty(0);

    private final DebugOptions fOpts;

    /**
     * Constructor
     *
     * @param widget
     *            The time graph widget to which this toolbar button is
     *            associated.
     */
    public DebugOptionsButton(TimeGraphWidget widget) {
        Image icon = JfxImageFactory.instance().getImageFromResource(CONFIG_ICON_PATH);
        setGraphic(new ImageView(icon));
        setTooltip(new Tooltip(Messages.debugOptionsDialogName));

        fOpts = widget.getDebugOptions();

        setOnAction(e -> {
            Dialog<Void> dialog = new DebugOptionsDialog(this);
            dialog.show();
            JfxUtils.centerDialogOnScreen(dialog, DebugOptionsButton.this);
        });
    }

    DebugOptions getDebugOptions() {
        return fOpts;
    }

    IntegerProperty lastSelectedDialogProperty() {
        return fLastSelectedDialogTab;
    }
}
