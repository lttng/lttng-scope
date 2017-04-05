/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.tmf2.views.ui.timeline;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

import org.lttng.scope.tmf2.views.core.timegraph.control.TimeGraphModelControl;
import org.lttng.scope.tmf2.views.core.timegraph.model.provider.ITimeGraphModelProviderFactory;
import org.lttng.scope.tmf2.views.core.timegraph.model.provider.ITimeGraphModelRenderProvider;
import org.lttng.scope.tmf2.views.core.timegraph.model.provider.TimeGraphModelProviderManager;
import org.lttng.scope.tmf2.views.core.timegraph.view.TimeGraphModelView;
import org.lttng.scope.tmf2.views.ui.timegraph.swtjfx.SwtJfxTimeGraphViewer;

import com.google.common.collect.ImmutableSet;

public class TimelineManager {

    private final Set<ITimelineWidget> fWidgets = new LinkedHashSet<>();

    public TimelineManager() {

        /* Add widgets for all known timegraph model providers */
        for (ITimeGraphModelProviderFactory factory : TimeGraphModelProviderManager.instance().getRegisteredProviderFactories()) {
            /* Instantiate a widget for this provider type */
            ITimeGraphModelRenderProvider provider = factory.get();
            TimeGraphModelControl control = new TimeGraphModelControl(provider);
            SwtJfxTimeGraphViewer viewer = new SwtJfxTimeGraphViewer(control);
            control.attachView(viewer);

            fWidgets.add(viewer);
        }
    }

    public void dispose() {
        fWidgets.forEach(w -> {
            if (w instanceof TimeGraphModelView) {
                /*
                 * TimeGraphModelView's are disposed via their control
                 *
                 * FIXME Do this better.
                 */
                ((TimeGraphModelView) w).getControl().dispose();
            } else {
                w.dispose();
            }
        });
    }

    public Iterable<ITimelineWidget> getWidgets() {
        return ImmutableSet.copyOf(fWidgets);
    }

    void resetInitialSeparatorPosition() {
        fWidgets.stream()
                .map(w -> w.getSplitPane())
                .filter(Objects::nonNull)
                .map(p -> Objects.requireNonNull(p))
                .forEach(pane -> pane.setDividerPositions(0.2));
    }

}
