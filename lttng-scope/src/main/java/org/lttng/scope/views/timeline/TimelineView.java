/*******************************************************************************
 * Copyright (c) 2016 EfficiOS Inc., Alexandre Montplaisir
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.lttng.scope.views.timeline;

import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.SplitPane;
import org.lttng.scope.views.context.ViewGroupContextManager;
import org.lttng.scope.views.jfx.JfxUtils;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.requireNonNull;

public class TimelineView {

    public static final String VIEW_ID = "org.lttng.scope.views.timeline"; //$NON-NLS-1$

    private final Map<Node, Integer> itemWeights = new HashMap<>();

    private TimelineManager fManager;
    private SplitPane fSplitPane;

    public TimelineView() {
        super();
    }

    public void createPartControl() {
        SplitPane sp = new SplitPane();
        sp.setOrientation(Orientation.VERTICAL);
        fSplitPane = sp;

        TimelineManager manager = new TimelineManager(this, ViewGroupContextManager.getCurrent());
        fManager = manager;

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

    public void dispose() {
        if (fManager != null) {
            fManager.dispose();
        }
        if (fSplitPane != null) {
            fSplitPane.getItems().clear();
        }
    }

}
