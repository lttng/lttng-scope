/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */


package org.lttng.scope.common;

import org.junit.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.*;

/**
 * Tests for the {@link NestingBoolean} class.
 *
 * TODO Multi-threaded tests
 */
public class NestingBooleanTest {

    private final NestingBoolean fixture = new NestingBoolean();

    /**
     * Test the initial value
     */
    @Test
    public void testInitial() {
        assertTrue(fixture.enabledProperty().get());
    }

    /**
     * Test sequential changes
     */
    @Test
    public void testChanges() {
        fixture.disable();
        assertFalse(fixture.enabledProperty().get());
        fixture.enable();
        assertTrue(fixture.enabledProperty().get());

        fixture.disable();
        fixture.disable();
        assertFalse(fixture.enabledProperty().get());
        fixture.enable();
        /* Still disabled! */
        assertFalse(fixture.enabledProperty().get());
        fixture.enable();
        assertTrue(fixture.enabledProperty().get());
    }

    /**
     * Test that enabling an already-enabling boolean does not trigger change
     * listeners.
     */
    @Test
    public void testEnablingNoChange() {
        fixture.enabledProperty().addListener((obs, oldVal, newVal) -> {
            fail();
        });
        fixture.enable();
        fixture.enable();
        fixture.enable();
    }

    /**
     * Test that disabling an already-disabled boolean does not trigger change
     * listeners.
     */
    @Test
    public void testDisablingNoChange() {
        fixture.disable();
        fixture.enabledProperty().addListener((obs, oldVal, newVal) -> {
            fail();
        });
        fixture.disable();
        fixture.disable();
        fixture.disable();
    }

    /**
     * Test that really changing the value to enabled triggers a change
     * listener.
     */
    @Test
    public void testEnablingChange() {
        AtomicBoolean received = new AtomicBoolean(false);
        fixture.disable();
        fixture.enabledProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == true) {
                received.set(true);
            }
        });
        fixture.enable();

        assertTrue(received.get());
    }

    /**
     * Test that really changing the value to disabled triggers a change
     * listener.
     */
    @Test
    public void testDisablingChange() {
        AtomicBoolean received = new AtomicBoolean(false);
        fixture.enabledProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == false) {
                received.set(true);
            }
        });
        fixture.disable();

        assertTrue(received.get());
    }
}
