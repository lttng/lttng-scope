/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.efficios.jabberwocky.views.common

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class ColorDefinitionTest {

    @Test
    fun testValid() {
        ColorDefinition(  0,   0  , 0)
        ColorDefinition(128, 128, 128)
        ColorDefinition(255, 255, 255)

        ColorDefinition(  0,   0,   0,   0)
        ColorDefinition(128, 128, 128, 128)
        ColorDefinition(255, 255, 255, 255)
    }

    @Test
    fun testInvalid1() {
        assertThrows<IllegalArgumentException> { ColorDefinition(-1, 0, 0, 0) }
    }

    @Test
    fun testInvalid2() {
        assertThrows<IllegalArgumentException> { ColorDefinition(0, 0, 500, 0) }
    }
}