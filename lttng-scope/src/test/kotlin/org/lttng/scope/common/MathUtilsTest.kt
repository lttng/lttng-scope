/*
 * Copyright (C) 2017-2018 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.common

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

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
        assertThat(12L.roundToClosestHigherMultiple(10L)).isEqualTo(20L)
        assertThat(20L.roundToClosestHigherMultiple(20L)).isEqualTo(20L)
    }

    @Test
    fun testClamp() {
        assertThat(10L.clampMin(15L)).isEqualTo(15L)
        assertThat(15L.clampMin(15L)).isEqualTo(15L)
        assertThat(20L.clampMin(15L)).isEqualTo(20L)

        assertThat(10L.clampMax(15L)).isEqualTo(10L)
        assertThat(15L.clampMax(15L)).isEqualTo(15L)
        assertThat(20L.clampMax(15L)).isEqualTo(15L)

        assertThat( 5L.clamp(10L, 20L)).isEqualTo(10L)
        assertThat(10L.clamp(10L, 20L)).isEqualTo(10L)
        assertThat(15L.clamp(10L, 20L)).isEqualTo(15L)
        assertThat(20L.clamp(10L, 20L)).isEqualTo(20L)
        assertThat(25L.clamp(10L, 20L)).isEqualTo(20L)
    }
}
