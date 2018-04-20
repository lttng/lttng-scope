/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
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
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test the {@link DataSizeWithUnitFormat} class
 *
 * @author Geneviève Bastien
 */
public class DataSizeFormatTest {

    private static final @NotNull Format FORMAT = DataSizeWithUnitFormat.getInstance();

    protected static Stream<Arguments> getParameters() {
        return Stream.of(
                Arguments.of(0, "0", 0L),
                Arguments.of(3, "3 B", 3L),
                Arguments.of(975, "975 B", 975L),
                Arguments.of(1024, "1 KB", 1024L),
                Arguments.of(1024 * 1024, "1 MB", 1024 * 1024L),
                Arguments.of(1024 * 1024 * 1024, "1 GB", 1024 * 1024 * 1024L),
                Arguments.of(1024L * 1024L * 1024L * 1024L, "1 TB", 1024 * 1024 * 1024 * 1024L),
                Arguments.of(4096, "4 KB", 4096L),
                Arguments.of(-4096, "-4 KB", -4096L),
                Arguments.of(4096L, "4 KB", 4096L),
                Arguments.of(4096.0, "4 KB", 4096L),
                Arguments.of(12345678, "11.774 MB", 12345933.824),
                Arguments.of(Integer.MAX_VALUE, "2 GB", 2147483648L),
                Arguments.of(Integer.MIN_VALUE, "-2 GB", -2147483648L),
                Arguments.of(Long.MAX_VALUE, "8388608 TB", 9.223372036854775808E18),
                Arguments.of(98765432.123456, "94.19 MB", 98765373.44),
                Arguments.of(-98765432.123456, "-94.19 MB", -98765373.44),
                Arguments.of(555555555555L, "517.401 GB", 555555093479.424),
                Arguments.of(555555555555555L, "505.275 TB", 555555737724518.4)
        );
    }

    /**
     * Get the formatted to use for the unit test
     *
     * @return The formatter to use for the unit test
     */
    protected Format getFormatter() {
        return FORMAT;
    }

    /**
     * Test the {@link Format#format(Object)} method
     */
    @ParameterizedTest
    @MethodSource("getParameters")
    void testFormat(@NotNull Number numValue, @NotNull String stringValue, @NotNull Number parseValue) {
        assertEquals(stringValue, getFormatter().format(numValue), "format value");
    }

    /**
     * Test the {@link Format#parseObject(String)} method
     *
     * @throws ParseException if the string cannot be parsed
     */
    @ParameterizedTest
    @MethodSource("getParameters")
    void testParseObject(@NotNull Number numValue, @NotNull String stringValue, @NotNull Number parseValue) throws ParseException {
        assertEquals(parseValue, getFormatter().parseObject(stringValue), "parseObject value");
    }
}
