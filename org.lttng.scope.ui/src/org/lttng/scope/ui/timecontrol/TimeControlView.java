/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.ui.timecontrol;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

import javafx.embed.swt.FXCanvas;
import javafx.scene.Scene;

public class TimeControlView extends ViewPart {

    public static final String VIEW_ID = "org.lttng.scope.views.timecontrol"; //$NON-NLS-1$

    @Override
    public void createPartControl(Composite parent) {
        if (parent == null) {
            return;
        }

        FXCanvas fxCanvas = new FXCanvas(parent, SWT.NONE);
        TimeControl tc = new TimeControl();

        fxCanvas.setScene(new Scene(tc));
    }

    @Override
    public void setFocus() {
    }

}
