/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.efficios.jabberwocky.views.timegraph.model.provider.drawnevents;

import com.efficios.jabberwocky.common.TimeRange;
import com.efficios.jabberwocky.project.TraceProject;
import com.efficios.jabberwocky.views.timegraph.model.render.drawnevents.TimeGraphDrawnEventRender;
import com.efficios.jabberwocky.views.timegraph.model.render.drawnevents.TimeGraphDrawnEventSeries;
import com.efficios.jabberwocky.views.timegraph.model.render.tree.TimeGraphTreeRender;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty

import java.util.concurrent.FutureTask;

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
abstract class TimeGraphDrawnEventProvider(val drawnEventSeries: TimeGraphDrawnEventSeries) {

    /**
     * The trace from which the provider will fetch its events. The Property
     * mechanisms can be used to sync with trace changes elsewhere in the
     * framework.
     */
    private val traceProjectProperty: ObjectProperty<TraceProject<*, *>?> = SimpleObjectProperty(null)
    fun traceProjectProperty() = traceProjectProperty
    var traceProject
        get() = traceProjectProperty.get()
        set(value) = traceProjectProperty.set(value)

    /**
     * The 'enabled' property of this provider. A provider can be created and
     * registered to the manager, but considered disabled so that it stops
     * painting its events.
     *
     * For example, if the user can uncheck elements in a list showing all
     * existing event providers, this could disable said providers without
     * completely destroying them.
     */
    private val enabledProperty: BooleanProperty = SimpleBooleanProperty(false)
    fun enabledProperty() = enabledProperty
    var enabled
        get() = enabledProperty.get()
        set(value) = enabledProperty.set(value)

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
    abstract fun getEventRender(treeRender: TimeGraphTreeRender,
                                timeRange: TimeRange,
                                task: FutureTask<*>?): TimeGraphDrawnEventRender

}
