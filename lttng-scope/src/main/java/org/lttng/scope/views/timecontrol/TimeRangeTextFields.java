/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.views.timecontrol;

import com.efficios.jabberwocky.common.TimeRange;
import com.efficios.jabberwocky.context.ViewGroupContext;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.TextField;
import org.jetbrains.annotations.Nullable;

import java.util.stream.Stream;

import static org.lttng.scope.views.timecontrol.TimestampConversion.tsToString;

/**
 * Group of 3 {@link TextField} linked together to represent a
 * {@link TimeRange}.
 *
 * @author Alexandre Montplaisir
 */
public class TimeRangeTextFields {

    private final Long minimumDuration;

    private TimeRange limits;
    public void setLimits(TimeRange newLimits) {
        if (minimumDuration != null
                && newLimits != ViewGroupContext.UNINITIALIZED_RANGE
                && minimumDuration > newLimits.getDuration()) {
            throw new IllegalArgumentException("Minimum duration is " + minimumDuration + ", requested is " + newLimits.getDuration());
        }
        limits = newLimits;
    }

    private final TimeRangeTextField startTextField = new StartTextField();
    public TextField getStartTextField() { return startTextField; }

    private final TimeRangeTextField endTextField = new EndTextField();
    public TextField getEndTextField() { return endTextField; }

    private final TimeRangeTextField durationTextField = new DurationTextField();
    public TextField getDurationTextField() { return durationTextField; }

    private final ObjectProperty<TimeRange> timeRangeProperty = new SimpleObjectProperty<>();
    public ObjectProperty<TimeRange> timeRangeProperty() { return timeRangeProperty; }
    public TimeRange getTimeRange() { return timeRangeProperty.get(); }
    public void setTimeRange(TimeRange value) { timeRangeProperty.set(value); }

    public TimeRangeTextFields(TimeRange initialLimits, @Nullable Long minimumDuration) {
        if (minimumDuration != null
                && initialLimits != ViewGroupContext.UNINITIALIZED_RANGE
                && minimumDuration > initialLimits.getDuration()) {
            throw new IllegalArgumentException();
        }

        this.limits = initialLimits;
        this.minimumDuration = minimumDuration;

        timeRangeProperty.addListener((obs) -> {
            resetAllValues();
        });
    }

    private void resetAllValues() {
        Stream.of(startTextField, endTextField, durationTextField).forEach(TimeRangeTextField::resetValue);
    }

    private abstract class TimeRangeTextField extends TextField {

        public TimeRangeTextField() {

            focusedProperty().addListener((obs, wasFocused, isFocused) -> {
                if (!isFocused) {
                    resetValue();
                }
            });
            setOnKeyPressed(event -> {
                switch (event.getCode()) {
                case ENTER:
                    applyCurrentText();
                    break;
                case ESCAPE:
                    resetValue();
                    break;
                // $CASES-OMITTED$
                default:
                    /* Ignore */
                    break;
                }
            });
        }

        /**
         * Re-synchronize the displayed value, by making it equal to the tracked time
         * range.
         */
        public abstract void resetValue();

        public final void applyCurrentText() {
            String text = getText();
            /* First see if the current text makes sense. */
            Long value = TimestampConversion.stringToTs(text);
            if (value == null) {
                /* Invalid value, reset to previous one */
                resetValue();
                return;
            }
            applyValue(value);
        }

        protected abstract void applyValue(long value);
    }

    private class StartTextField extends TimeRangeTextField {

        @Override
        public void resetValue() {
            TimeRange tr = getTimeRange();
            long start = (tr == null ? 0 : tr.getStartTime());
            setText(tsToString(start));
        }

        @Override
        public void applyValue(long value) {
            long prevEnd = timeRangeProperty.get().getEndTime();

            /* The value is valid, apply it as the new range start. */
            long newStart = Math.max(limits.getStartTime(), value);
            newStart = Math.min(limits.getEndTime(), newStart);
            /* Update the end time if we need to (it needs to stay >= start) */
            long newEnd = Math.max(newStart, prevEnd);

            TimeRange newTimeRange = TimeRange.of(newStart, newEnd);

            if (minimumDuration != null && newTimeRange.getDuration() < minimumDuration) {
                if (limits.getEndTime() - newStart > minimumDuration) {
                    /* We have room, only offset the end time */
                    newEnd = newStart + minimumDuration;
                } else {
                    /* We don't have room, clamp to end and also offset the new start. */
                    newEnd = limits.getEndTime();
                    newStart = newEnd - minimumDuration;
                }
                newTimeRange = TimeRange.of(newStart, newEnd);
            }

            timeRangeProperty.set(newTimeRange);
            resetAllValues();
        }

    }

    private class EndTextField extends TimeRangeTextField {

        @Override
        public void resetValue() {
            TimeRange tr = getTimeRange();
            long end = (tr == null ? 0 : tr.getEndTime());
            setText(tsToString(end));
        }

        @Override
        public void applyValue(long value) {
            long prevStart = timeRangeProperty.get().getStartTime();

            long newEnd = Math.min(limits.getEndTime(), value);
            newEnd = Math.max(limits.getStartTime(), newEnd);
            long newStart = Math.min(newEnd, prevStart);

            TimeRange newTimeRange = TimeRange.of(newStart, newEnd);

            if (minimumDuration != null && newTimeRange.getDuration() < minimumDuration) {
                if (newEnd - limits.getStartTime() > minimumDuration) {
                    /* We have room, only offset the start time */
                    newStart = newEnd - minimumDuration;
                } else {
                    /* We don't have room, clamp to end and also offset the new start. */
                    newStart = limits.getStartTime();
                    newEnd = newStart + minimumDuration;
                }
                newTimeRange = TimeRange.of(newStart, newEnd);
            }

            timeRangeProperty.set(newTimeRange);
            resetAllValues();
        }

    }

    private class DurationTextField extends TimeRangeTextField {

        @Override
        public void resetValue() {
            TimeRange tr = getTimeRange();
            long duration = (tr == null ? 0 : tr.getDuration());
            setText(tsToString(duration));
        }

        @Override
        public void applyValue(long value) {
            TimeRange prevTimeRange = timeRangeProperty.get();
            long requestedDuration = (minimumDuration == null ? value : Math.max(minimumDuration, value));

            TimeRange newRange;
            /*
             * If the entered time span is greater than the limits, we will simply change it
             * to the limits themselves.
             */
            if (requestedDuration >= limits.getDuration()) {
                newRange = limits;
            } else if ((limits.getEndTime() - prevTimeRange.getStartTime()) > requestedDuration) {
                /*
                 * We will apply the requested time span no matter what. We will prioritize
                 * modifying the end time first.
                 */
                /* There is room, we only need to change the end time. */
                long newStart = prevTimeRange.getStartTime();
                long newEnd = newStart + requestedDuration;
                newRange = TimeRange.of(newStart, newEnd);
            } else {
                /*
                 * There is not enough "room", we will clamp the end to the limit and also
                 * modify the start time.
                 */
                long newEnd = limits.getEndTime();
                long newStart = newEnd - requestedDuration;
                newRange = TimeRange.of(newStart, newEnd);
            }

            timeRangeProperty.set(newRange);
            resetAllValues();
        }

    }

}
