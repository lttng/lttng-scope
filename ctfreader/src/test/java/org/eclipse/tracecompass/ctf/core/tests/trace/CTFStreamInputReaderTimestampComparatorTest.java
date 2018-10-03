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

import org.eclipse.tracecompass.internal.ctf.core.trace.StreamInputReaderTimestampComparator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * The class <code>StreamInputReaderTimestampComparatorTest</code> contains
 * tests for the class <code>{@link StreamInputReaderTimestampComparator}</code>
 *
 * @author ematkho
 * @version $Revision: 1.0 $
 */
class CTFStreamInputReaderTimestampComparatorTest {

    private StreamInputReaderTimestampComparator fixture;

    /**
     * Perform pre-test initialization.
     */
    @BeforeEach
    void setUp() {
        fixture = new StreamInputReaderTimestampComparator();
    }

    /**
     * Run the StreamInputReaderTimestampComparator() constructor test.
     */
    @Test
    void testStreamInputReaderTimestampComparator_1() {
        assertNotNull(fixture);
    }

}
