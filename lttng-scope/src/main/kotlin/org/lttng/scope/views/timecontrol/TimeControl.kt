/*
 * Copyright (C) 2017-2018 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.views.timecontrol

import com.efficios.jabberwocky.context.ViewGroupContext
import com.efficios.jabberwocky.project.TraceProject
import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.geometry.HPos
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.geometry.VPos
import javafx.scene.control.Label
import javafx.scene.control.TextField
import javafx.scene.layout.*
import javafx.scene.paint.Color
import javafx.scene.text.Font
import javafx.scene.text.FontWeight
import org.lttng.scope.application.ScopeOptions
import org.lttng.scope.views.context.ViewGroupContextManager

class TimeControl : BorderPane() {

    companion object {
        /* UI Text */
        private const val LABEL_VISIBLE_TIME_RANGE = "Visible Time Range"
        private const val LABEL_SELECTION_TIME_RANGE = "Selection Time Range"
        private const val LABEL_PROJECT_RANGE = "Trace Project Time Range"
        private const val LABEL_START = "Start"
        private const val LABEL_END = "End"
        private const val LABEL_SPAN = "Span (s)"

        private const val MINIMUM_VISIBLE_RANGE = 10000L
        private val TITLE_FONT = Font.font(null, FontWeight.BOLD, -1.0)
        private val GRID_PADDING = Insets(10.0)
    }

    private val viewContextProperty: ObjectProperty<ViewGroupContext> = SimpleObjectProperty(ViewGroupContextManager.getCurrent())

    private val textFields: Array<TextField>
    private val visibleRangeFields: TimeRangeTextFields
    private val selectionRangeFields: TimeRangeTextFields
    private val projRangeTextFields: Array<TextField>


    init {
        val viewCtx = viewContextProperty.get()

        visibleRangeFields = TimeRangeTextFields(viewCtx.getCurrentProjectFullRange(), MINIMUM_VISIBLE_RANGE)
        selectionRangeFields = TimeRangeTextFields(viewCtx.getCurrentProjectFullRange(), null)

        textFields = arrayOf(
                visibleRangeFields.startTextField,
                visibleRangeFields.endTextField,
                visibleRangeFields.durationTextField,
                selectionRangeFields.startTextField,
                selectionRangeFields.endTextField,
                selectionRangeFields.durationTextField
        )
        textFields.forEach { it.alignment = Pos.CENTER_LEFT }

        projRangeTextFields = generateSequence { TextField() }.take(3)
                .onEach {
                    it.isEditable = false
                    it.prefWidth = 220.0
                    it.alignment = Pos.CENTER_LEFT
                    it.background = Background(BackgroundFill(Color.LIGHTGRAY, CornerRadii(2.0), null))
                }.toList().toTypedArray()

        val topHeaderLabels = listOf(LABEL_START, LABEL_END, LABEL_SPAN)
                .map { Label(it) }
                .onEach {
                    it.font = TITLE_FONT
                    GridPane.setHalignment(it, HPos.CENTER)
                }.toTypedArray()

        val leftHeaderLabels = listOf(LABEL_VISIBLE_TIME_RANGE, LABEL_SELECTION_TIME_RANGE, LABEL_PROJECT_RANGE)
                .map { Label(it) }
                .onEach {
                    it.font = TITLE_FONT
                    GridPane.setHalignment(it, HPos.RIGHT)
                    GridPane.setValignment(it, VPos.CENTER)
                }.toTypedArray()


        /* Setup the elements in the grid */
        val grid = GridPane().apply {
            padding = GRID_PADDING
            hgap = 2.0
            vgap = 2.0

            add(topHeaderLabels[0], 1, 0)
            add(topHeaderLabels[1], 2, 0)
            add(topHeaderLabels[2], 3, 0)

            add(leftHeaderLabels[0], 0, 1)
            add(textFields[0], 1, 1)
            add(textFields[1], 2, 1)
            add(textFields[2], 3, 1)

            add(leftHeaderLabels[1], 0, 2)
            add(textFields[3], 1, 2)
            add(textFields[4], 2, 2)
            add(textFields[5], 3, 2)

            add(leftHeaderLabels[2], 0, 3)
            add(projRangeTextFields[0], 1, 3)
            add(projRangeTextFields[1], 2, 3)
            add(projRangeTextFields[2], 3, 3)
        }

        center = grid
    }

    private val projectChangeListener = object : ViewGroupContext.ProjectChangeListener {
        override fun newProjectCb(newProject: TraceProject<*, *>?) {
            val viewCtx = viewContextProperty.get()
            val tf = ScopeOptions.timestampFormat

            /* Update the displayed trace's time range. */
            val projRange = newProject?.fullRange ?: ViewGroupContext.UNINITIALIZED_RANGE

            projRangeTextFields[0].text = tf.tsToString(projRange.startTime)
            projRangeTextFields[1].text = tf.tsToString(projRange.endTime)
            projRangeTextFields[2].text = tf.tsToString(projRange.duration)

            /* Update the text fields' limits */
            visibleRangeFields.limits = projRange
            selectionRangeFields.limits = projRange

            /* Read initial dynamic range values from the view context. */
            val visibleRange = viewCtx.visibleTimeRange
            val selectionRange = viewCtx.selectionTimeRange
            with(textFields) {
                get(0).text = tf.tsToString(visibleRange.startTime)
                get(1).text = tf.tsToString(visibleRange.endTime)
                get(2).text = tf.tsToString(visibleRange.duration)
                get(3).text = tf.tsToString(selectionRange.startTime)
                get(4).text = tf.tsToString(selectionRange.endTime)
                get(5).text = tf.tsToString(selectionRange.duration)
            }

            /**
             * Add listeners to update the values when the context's time ranges change.
             * Note we dot no use binds here, we do not want every single keystroke in the
             * text fields to update the view context values!
             */
            viewCtx.visibleTimeRangeProperty().addListener { _, _, newVal ->
                if (viewCtx.listenerFreeze) return@addListener
                textFields[0].text = tf.tsToString(newVal.startTime)
                textFields[1].text = tf.tsToString(newVal.endTime)
                textFields[2].text = tf.tsToString(newVal.duration)
            }
            viewCtx.selectionTimeRangeProperty().addListener { _, _, newVal ->
                if (viewCtx.listenerFreeze) return@addListener
                textFields[3].text = tf.tsToString(newVal.startTime)
                textFields[4].text = tf.tsToString(newVal.endTime)
                textFields[5].text = tf.tsToString(newVal.duration)
            }

            /* Bind underlying properties */
            visibleRangeFields.timeRangeProperty().bindBidirectional(viewCtx.visibleTimeRangeProperty())
            selectionRangeFields.timeRangeProperty().bindBidirectional(viewCtx.selectionTimeRangeProperty())
        }

    }

    init {
        viewContextProperty.get().registerProjectChangeListener(projectChangeListener)
    }

    @Suppress("ProtectedInFinal", "Unused")
    protected fun finalize() {
        // TODO Make sure we de register from the correct view context once there
        // are more than one.
        viewContextProperty.get().deregisterProjectChangeListener(projectChangeListener)
    }

}
