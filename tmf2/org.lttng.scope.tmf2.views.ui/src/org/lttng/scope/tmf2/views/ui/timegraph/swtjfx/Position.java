/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.tmf2.views.ui.timegraph.swtjfx;

import java.util.Objects;

import org.eclipse.jdt.annotation.Nullable;
import org.lttng.scope.tmf2.views.core.TimeRange;
import org.lttng.scope.tmf2.views.core.timegraph.control.TimeGraphModelControl;

import com.google.common.base.MoreObjects;

interface Position {

    /**
     * Placeholder for uninitialized horizontal positions.
     */
    HorizontalPosition UNINITIALIZED_HP = new HorizontalPosition(
            TimeGraphModelControl.UNINITIALIZED);

    /**
     * Placeholder for uninitialized vertical positions.
     */
    VerticalPosition UNINITIALIZED_VP = new VerticalPosition(0.0, 0.0);

    class HorizontalPosition {

        public final TimeRange fTimeRange;

        public HorizontalPosition(TimeRange range) {
            fTimeRange = range;
        }

        @Override
        public int hashCode() {
            return Objects.hash(fTimeRange);
        }

        @Override
        public boolean equals(@Nullable Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            HorizontalPosition other = (HorizontalPosition) obj;
            return (fTimeRange.equals(other.fTimeRange));
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("Time range", fTimeRange) //$NON-NLS-1$
                    .toString();
        }
    }

    class VerticalPosition {

        private static final double EPSILON = 0.00001;

        public final double fTopPos;
        public final double fBottomPos;

        public VerticalPosition(double topPos, double bottomPos) {
            fTopPos = topPos;
            fBottomPos = bottomPos;
        }

        @Override
        public int hashCode() {
            return Objects.hash(fTopPos, fBottomPos);
        }

        @Override
        public boolean equals(@Nullable Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            VerticalPosition other = (VerticalPosition) obj;
            return (doubleEquals(fTopPos, other.fTopPos)
                    && doubleEquals(fBottomPos, other.fBottomPos));
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("fTopPos", fTopPos) //$NON-NLS-1$
                    .add("fBottomPos", fBottomPos) //$NON-NLS-1$
                    .toString();
        }

        private static boolean doubleEquals(double d1, double d2) {
            return (Math.abs(d1 - d2) < EPSILON);
        }
    }
}
