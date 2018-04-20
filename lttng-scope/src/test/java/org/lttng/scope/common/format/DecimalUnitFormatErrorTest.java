/*******************************************************************************
 * Copyright (c) 2016 EfficiOS Inc., Michael Jeanson and others
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.lttng.scope.common.format;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.text.Format;
import java.text.ParseException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Test the {@link DecimalUnitFormat} class
 *
 * @author Michael Jeanson
 */
class DecimalUnitFormatErrorTest {

    private static final @NotNull Format FORMATTER = new DecimalUnitFormat();

    /**
     * Test format with an illegal argument
     */
    @Test
    void testFormatIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () -> FORMATTER.format("Toto"));
    }

    /**
     * Test parsing a string that is not a number
     */
    @Test
    void testParseNotANumber() {
        assertThrows(ParseException.class, () -> FORMATTER.parseObject("Toto"));

    }

    /**
     * Test parsing a number with a unit
     *
     * @throws ParseException if the string cannot be parsed
     */
    @Test
    void testParseWithUnit() throws ParseException {
        assertEquals(1.2, FORMATTER.parseObject("1.2 s"));
    }

    /**
     * Test parsing a number with a prefix and a unit
     *
     * @throws ParseException if the string cannot be parsed
     */
    @Test
    void testParsePrefixWithUnitAndPrefix() throws ParseException {
        assertEquals(0.0012, FORMATTER.parseObject("1.2 ms"));
    }

    /**
     * Test parsing a special Double number with a prefix
     *
     * @throws ParseException if the string cannot be parsed
     */
    @Test
    void testParseSpecialWithPrefix() throws ParseException {
        assertEquals(Double.POSITIVE_INFINITY, FORMATTER.parseObject("∞ k"));
        assertEquals(Double.NEGATIVE_INFINITY, FORMATTER.parseObject("-∞ p"));
        assertEquals(Double.NaN, FORMATTER.parseObject("�M"));
    }
}
