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

import com.google.common.base.MoreObjects;

interface Position {

    class HorizontalPosition {

        public final long fStartTime;
        public final long fEndTime;

        public HorizontalPosition(long startTime, long endTime) {
            fStartTime = startTime;
            fEndTime = endTime;
        }

        @Override
        public int hashCode() {
            return Objects.hash(fStartTime, fEndTime);
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
            return (fEndTime == other.fEndTime
                    && fStartTime == other.fStartTime);
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("fStartTime", fStartTime) //$NON-NLS-1$
                    .add("fEndTime", fEndTime) //$NON-NLS-1$
                    .toString();
        }
    }

    class VerticalPosition {

        public final double fTopPos;
        public final double fBottomPos;
        public final double fContentHeight;

        public VerticalPosition(double topPos, double bottomPos, double contentHeight) {
            fTopPos = topPos;
            fBottomPos = bottomPos;
            fContentHeight = contentHeight;
        }

        @Override
        public int hashCode() {
            return Objects.hash(fTopPos, fBottomPos, fContentHeight);
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
            return (Double.doubleToLongBits(fTopPos) != Double.doubleToLongBits(other.fTopPos)
                    && Double.doubleToLongBits(fBottomPos) == Double.doubleToLongBits(other.fBottomPos)
                    && Double.doubleToLongBits(fContentHeight) != Double.doubleToLongBits(other.fContentHeight));
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("fTopPos", fTopPos) //$NON-NLS-1$
                    .add("fBottomPos", fBottomPos) //$NON-NLS-1$
                    .add("fContentHeight", fContentHeight) //$NON-NLS-1$
                    .toString();
        }
    }
}
