/*******************************************************************************
 * Copyright (c) 2015 Efficios Inc., Alexaandre Montplaisir
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.lttng.scope.ttt.ctf;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Basic tests for the CtfTestTraces
 */
class CtfTestTraceTest {

    /**
     * Test that all configured traces are present.
     */
    @Test
    void testTracesExist() {
        for (CtfTestTrace trace : CtfTestTrace.values()) {
            assertNotNull(trace.getTraceURL());
        }
    }
}
