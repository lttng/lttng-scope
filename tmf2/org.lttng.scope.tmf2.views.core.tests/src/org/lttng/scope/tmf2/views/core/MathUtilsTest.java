/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.tmf2.views.core;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Tests for {@link MathUtils}.
 */
public class MathUtilsTest {

    /**
     * Test the {@link MathUtils#roundToClosestHigherMultiple(long, long)}
     * method.
     */
    @Test
    public void testRoundToClosestHigherMultiple() {
        long res1 = MathUtils.roundToClosestHigherMultiple(12, 10);
        assertEquals(20, res1);

        long res2 = MathUtils.roundToClosestHigherMultiple(20, 20);
        assertEquals(20, res2);
    }
}
