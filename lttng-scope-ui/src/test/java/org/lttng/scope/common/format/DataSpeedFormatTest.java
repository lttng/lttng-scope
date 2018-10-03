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

/**
 * Test the {@link DataSpeedWithUnitFormat} class
 *
 * @author Geneviève Bastien
 */
public class DataSpeedFormatTest extends DataSizeFormatTest {

    private static final @NotNull Format FORMAT = DataSpeedWithUnitFormat.getInstance();
    private static final String PER_SECOND = "/s";

    private static Stream<Arguments> getParametersSubClass() {
        /* Add "/s" to the formatted values, rest of the test data is the same. */
        return DataSizeFormatTest.getParameters()
                .map(arguments -> {
                    Object[] args = arguments.get();
                    return Arguments.of(args[0], args[1] + PER_SECOND, args[2]);
                });
    }

    @Override
    protected Format getFormatter() {
        return FORMAT;
    }

    @ParameterizedTest
    @MethodSource("getParametersSubClass")
    void testFormat(@NotNull Number numValue, @NotNull String stringValue, @NotNull Number parseValue) {
        super.testFormat(numValue, stringValue, parseValue);
    }

    /**
     * Test the {@link Format#parseObject(String)} method
     *
     * @throws ParseException if the string cannot be parsed
     */
    @ParameterizedTest
    @MethodSource("getParametersSubClass")
    void testParseObject(@NotNull Number numValue, @NotNull String stringValue, @NotNull Number parseValue) throws ParseException {
        super.testParseObject(numValue, stringValue, parseValue);
    }
}
