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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.text.Format;
import java.text.ParseException;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test the {@link DecimalUnitFormat} class
 *
 * @author Michael Jeanson
 */
class DecimalUnitFormatTest {

    private static final @NotNull Format FORMATTER = new DecimalUnitFormat();

    private static Iterable<Arguments> getParameters() {
        return Arrays.asList(
                Arguments.of(3, "3", 3L),
                Arguments.of(5.6, "5.6", 5.6),
                Arguments.of(1.234567, "1.2", 1.2),
                Arguments.of(1.01, "1", 1L),
                Arguments.of(975, "975", 975L),
                Arguments.of(1000, "1 k", 1000L),
                Arguments.of(4000, "4 k", 4000L),
                Arguments.of(-4000, "-4 k", -4000L),
                Arguments.of(4000L, "4 k", 4000L),
                Arguments.of(4000.0, "4 k", 4000L),
                Arguments.of(12345678, "12.3 M", 12300000L),
                Arguments.of(Integer.MAX_VALUE, "2.1 G", 2100000000L),
                Arguments.of(Integer.MIN_VALUE, "-2.1 G", -2100000000L),
                Arguments.of(Long.MAX_VALUE, "9223.4 P", 9.2234E18),
                Arguments.of(98765432.123456, "98.8 M", 98800000L),
                Arguments.of(-98765432.123456, "-98.8 M", -98800000L),
                Arguments.of(555555555555L, "555.6 G", 555600000000L),
                Arguments.of(555555555555555L, "555.6 T", 555600000000000L),
                Arguments.of(100100000, "100.1 M", 100100000L),
                Arguments.of(0.1, "100 m", 0.1),
                Arguments.of(0.001, "1 m", 0.001),
                Arguments.of(0.000001, "1 µ", 0.000001),
                Arguments.of(0.000000001, "1 n", 0.000000001),
                Arguments.of(0.000000000001, "1 p", 0.000000000001),
                Arguments.of(0.0000000000001, "0", 0L),
                Arguments.of(-0.04, "-40 m", -0.04),
                Arguments.of(0.002, "2 m", 0.002),
                Arguments.of(0.0555, "55.5 m", 0.0555),
                Arguments.of(0.0004928373928, "492.8 µ", 0.0004928),
                Arguments.of(0.000000251, "251 n", 0.000000251),
                Arguments.of(0.000000000043, "43 p", 0.000000000043),
                Arguments.of(0.000000045643, "45.6 n", 0.0000000456),
                Arguments.of(Double.MAX_VALUE, "1.7976931348623157E308", 1.7976931348623157E308),
                Arguments.of(Double.POSITIVE_INFINITY, "∞", Double.POSITIVE_INFINITY),
                Arguments.of(Double.MIN_NORMAL, "0", 0L),
                Arguments.of(Double.NEGATIVE_INFINITY, "-∞", Double.NEGATIVE_INFINITY),
                Arguments.of(Double.NaN, "�", Double.NaN)
        );
    }

    /**
     * Test the {@link Format#format(Object)} method
     */
    @ParameterizedTest
    @MethodSource("getParameters")
    void testFormat(@NotNull Number numValue, @NotNull String stringValue, @NotNull Number parseValue) {
        assertEquals(stringValue, FORMATTER.format(numValue), "format value");
    }

    /**
     * Test the {@link Format#parseObject(String)} method
     *
     * @throws ParseException if the string cannot be parsed
     */
    @ParameterizedTest
    @MethodSource("getParameters")
    void testParseObject(@NotNull Number numValue, @NotNull String stringValue, @NotNull Number parseValue) throws ParseException {
        assertEquals(parseValue, FORMATTER.parseObject(stringValue), "parseObject value");
    }
}
