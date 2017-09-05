/*******************************************************************************
 * Copyright (c) 2016 EfficiOS Inc., Alexandre Montplaisir
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.lttng.scope.ui.timeline;

import static java.util.Objects.requireNonNull;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;
import org.lttng.scope.ui.context.ViewGroupContextManager;
import org.lttng.scope.ui.jfx.JfxUtils;

import javafx.embed.swt.FXCanvas;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.SplitPane;

public class TimelineView extends ViewPart {

    public static final String VIEW_ID = "org.lttng.scope.views.timeline"; //$NON-NLS-1$

    private final Map<Node, Integer> itemWeights = new HashMap<>();

    private @Nullable TimelineManager fManager;
    private @Nullable SplitPane fSplitPane;

    public TimelineView() {
        super();
    }

    @Override
    public void createPartControl(@Nullable Composite parent) {
        if (parent == null) {
            return;
        }

        FXCanvas fxCanvas = new FXCanvas(parent, SWT.NONE);
        SplitPane sp = new SplitPane();
        sp.setOrientation(Orientation.VERTICAL);
        fSplitPane = sp;

        TimelineManager manager = new TimelineManager(this, ViewGroupContextManager.getCurrent());
        fManager = manager;

        fxCanvas.setScene(new Scene(sp));

        /*
         * Set the initial divider positions. Has to be done *after* the
         * Stage/Scene is initialized.
         */
        manager.resetInitialSeparatorPosition();
    }

    void addWidget(TimelineWidget widget) {
        SplitPane sp = requireNonNull(fSplitPane);
        JfxUtils.runOnMainThread(() -> {
            Node node = widget.getRootNode();
            itemWeights.put(node, widget.getWeight());

            sp.getItems().add(node);
            sp.getItems().sort(Comparator.comparingInt(item -> itemWeights.get(item)));
        });
    }

    @Override
    public void dispose() {
        if (fManager != null) {
            fManager.dispose();
        }
        if (fSplitPane != null) {
            fSplitPane.getItems().clear();
        }
    }

    @Override
    public void setFocus() {
    }

}
