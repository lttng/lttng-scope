/*
 * Copyright (C) 2017-2018 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.efficios.jabberwocky.views.xychart.control

import com.efficios.jabberwocky.common.TimeRange
import com.efficios.jabberwocky.context.ViewGroupContext
import com.efficios.jabberwocky.context.ViewGroupContext.ProjectChangeListener
import com.efficios.jabberwocky.project.TraceProject
import com.efficios.jabberwocky.views.xychart.model.provider.XYChartModelProvider
import com.efficios.jabberwocky.views.xychart.view.XYChartView
import javafx.beans.value.ChangeListener

class XYChartControl(val viewContext: ViewGroupContext, val renderProvider: XYChartModelProvider) {

    private val projectChangeListener = object : ProjectChangeListener(this) {
        // TODO Override 'flush()' method to handle views being able to flush out-of-band threads.

        override fun newProjectCb(newProject: TraceProject<*, *>?) {
            initializeForProject(newProject)
        }
    }

    private val selectionTimeRangeChangeListener = ChangeListener<TimeRange> {_, _, newRange ->
        if (viewContext.listenerFreeze) return@ChangeListener
        drawSelection(newRange)
    }

    private val visibleRangeChangeListener = ChangeListener<TimeRange> { _, _, newRange ->
        if (viewContext.listenerFreeze) return@ChangeListener
        seekVisibleRange(newRange)
    }

    var view: XYChartView? = null
        set(value) {
            field = value
            /*
             * Initially populate the view with the context of the current trace project.
             */
            initializeForProject(viewContext.traceProject)
        }


    init {
        viewContext.registerProjectChangeListener(projectChangeListener)
        viewContext.selectionTimeRangeProperty().addListener(selectionTimeRangeChangeListener)
        viewContext.visibleTimeRangeProperty().addListener(visibleRangeChangeListener)
    }

    fun dispose() {
        viewContext.deregisterProjectChangeListener(projectChangeListener)
        view?.dispose()
    }

    // ------------------------------------------------------------------------
    // Control -> View operations
    // ------------------------------------------------------------------------

    @Synchronized
    fun initializeForProject(traceProject: TraceProject<*, *>?) {
        val view = view ?: return
        view.clear()

        if (traceProject == null) {
            renderProvider.traceProject = null

            view.seekVisibleRange(ViewGroupContext.UNINITIALIZED_RANGE)
            view.drawSelection(ViewGroupContext.UNINITIALIZED_RANGE)

            /* View will remain cleared */
            return
        }
        renderProvider.traceProject = traceProject

        viewContext.visibleTimeRange.let {
            checkWindowTimeRange(it)
            view.seekVisibleRange(it)
        }
        view.drawSelection(viewContext.selectionTimeRange)
    }

    fun repaintCurrentArea() {
        val project = viewContext.traceProject
        val currentRange = viewContext.visibleTimeRange
        if (project == null || currentRange == ViewGroupContext.UNINITIALIZED_RANGE) {
            return
        }

        val view = view
        if (view != null) {
            view.clear()
            view.seekVisibleRange(currentRange)
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

    fun updateTimeRangeSelection(newSelectionRange: TimeRange) {
        viewContext.selectionTimeRange = newSelectionRange
    }

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

    fun checkTimeRange(range: TimeRange) {
        if (range.startTime == Long.MAX_VALUE) {
            throw IllegalArgumentException("You are trying to make me believe the range starts at " +
                    range.startTime + ". I do not believe you.")
        }
        if (range.endTime == Long.MAX_VALUE) {
            throw IllegalArgumentException("You are trying to make me believe the range ends at " +
                    range.endTime + ". I do not believe you.")
        }
    }

    fun checkWindowTimeRange(windowRange: TimeRange) {
        checkTimeRange(windowRange);

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
