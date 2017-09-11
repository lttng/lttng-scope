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

import java.text.Format;

/**
 * Test the {@link DataSpeedWithUnitFormat} class
 *
 * @author Geneviève Bastien
 */
public class DataSpeedFormatTest extends DataSizeFormatTest {

    private static final @NotNull Format FORMAT = DataSpeedWithUnitFormat.getInstance();
    private static final String PER_SECOND = "/s";

    /**
     * Constructor
     *
     * @param numValue
     *            The numeric value
     * @param stringValue
     *            The string value
     * @param parseValue
     *            The parse value of the string value
     */
    public DataSpeedFormatTest(@NotNull Number numValue, @NotNull String stringValue, @NotNull Number parseValue) {
        super(numValue, stringValue + PER_SECOND, parseValue);
    }

    @Override
    protected Format getFormatter() {
        return FORMAT;
    }
}
