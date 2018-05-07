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
import org.junit.jupiter.api.fail
import java.util.concurrent.atomic.AtomicBoolean


/**
 * Tests for the {@link NestingBoolean} class.
 * <p>
 * TODO Multi-threaded tests
 */
class NestingBooleanTest {

    private val fixture = NestingBoolean()

    /**
     * Test the initial value
     */
    @Test
    fun testInitial() {
        assertThat(fixture.enabledProperty().get()).isTrue()
    }

    /**
     * Test sequential changes
     */
    @Test
    fun testChanges() {
        fixture.disable()
        assertThat(fixture.enabledProperty().get()).isFalse()
        fixture.enable()
        assertThat(fixture.enabledProperty().get()).isTrue()

        fixture.disable()
        fixture.disable()
        assertThat(fixture.enabledProperty().get()).isFalse()
        fixture.enable()
        /* Still disabled! */
        assertThat(fixture.enabledProperty().get()).isFalse()
        fixture.enable()
        assertThat(fixture.enabledProperty().get()).isTrue()
    }

    /**
     * Test that enabling an already-enabled boolean does not trigger change
     * listeners.
     */
    @Test
    fun testEnablingNoChange() {
        fixture.enabledProperty().addListener { _, _, _ ->
            fail("Listener triggered")
        }
        fixture.enable()
        fixture.enable()
        fixture.enable()
    }

    /**
     * Test that disabling an already-disabled boolean does not trigger change
     * listeners.
     */
    @Test
    fun testDisablingNoChange() {
        fixture.disable()
        fixture.enabledProperty().addListener { _, _, _ ->
            fail("Listener triggered")
        }
        fixture.disable()
        fixture.disable()
        fixture.disable()
    }

    /**
     * Test that really changing the value to enabled triggers a change
     * listener.
     */
    @Test
    fun testEnablingChange() {
        val received = AtomicBoolean(false)
        fixture.disable()
        fixture.enabledProperty().addListener { _, _, newVal ->
            if (newVal == true) {
                received.set(true)
            }
        }
        fixture.enable()

        assertThat(received.get()).isTrue()
    }

    /**
     * Test that really changing the value to disabled triggers a change
     * listener.
     */
    @Test
    fun testDisablingChange() {
        val received = AtomicBoolean(false)
        fixture.enabledProperty().addListener { _, _, newVal ->
            if (newVal == false) {
                received.set(true)
            }
        }
        fixture.disable()

        assertThat(received.get()).isTrue()
    }
}
