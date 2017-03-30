/*
 * Copyright (C) 2016-2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.tmf2.views.ui.timegraph.swtjfx;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.lttng.scope.tmf2.views.core.TimeRange;

/**
 * {@link SwtJfxTimeGraphViewer} test suite unit-testing some static utility
 * methods.
 */
public class SwtJfxTimeGraphViewerStaticTest {

    private static final double DELTA = 0.1;

    /**
     * Test area consisting of 100 pixels representing a timerange from 1000 to
     * 2000.
     */
    private static class TestArea1 {
        private static final long START_TIMESTAMP = 1000;
        private static final long END_TIMESTAMP = 2000;
        private static final double NANOS_PER_PIXEL = 10.0;
    }

    /**
     * Test the {@link SwtJfxTimeGraphViewer#timestampToPaneXPos} method.
     */
    @Test
    public void testTimeToPosition() {
        double yPos = SwtJfxTimeGraphViewer.timestampToPaneXPos(1500,
                TimeRange.of(TestArea1.START_TIMESTAMP, TestArea1.END_TIMESTAMP),
                TestArea1.NANOS_PER_PIXEL);
        assertEquals(50.0, yPos, DELTA);

        long start = 1332170682440133097L;
        long end   = 1332170692664579801L;
        long ts1   = 1332170683481793497L;
        long ts2   = 1332170683485732407L;
        double yPos1 = SwtJfxTimeGraphViewer.timestampToPaneXPos(ts1, TimeRange.of(start, end), 10.0);
        double yPos2 = SwtJfxTimeGraphViewer.timestampToPaneXPos(ts2, TimeRange.of(start, end), 10.0);
        assertEquals(104166039.959, yPos1, DELTA);
        assertEquals(104559930.959, yPos2, DELTA);

    }

    /**
     * Test the {@link SwtJfxTimeGraphViewer#paneXPosToTimestamp} method.
     */
    @Test
    public void testPositionToTimestamp() {
        long ts = SwtJfxTimeGraphViewer.paneXPosToTimestamp(50.0,
                TestArea1.START_TIMESTAMP * TestArea1.NANOS_PER_PIXEL,
                TestArea1.START_TIMESTAMP,
                TestArea1.NANOS_PER_PIXEL);
        assertEquals(1500, ts);
    }
}
