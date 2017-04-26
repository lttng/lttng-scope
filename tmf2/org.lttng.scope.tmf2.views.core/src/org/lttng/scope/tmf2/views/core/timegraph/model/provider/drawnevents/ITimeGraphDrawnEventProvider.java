/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.tmf2.views.core.timegraph.model.provider.drawnevents;

import java.util.concurrent.FutureTask;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.lttng.scope.tmf2.views.core.TimeRange;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.drawnevents.TimeGraphDrawnEventRender;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.drawnevents.TimeGraphDrawnEventSeries;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.tree.TimeGraphTreeRender;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;

/**
 * Interface defining providers of drawn events.
 *
 * A "drawn event" is a symbol in a timegraph representing one particular
 * (trace) event. Contrary to a trace event, a drawn event has both horizontal
 * and vertical coordinates, being time and its corresponding time graph entry.
 *
 * @author Alexandre Montplaisir
 * @see TimeGraphDrawnEventProviderManager
 */
public interface ITimeGraphDrawnEventProvider {

    /**
     * The trace from which the provider will fetch its events. The Property
     * mechanisms can be used to sync with trace changes elsewhere in the
     * framework.
     *
     * @return The target trace
     */
    ObjectProperty<@Nullable ITmfTrace> traceProperty();

    /**
     * The 'enabled' property of this provider. A provider can be created and
     * registered to the manager, but considered disabled so that it stops
     * painting its events.
     *
     * For example, if the user can uncheck elements in a list showing all
     * existing event providers, this could disable said providers without
     * completely destroying them.
     *
     * @return The enabled property
     */
    BooleanProperty enabledProperty();

    /**
     * Return the {@link TimeGraphDrawnEventSeries} provided by this provider. A
     * single provider should manage a single event series; different providers
     * should be implemented for different series.
     *
     * @return The event series of this provider
     */
    TimeGraphDrawnEventSeries getEventSeries();

    /**
     * Query for a render of drawn events on this provider.
     *
     * @param treeRender
     *            The tree render of the timegraph to paint on
     * @param timeRange
     *            The time range of the timegraph (and trace) to run the query
     *            on.
     * @param task
     *            If this method is called from within a {@link FutureTask}, it
     *            can optionally be passed here. If the execution is expected to
     *            take a long time, the implementation is suggested to
     *            periodically check this parameter for cancellation.
     * @return The corresponding event render that contains the result of the
     *         query
     */
    TimeGraphDrawnEventRender getEventRender(TimeGraphTreeRender treeRender,
            TimeRange timeRange, @Nullable FutureTask<?> task);
}
