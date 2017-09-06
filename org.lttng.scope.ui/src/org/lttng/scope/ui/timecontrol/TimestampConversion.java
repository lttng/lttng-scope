/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.ui.timecontrol;

import java.math.BigDecimal;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.Nullable;

public final class TimestampConversion {

    private static final long NANOS_PER_SEC = 1000000000;
    private static final BigDecimal NANOS_PER_SEC_BD = new BigDecimal(NANOS_PER_SEC);

    /**
     * Non-numerical characters that might be part of the displayed values but
     * should not be interpreted when trying to match them to a Long value.
     */
    private static final Pattern NON_NUMERICAL_CHARACTERS = Pattern.compile("[.,;:\\s]");

    private static final Pattern POINT_PATTERN = Pattern.compile("\\.");

    private TimestampConversion() {}

    /**
     * Convert a framework timestamp into a string for the UI (and readable by
     * Babeltrace).
     */
    public static String tsToString(long ts) {
        /* Same timestamp format as Babeltrace */
        long s = ts / NANOS_PER_SEC;
        long ns = ts % NANOS_PER_SEC;
        return String.format("%d.%09d", s, ns);
    }

    /**
     * Convert a string to a timestamp (in nanos).
     *
     * Supported formats:
     * <ul>
     * <li>s.ns</li>
     * </ul>
     *
     * TODO Add more!
     *
     * @return The long value, or null if the string is not parseable
     */
    public static @Nullable Long stringToTs(String input) {
        return parseSNS(input);
    }

    /**
     * Parse the format s.ns. If there is no "." we assume the number represents
     * nanos.
     */
    private static @Nullable Long parseSNS(String input) {
        long nbPoints = input.chars().filter(c -> c == '.').count();
        if (nbPoints > 1) {
            /* Only 1 decimal point allowed */
            return null;
        }
        try {
            BigDecimal bd = new BigDecimal(input);
            if (nbPoints == 0) {
                /* Keep the value as nanoseconds. */
            } else {
                /* Parse as seconds then convert to nanos. */
                bd = bd.multiply(NANOS_PER_SEC_BD);
            }
            return bd.longValue();

        } catch (NumberFormatException e) {
            return null;
        }
    }

}
