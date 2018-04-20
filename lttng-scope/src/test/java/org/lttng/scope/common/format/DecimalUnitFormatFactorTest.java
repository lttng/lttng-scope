/*******************************************************************************
 * Copyright (c) 2016 EfficiOS inc, Michael Jeanson
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
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test the {@link DecimalUnitFormat} class
 *
 * @author Michael Jeanson
 */
class DecimalUnitFormatFactorTest {

    private static Iterable<Arguments> getParameters() {
        return Arrays.asList(
                Arguments.of(0, "0", 10.0),
                Arguments.of(3, "300", 100.0),
                Arguments.of(975, "97.5", 0.1),
                Arguments.of(1000, "1 k", 1.0),
                Arguments.of(4000, "40", 0.01),
                Arguments.of(-4000, "-40", 0.01),
                Arguments.of(-0.04, "-4", 100.0),
                Arguments.of(0.002, "20", 10000.0),
                Arguments.of(0.0555, "5.5 k", 100000.0),
                Arguments.of(0.0004928373928, "49.3 n", 0.0001),
                Arguments.of(0.000000251, "251 p", 0.001),
                Arguments.of(Double.POSITIVE_INFINITY, "∞", 0.001),
                Arguments.of(Double.MAX_VALUE, "4", Double.MIN_NORMAL)
        );
    }

    /**
     * Test the {@link Format#format(Object)} method
     */
    @ParameterizedTest
    @MethodSource("getParameters")
    void testFormat(@NotNull Number value, @NotNull String expected, @NotNull Double factor) {
        Format format = new DecimalUnitFormat(factor);
        assertEquals(expected, format.format(value), "format value");
    }
}
