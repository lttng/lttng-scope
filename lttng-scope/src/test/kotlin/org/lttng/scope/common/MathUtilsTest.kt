/*
 * Copyright (C) 2017-2018 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.common

import org.hamcrest.core.Is.`is`
import org.junit.Assert.assertThat
import org.junit.Test

/**
 * Tests for MathUtils.
 */
class MathUtilsTest {

    /**
     * Test the {@link MathUtils#roundToClosestHigherMultiple(long, long)}
     * method.
     */
    @Test
    fun testRoundToClosestHigherMultiple() {
        assertThat(12L.roundToClosestHigherMultiple(10L), `is`(20L))
        assertThat(20L.roundToClosestHigherMultiple(20L), `is`(20L))
    }

    @Test
    fun testClamp() {
        assertThat(10L.clampMin(15L), `is`(15L))
        assertThat(15L.clampMin(15L), `is`(15L))
        assertThat(20L.clampMin(15L), `is`(20L))

        assertThat(10L.clampMax(15L), `is`(10L))
        assertThat(15L.clampMax(15L), `is`(15L))
        assertThat(20L.clampMax(15L), `is`(15L))

        assertThat( 5L.clamp(10L, 20L), `is`(10L))
        assertThat(10L.clamp(10L, 20L), `is`(10L))
        assertThat(15L.clamp(10L, 20L), `is`(15L))
        assertThat(20L.clamp(10L, 20L), `is`(20L))
        assertThat(25L.clamp(10L, 20L), `is`(20L))
    }
}
