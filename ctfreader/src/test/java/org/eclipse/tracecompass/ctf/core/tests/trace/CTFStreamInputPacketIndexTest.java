/*******************************************************************************
 * Copyright (c) 2013, 2014 Ericsson
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Khouzam - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.ctf.core.tests.trace;

import org.eclipse.tracecompass.ctf.core.CTFException;
import org.eclipse.tracecompass.internal.ctf.core.trace.StreamInputPacketIndex;
import org.eclipse.tracecompass.internal.ctf.core.trace.StreamInputPacketIndexEntry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * The class <code>StreamInputPacketIndexTest</code> contains tests for the
 * class <code>{@link StreamInputPacketIndex}</code>.
 *
 * @author Matthew Khouzam
 */
@SuppressWarnings("javadoc")
class CTFStreamInputPacketIndexTest {

    private StreamInputPacketIndex fixture;

    /**
     * Perform pre-test initialization.
     *
     * @throws CTFException
     */
    @BeforeEach
    void setUp() {
        fixture = new StreamInputPacketIndex();
        fixture.append(new StreamInputPacketIndexEntry(1L,0L));
    }

    /**
     * Run the StreamInputPacketIndex() constructor test.
     */
    @Test
    void testStreamInputPacketIndex() {
        assertNotNull(fixture);
        assertNotNull(fixture.getElement(0));
    }

}