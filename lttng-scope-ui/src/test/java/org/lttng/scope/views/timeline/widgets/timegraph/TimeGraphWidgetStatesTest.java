/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.views.timeline.widgets.timegraph;

import com.efficios.jabberwocky.common.TimeRange;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.lttng.scope.common.tests.StubTrace;

import java.util.Collection;
import java.util.Comparator;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@link TimeGraphWidget} test suite testing the correctness of the rendered
 * state rectangles.
 */
@Disabled("Needs reimplementation in proper testing framework")
class TimeGraphWidgetStatesTest extends TimeGraphWidgetTestBase {

    private static final long START_TIME = 150000L;

    /**
     * Test a very zoomed-in view, where all state system intervals should be
     * present in the rendered view.
     */
    @ParameterizedTest
    @ValueSource(ints = {1, 2, 5, 10, 50, 100})
    void testStatesResolution(int targetResolution) {
        repaint();

        /*
         * This width is the maximum number of nanoseconds the time range can
         * have to have a query resolution of 1.
         */
        double viewWidth = getTimeGraphWidth();
        long duration = (long) (viewWidth / 2.0) * targetResolution;
        long end = Math.min(START_TIME + duration, StubTrace.FULL_TRACE_END_TIME);
        TimeRange visibleRange = TimeRange.of(START_TIME, end);

        renderRange(visibleRange);
        Collection<StateRectangle> renderedStates = getWidget().getRenderedStateRectangles();

        /* Check the states for each of the first 10 tree entries. */
        for (int i = 1; i <= 10; i++) {
            int entryIndex = i;
            Collection<StateRectangle> entryStates = renderedStates.stream()
                    .filter(rect -> rect.getStateInterval().getTreeElement().getName().equals(StubModelProvider.ENTRY_NAME_PREFIX + entryIndex))
                    .sorted(Comparator.comparingLong(rect -> rect.getStateInterval().getStartEvent().getTimestamp()))
                    .collect(Collectors.toList());

            /* There should be no duplicates */
            assertEquals(entryStates.stream().distinct().count(), entryStates.size());

            /*
             * Check the minimum number of states. There might be more than
             * expected due to prefetching on each side ...
             */
            int expectedSize = (int) (duration / (entryIndex * StubModelStateProvider.DURATION_FACTOR));
            assertThat(entryStates.size()).isGreaterThanOrEqualTo(expectedSize);
            /* ... but never more than twice that number. */
            assertThat(entryStates.size()).isLessThanOrEqualTo(2 * expectedSize);
        }
    }

}
