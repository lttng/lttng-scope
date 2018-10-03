/*
 * Copyright (C) 2016, 2018 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.efficios.jabberwocky.views.timegraph.control

import com.efficios.jabberwocky.common.TimeRange
import com.efficios.jabberwocky.context.ViewGroupContext
import com.efficios.jabberwocky.project.TraceProject
import com.efficios.jabberwocky.views.timegraph.model.provider.ITimeGraphModelProvider
import com.efficios.jabberwocky.views.timegraph.view.TimeGraphModelView
import javafx.beans.value.ChangeListener

/**
 * Control part of the timegraph MVC mechanism. It links a
 * {@link TimeGraphModelView} to a {@link ITimeGraphModelProvider}.
 *
 * The control links a model provider, and a view. But the view also needs a
 * back-reference to the control. The suggested pattern is to do:
 *
 * <pre>
 * ITimeGraphModelProvider provider = ...
 * TimeGraphModelControl control = new TimeGraphModelControl(viewContext, provider);
 * TimeGraphModelView view = new TimeGraphModelView(control);
 * control.attachView(view);
 * </pre>
 *
 * @param viewContext
 *            The view context to which this timegraph belongs
 * @param renderProvider
 *            The model provider that goes with this control
 */
class TimeGraphModelControl(val viewContext: ViewGroupContext, val renderProvider: ITimeGraphModelProvider) {

    private val projectChangeListener = object : ViewGroupContext.ProjectChangeListener(this) {
        override fun flush() {
            // TODO ?
        }

        override fun newProjectCb(newProject: TraceProject<*, *>?) {
            initializeForProject(newProject);
        }
    }

    private val selectionTimeRangeChangeListener = ChangeListener<TimeRange> { _, _, newRange ->
        if (viewContext.listenerFreeze) return@ChangeListener
        drawSelection(newRange)
    }

    private val visibleRangeChangeListener = ChangeListener<TimeRange> { _, _, newRange ->
        if (viewContext.listenerFreeze) return@ChangeListener
        seekVisibleRange(newRange)
    }

    var view: TimeGraphModelView? = null
        set(value) {
            field = value
            /*
             * Initially populate the view with the context of the current trace project.
             */
            initializeForProject(viewContext.traceProject)
        }

    init {
        /* Attach listeners */
        viewContext.registerProjectChangeListener(projectChangeListener)
        viewContext.selectionTimeRangeProperty().addListener(selectionTimeRangeChangeListener)
        viewContext.visibleTimeRangeProperty().addListener(visibleRangeChangeListener)
    }

    fun dispose() {
        viewContext.deregisterProjectChangeListener(projectChangeListener);
        view?.dispose()
    }

    // ------------------------------------------------------------------------
    // Control -> View operations
    // ------------------------------------------------------------------------

    /**
     * Initialize this timegraph for a new trace.
     *
     * @param traceProject
     *            The project to initialize in the view. If it is null it indcates
     *            'no trace project'.
     */
    @Synchronized
    fun initializeForProject(traceProject: TraceProject<*, *>?) {
        val view = view ?: return
        view.clear()

        if (traceProject == null) {
            renderProvider.traceProject = null
            /* View will remain cleared */
            return
        }
        renderProvider.traceProject = traceProject

        val currentVisibleRange = viewContext.visibleTimeRange
        checkWindowTimeRange(currentVisibleRange)
        view.seekVisibleRange(currentVisibleRange)
    }

    /**
     * Repaint, without seeking anywhere else, the current displayed area of the
     * view.
     *
     * This can be called whenever some settings like filters etc. have changed,
     * so that a repaint will show updated information.
     */
    fun repaintCurrentArea() {
        val trace = viewContext.traceProject
        val currentRange = viewContext.visibleTimeRange
        if (trace == null || currentRange == ViewGroupContext.UNINITIALIZED_RANGE) {
            return
        }

        view?.let {
            it.clear()
            it.seekVisibleRange(currentRange)
        }
    }

    fun seekVisibleRange(newRange: TimeRange) {
        checkWindowTimeRange(newRange)
        view?.seekVisibleRange(newRange)
    }

    fun drawSelection(selectionRange: TimeRange) {
        checkWindowTimeRange(selectionRange)
        view?.drawSelection(selectionRange)
    }

    // ------------------------------------------------------------------------
    // View -> Control operations (Control external API)
    // ------------------------------------------------------------------------

    /**
     * Change the current time range selection.
     *
     * Called by the view to indicate that the user has input a new time range
     * selection from the view. The control will relay this to the rest of the
     * framework.
     *
     * @param newSelectionRange
     *            The new time range selection.
     */
    fun updateTimeRangeSelection(newSelectionRange: TimeRange) {
        viewContext.selectionTimeRange = newSelectionRange
    }

    /**
     * Change the current visible time range.
     *
     * Called by the view whenever the user selects a new visible time range,
     * for example by scrolling left or right.
     *
     * @param newVisibleRange
     *            The new visible time range
     * @param echo
     *            This flag indicates if the view wants to receive the new time
     *            range notification back to itself (via
     *            {@link #seekVisibleRange}.
     */
    fun updateVisibleTimeRange(newVisibleRange: TimeRange, echo: Boolean) {
        checkTimeRange(newVisibleRange)

        /*
         * If 'echo' is 'off', we will avoid triggering our change listener on
         * this modification by detaching then re-attaching it afterwards.
         */
        if (echo) {
            viewContext.visibleTimeRange = newVisibleRange
        } else {
            viewContext.visibleTimeRangeProperty().removeListener(visibleRangeChangeListener)
            viewContext.visibleTimeRange = newVisibleRange
            viewContext.visibleTimeRangeProperty().addListener(visibleRangeChangeListener)
        }
    }

    // ------------------------------------------------------------------------
    // Utils
    // ------------------------------------------------------------------------

    private fun checkTimeRange(range: TimeRange) {
        if (range.startTime == Long.MAX_VALUE) {
            throw IllegalArgumentException("You are trying to make me believe the range starts at " + range.startTime + ". I do not believe you.")
        }
        if (range.endTime == Long.MAX_VALUE) {
            throw IllegalArgumentException("You are trying to make me believe the range ends at " + range.endTime + ". I do not believe you.")
        }
    }

    private fun checkWindowTimeRange(windowRange: TimeRange) {
        checkTimeRange(windowRange)

        val fullRange = viewContext.getCurrentProjectFullRange()

        if (windowRange.startTime < fullRange.startTime) {
            throw IllegalArgumentException("Requested window start time: " + windowRange.startTime +
                    " is smaller than trace start time " + fullRange.startTime)
        }
        if (windowRange.endTime > fullRange.endTime) {
            throw IllegalArgumentException("Requested window end time: " + windowRange.endTime +
                    " is greater than trace end time " + fullRange.endTime)
        }
    }

}
