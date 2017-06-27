/*******************************************************************************
 * Copyright (c) 2016 EfficiOS Inc., Alexandre Montplaisir
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.lttng.scope.tmf2.views.ui.timeline;

import static java.util.Objects.requireNonNull;

import java.util.Collection;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tracecompass.tmf.ui.views.TmfView;
import org.lttng.scope.common.core.StreamUtils;
import org.lttng.scope.tmf2.views.core.context.ViewGroupContext;

import javafx.embed.swt.FXCanvas;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.SplitPane;

public class TimelineView extends TmfView {

    public static final String VIEW_ID = "org.lttng.scope.views.timeline"; //$NON-NLS-1$

    private static final String VIEW_NAME = requireNonNull(Messages.timelineViewName);

    private @Nullable TimelineManager fManager;

    public TimelineView() {
        super(VIEW_NAME);
    }

    @Override
    public void createPartControl(@Nullable Composite parent) {
        if (parent == null) {
            return;
        }

        FXCanvas fxCanvas = new FXCanvas(parent, SWT.NONE);
        SplitPane sp = new SplitPane();
        sp.setOrientation(Orientation.VERTICAL);

        /* Add the widget to the view */
        TimelineManager manager = new TimelineManager(ViewGroupContext.getCurrent());
        Collection<Node> nodes = StreamUtils.getStream(manager.getWidgets())
                .map(widget -> widget.getRootNode())
                .collect(Collectors.toList());
        sp.getItems().addAll(nodes);

        fxCanvas.setScene(new Scene(sp));

        /*
         * Set the initial divider positions. Has to be done *after* the
         * Stage/Scene is initialized.
         */
        manager.resetInitialSeparatorPosition();

        fManager = manager;
    }

    @Override
    public void dispose() {
        if (fManager != null) {
            fManager.dispose();
        }
    }

    @Override
    public void setFocus() {
    }

}
