/*
 * Copyright (C) 2017-2018 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.views.timecontrol

import com.efficios.jabberwocky.common.TimeRange
import com.efficios.jabberwocky.context.ViewGroupContext
import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.control.TextField
import javafx.scene.input.KeyCode
import org.lttng.scope.application.ScopeOptions
import org.lttng.scope.common.TimestampFormat
import org.lttng.scope.views.context.ViewGroupContextManager
import kotlin.math.max
import kotlin.math.min

/**
 * Group of 3 {@link TextField} linked together to represent a
 * {@link TimeRange}.
 */
class TimeRangeTextFields(initialLimits: TimeRange, private val minimumDuration: Long?) {

    var limits: TimeRange = initialLimits

    val startTextField: TextField = StartTextField()
    val endTextField: TextField = EndTextField()
    val durationTextField: TextField = DurationTextField()

    private val timeRangeProperty: ObjectProperty<TimeRange?> = SimpleObjectProperty()
    fun timeRangeProperty() = timeRangeProperty
    var timeRange: TimeRange?
        get() = timeRangeProperty.get()
        set(timeRange) = timeRangeProperty.set(timeRange)

    init {
        if (minimumDuration != null
                && initialLimits != ViewGroupContext.UNINITIALIZED_RANGE
                && minimumDuration > initialLimits.duration) {
            throw IllegalArgumentException()
        }

        timeRangeProperty.addListener { _ -> resetAllValues() }
        ScopeOptions.timestampFormatProperty().addListener { _ -> resetAllValues() }
        ScopeOptions.timestampTimeZoneProperty().addListener { _ -> resetAllValues() }
    }

    private fun resetAllValues() {
        listOf(startTextField, endTextField, durationTextField)
                .map { it as TimeRangeTextField }
                .forEach { it.resetValue() }
    }

    private abstract inner class TimeRangeTextField : TextField() {

        init {
            focusedProperty().addListener { _, _, isFocused ->
                if (!isFocused) resetValue()
            }

            setOnKeyPressed { event ->
                when (event.code) {
                    KeyCode.ENTER -> applyCurrentText()
                    KeyCode.ESCAPE -> resetValue()
                    else -> {
                    }
                }
            }
        }

        /**
         * Re-synchronize the displayed value, by making it equal to the tracked time
         * range.
         */
        abstract fun resetValue()

        fun applyCurrentText() {
            /* First see if the current text makes sense. */
            val range = ViewGroupContextManager.getCurrent().getCurrentProjectFullRange()
            val value = getTimestampFormat().stringToTs(range, text)
            if (value == null) {
                /* Invalid value, reset to previous one */
                resetValue()
                return
            }
            applyValue(value)
        }

        protected abstract fun applyValue(value: Long)

        protected open fun getTimestampFormat(): TimestampFormat = ScopeOptions.timestampFormatProperty().get()
    }

    private inner class StartTextField : TimeRangeTextField() {

        override fun resetValue() {
            val start = timeRange?.startTime ?: 0L
            text = getTimestampFormat().tsToString(start)
        }

        override fun applyValue(value: Long) {
            val prevEnd = timeRange?.endTime ?: 0L

            /* The value is valid, apply it as the new range start. */
            var newStart = value.coerceIn(limits.startTime, limits.endTime)
            /* Update the end time if we need to (it needs to stay >= start) */
            var newEnd = max(newStart, prevEnd)

            if (minimumDuration != null && (newEnd - newStart) < minimumDuration) {
                if (limits.endTime - newStart > minimumDuration) {
                    /* We have room, only offset the end time */
                    newEnd = newStart + minimumDuration
                } else {
                    /* We don't have room, clamp to end and also offset the new start. */
                    newEnd = limits.endTime
                    newStart = newEnd - minimumDuration
                }
            }

            timeRangeProperty.set(TimeRange.of(newStart, newEnd))
            resetAllValues()
        }

    }

    private inner class EndTextField : TimeRangeTextField() {

        override fun resetValue() {
            val end = timeRange?.endTime ?: 0L
            text = getTimestampFormat().tsToString(end)
        }

        override fun applyValue(value: Long) {
            val prevStart = timeRange?.startTime ?: 0L

            var newEnd = value.coerceIn(limits.startTime, limits.endTime)
            var newStart = min(newEnd, prevStart)

            if (minimumDuration != null && (newEnd - newStart) < minimumDuration) {
                if (newEnd - limits.startTime > minimumDuration) {
                    /* We have room, only offset the start time */
                    newStart = newEnd - minimumDuration
                } else {
                    /* We don't have room, clamp to end and also offset the new start. */
                    newStart = limits.startTime
                    newEnd = newStart + minimumDuration
                }
            }

            timeRangeProperty.set(TimeRange.of(newStart, newEnd))
            resetAllValues()
        }

    }

    private inner class DurationTextField : TimeRangeTextField() {

        override fun resetValue() {
            val duration = timeRange?.duration ?: 0L
            text = getTimestampFormat().tsToString(duration)
        }

        override fun applyValue(value: Long) {
            val prevTimeRange = timeRange ?: TimeRange.of(0L, 0L)
            val requestedDuration = minimumDuration?.let { max(minimumDuration, value) } ?: value

            /*
             * If the entered time span is greater than the limits, we will simply change it
             * to the limits themselves.
             */
            val newRange: TimeRange = if (requestedDuration >= limits.duration) {
                limits
            } else if ((limits.endTime - prevTimeRange.startTime) > requestedDuration) {
                /*
                 * We will apply the requested time span no matter what. We will prioritize
                 * modifying the end time first.
                 */
                /* There is room, we only need to change the end time. */
                val newStart = prevTimeRange.startTime
                val newEnd = newStart + requestedDuration
                TimeRange.of(newStart, newEnd)
            } else {
                /*
                 * There is not enough "room", we will clamp the end to the limit and also
                 * modify the start time.
                 */
                val newEnd = limits.endTime
                val newStart = newEnd - requestedDuration
                TimeRange.of(newStart, newEnd)
            }

            timeRangeProperty.set(newRange)
            resetAllValues()
        }

        /* Duration always uses s.ns */
        override fun getTimestampFormat() = TimestampFormat.SECONDS_POINT_NANOS
    }

}
