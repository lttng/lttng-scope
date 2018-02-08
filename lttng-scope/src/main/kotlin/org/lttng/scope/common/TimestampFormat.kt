/*
 * Copyright (C) 2017-2018 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.common

import java.math.BigDecimal

enum class TimestampFormat {

    /** "s.ns" format */
    SECONDS_POINT_NANOS {
        override fun tsToString(ts: Long): String {
            val s = ts / NANOS_PER_SEC
            val ns = ts % NANOS_PER_SEC
            return "%d.%09d".format(s, ns)
        }

        override fun stringToTs(input: String): Long? {
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
    };

    companion object {
        const val NANOS_PER_SEC = 1000000000L
        private val NANOS_PER_SEC_BD = BigDecimal(NANOS_PER_SEC)
    }

    /**
     * Convert a framework timestamp into a string for the UI.
     */
    abstract fun tsToString(ts: Long): String

    /**
     * Convert a string to a timestamp (in nanos).
     *
     * @return The long value, or null if the string is not parseable
     */
    abstract fun stringToTs(input: String): Long?
}
