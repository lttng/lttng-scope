/*
 * Copyright (C) 2017-2018 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.views.timecontrol

import java.math.BigDecimal
import java.util.regex.Pattern

object TimestampConversion {

    const val NANOS_PER_SEC = 1000000000L
    private val NANOS_PER_SEC_BD = BigDecimal(NANOS_PER_SEC)

    /**
     * Non-numerical characters that might be part of the displayed values but
     * should not be interpreted when trying to match them to a Long value.
     */
    private val NON_NUMERICAL_CHARACTERS = Pattern.compile("[.,;:\\s]")

    private val POINT_PATTERN = Pattern.compile("\\.")

    /**
     * Convert a framework timestamp into a string for the UI (and readable by
     * Babeltrace).
     */
    fun tsToString(ts: Long): String {
        /* Same timestamp format as Babeltrace */
        val s = ts / NANOS_PER_SEC
        val ns = ts % NANOS_PER_SEC
        return "%d.%09d".format(s, ns)
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
    fun stringToTs(input: String): Long? {
        return parseSNS(input)
    }

    /**
     * Parse the format s.ns. If there is no "." we assume the number represents
     * nanos.
     */
    private fun parseSNS(input: String): Long? {
        val nbPoints = input.chars().filter { it.toChar() == '.' }.count().toInt()
        if (nbPoints > 1) {
            /* Only 1 decimal point allowed */
            return null
        }
        return try {
            if (nbPoints == 0) {
                /* Keep the value as nanoseconds. */
                BigDecimal(input).toLong()
            } else {
                /* Parse as seconds then convert to nanos. */
                BigDecimal(input).multiply(NANOS_PER_SEC_BD).toLong()
            }
        } catch (e: NumberFormatException) {
            null
        }
    }

}
