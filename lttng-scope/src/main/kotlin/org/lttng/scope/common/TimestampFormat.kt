/*
 * Copyright (C) 2017-2018 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.common

import com.efficios.jabberwocky.common.TimeRange
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

enum class TimestampFormat {

    /** "yyyy-mm-dd hh:mm:ss.n" format */
    YMD_HMS_N {
        override fun tsToString(ts: Long): String {
            val s = ts / NANOS_PER_SEC
            val ns = ts % NANOS_PER_SEC
            val dateTime = ZonedDateTime.ofInstant(Instant.ofEpochSecond(s, ns), ZoneOffset.UTC)
            return dateTime.format(YMD_HMS_N_FORMATTER)
        }

        override fun stringToTs(projectRange: TimeRange, input: String): Long? {
            return YMD_HMS_N_INPUT_FORMATTERS
                    .map {
                        try {
                            LocalDateTime.parse(input, it)
                        } catch (e: DateTimeParseException) {
                            null
                        }
                    }
                    .filterNotNull()
                    .firstOrNull()
                    ?.let {
                        with(it.toInstant(ZoneOffset.UTC)) {
                            epochSecond * NANOS_PER_SEC + nano
                        }
                    }
        }
    },

    /** "s.ns" format */
    SECONDS_POINT_NANOS {
        override fun tsToString(ts: Long): String {
            val s = ts / NANOS_PER_SEC
            val ns = ts % NANOS_PER_SEC
            return "%d.%09d".format(s, ns)
        }

        override fun stringToTs(projectRange: TimeRange, input: String): Long? {
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

        /* Time formatters, see https://docs.oracle.com/javase/8/docs/api/java/time/format/DateTimeFormatter.html */

        /* "Be specific in what you give ..." */
        private val YMD_HMS_N_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSSSSS")
        /* "... but liberal in what you accept." */
        private val YMD_HMS_N_INPUT_FORMATTERS = listOf(
                YMD_HMS_N_FORMATTER,
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSSSS"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSSS"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSS"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSS"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SS"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss."),
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
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
    abstract fun stringToTs(projectRange: TimeRange, input: String): Long?
}
