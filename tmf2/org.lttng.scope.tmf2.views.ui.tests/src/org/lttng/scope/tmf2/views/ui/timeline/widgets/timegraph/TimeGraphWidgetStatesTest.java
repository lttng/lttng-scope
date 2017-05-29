/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.tmf2.views.ui.timeline.widgets.timegraph;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.stream.Collectors;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.efficios.jabberwocky.common.TimeRange;

/**
 * {@link TimeGraphWidget} test suite testing the correctness of the rendered
 * state rectangles.
 */
@RunWith(Parameterized.class)
public class TimeGraphWidgetStatesTest extends TimeGraphWidgetTestBase {

    private static final long START_TIME = 150000L;

    private final int fTargetResolution;

    /**
     * Generator for test parameters.
     *
     * @return Test parameters
     */
    @Parameters(name = "resolution: {0}")
    public static Iterable<Object[]> getParameters() {
        return Arrays.asList(
                new Object[] { 1 },
                new Object[] { 2 },
                new Object[] { 5 },
                new Object[] { 10 },
                new Object[] { 50 },
                new Object[] { 100 }
                );
    }

    /**
     * Test constructor
     *
     * @param targetResolution
     *            The resolution we aim to have in the view.
     */
    public TimeGraphWidgetStatesTest(int targetResolution) {
        fTargetResolution = targetResolution;
    }

    /**
     * Test a very zoomed-in view, where all state system intervals should be
     * present in the rendered view.
     */
    @Test
    public void testStatesResolution() {
        /*
         * This width is the maximum number of nanoseconds the time range can
         * have to have a query resolution of 1.
         */
        double viewWidth = getTimeGraphWidth();
        long duration = (long) (viewWidth / 2.0) * fTargetResolution;
        TimeRange visibleRange = TimeRange.of(START_TIME, START_TIME + duration);
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
            assertTrue(entryStates.size() >= expectedSize);
            /* ... but never more than twice that number. */
            assertTrue(entryStates.size() <= 2 * expectedSize);
        }
    }

}
