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
import org.lttng.scope.application.ScopeOptions
import java.math.BigDecimal
import java.time.*
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.temporal.Temporal
import java.util.*

const val NANOS_PER_SEC = 1000000000L
private val NANOS_PER_SEC_BD = BigDecimal(NANOS_PER_SEC)

enum class TimestampFormat {

    /** "yyyy-mm-dd hh:mm:ss.n tz" format */
    YMD_HMS_N_TZ {
        override fun tsToString(ts: Long): String = ts.toZonedDateTime().format(YMD_HMS_N_TZ_FORMATTERS[0])
        override fun stringToTs(projectRange: TimeRange, input: String) = parseString(input, YMD_HMS_N_TZ_FORMATTERS, ZonedDateTime::parse)?.toTimestamp()
    },

    /** "yyyy-mm-dd hh:mm:ss.n" format */
    YMD_HMS_N {
        override fun tsToString(ts: Long): String = ts.toLocalDateTime().format(YMD_HMS_N_FORMATTERS[0])
        override fun stringToTs(projectRange: TimeRange, input: String) = parseString(input, YMD_HMS_N_FORMATTERS, LocalDateTime::parse)?.toTimestamp()
    },

    /** "hh:mm:ss.n" format */
    HMS_N {
        override fun tsToString(ts: Long): String = ts.toLocalDateTime().format(HMS_N_FORMATTERS[0])
        override fun stringToTs(projectRange: TimeRange, input: String) = parseHours(projectRange, input, HMS_N_FORMATTERS)
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
        /** The time zone of the user's system. Should only be modified for testing purposes. */
        var systemTimeZone: ZoneId = ZoneId.systemDefault()
            internal set

        /*
         * Time formatters, see https://docs.oracle.com/javase/8/docs/api/java/time/format/DateTimeFormatter.html
         * The lists include all the acceptable ones for parsing, but the one at index 0 should be used for formatting.
         */
        private val YMD_HMS_N_TZ_FORMATTERS = listOf(
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSSSSS xxx"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSSSS xxx"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSSS xxx"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS xxx"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSS xxx"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSS xxx"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS xxx"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SS xxx"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S xxx"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss. xxx"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss xxx")
        )
        private val YMD_HMS_N_FORMATTERS = listOf(
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSSSSS"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSSSS"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSSS"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSS"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSS"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SS"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss."),
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        )
        private val HMS_N_TZ_FORMATTERS = listOf(
                DateTimeFormatter.ofPattern("HH:mm:ss.SSSSSSSSS xxx"),
                DateTimeFormatter.ofPattern("HH:mm:ss.SSSSSSSS xxx"),
                DateTimeFormatter.ofPattern("HH:mm:ss.SSSSSSS xxx"),
                DateTimeFormatter.ofPattern("HH:mm:ss.SSSSSS xxx"),
                DateTimeFormatter.ofPattern("HH:mm:ss.SSSSS xxx"),
                DateTimeFormatter.ofPattern("HH:mm:ss.SSSS xxx"),
                DateTimeFormatter.ofPattern("HH:mm:ss.SSS xxx"),
                DateTimeFormatter.ofPattern("HH:mm:ss.SS xxx"),
                DateTimeFormatter.ofPattern("HH:mm:ss.S xxx"),
                DateTimeFormatter.ofPattern("HH:mm:ss. xxx"),
                DateTimeFormatter.ofPattern("HH:mm:ss xxx")
        )
        private val HMS_N_FORMATTERS = listOf(
                DateTimeFormatter.ofPattern("HH:mm:ss.SSSSSSSSS"),
                DateTimeFormatter.ofPattern("HH:mm:ss.SSSSSSSS"),
                DateTimeFormatter.ofPattern("HH:mm:ss.SSSSSSS"),
                DateTimeFormatter.ofPattern("HH:mm:ss.SSSSSS"),
                DateTimeFormatter.ofPattern("HH:mm:ss.SSSSS"),
                DateTimeFormatter.ofPattern("HH:mm:ss.SSSS"),
                DateTimeFormatter.ofPattern("HH:mm:ss.SSS"),
                DateTimeFormatter.ofPattern("HH:mm:ss.SS"),
                DateTimeFormatter.ofPattern("HH:mm:ss.S"),
                DateTimeFormatter.ofPattern("HH:mm:ss."),
                DateTimeFormatter.ofPattern("HH:mm:ss")
        )

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

/**
 * Parse a string containing hours only (not the date). We will use the given project
 * range to determine the corresponding date.
 *
 * This conversion is impossible if the project range spans over 24 hours.
 */
private fun parseHours(projectRange: TimeRange, input: String, formatters: List<DateTimeFormatter>): Long? {
    if (projectRange.duration > NANOS_PER_SEC * 60 * 60 * 24) {
        throw IllegalArgumentException("Cannot parse string $input as H:M:S format because range $projectRange spans over 24 hours.")
    }

    val startDateTime = projectRange.startTime.toLocalDateTime()
    val endDateTime = projectRange.endTime.toLocalDateTime()
    val tsTime = parseString(input, formatters, LocalTime::parse) ?: return null

    /* Only use this timestamp if it's part of the project range. */
    with(LocalDateTime.of(startDateTime.toLocalDate(), tsTime)) {
        if (this >= startDateTime && this <= endDateTime) {
            return this.toTimestamp()
        }
    }
    with(LocalDateTime.of(endDateTime.toLocalDate(), tsTime)) {
        if (this >= startDateTime && this <= endDateTime) {
            return this.toTimestamp()
        }
    }

    /* The target timestamp is not part of the project's range. */
    return null
}

/**
 * Convert a framework timstamp (long, representing nanoseconds since epoch)
 * into a [LocalDateTime] object in the currently configured time zone.
 */
private fun Long.toLocalDateTime(): LocalDateTime {
    val s = this / NANOS_PER_SEC
    val ns = this % NANOS_PER_SEC
    val instant = Instant.ofEpochSecond(s, ns)
    val timeZoneOffset = when (ScopeOptions.timestampTimeZone) {
        ScopeOptions.DisplayTimeZone.LOCAL -> TimestampFormat.systemTimeZone.rules.getOffset(instant)
        ScopeOptions.DisplayTimeZone.UTC -> ZoneOffset.UTC
    }
    return LocalDateTime.ofInstant(instant, timeZoneOffset)
}

private fun Long.toZonedDateTime(): ZonedDateTime {
    val s = this / NANOS_PER_SEC
    val ns = this % NANOS_PER_SEC
    val instant = Instant.ofEpochSecond(s, ns)
    val timeZoneOffset = when (ScopeOptions.timestampTimeZone) {
        ScopeOptions.DisplayTimeZone.LOCAL -> TimestampFormat.systemTimeZone.rules.getOffset(instant)
        ScopeOptions.DisplayTimeZone.UTC -> ZoneOffset.UTC
    }
    return ZonedDateTime.ofInstant(instant, timeZoneOffset)
}

/**
 * Convert a [LocalDateTime] to a framework timestamp in the currently configured time zone.
 */
private fun LocalDateTime.toTimestamp(): Long {
    val timeZoneOffset = when (ScopeOptions.timestampTimeZone) {
        ScopeOptions.DisplayTimeZone.LOCAL -> TimestampFormat.systemTimeZone.rules.getOffset(this)
        ScopeOptions.DisplayTimeZone.UTC -> ZoneOffset.UTC
    }

    return with(this.toInstant(timeZoneOffset)) {
        epochSecond * NANOS_PER_SEC + nano
    }
}

private fun ZonedDateTime.toTimestamp(): Long {
    return with(this.toInstant()) {
        epochSecond * NANOS_PER_SEC + nano
    }
}

/**
 * Attempt to parse the given string using a list of provided formatters.
 * The formatters will be tried according to the iteration order, until a working one is
 * found. If none of the formatters can parse the string, then null is returned.
 *
 * The 'parseFunction' parameter will determine the returned type, for example you can pass
 * "LocalTime::parse", which will return a [LocalTime] object. The parse should either return
 * null or throw a [DateTimeParseException] to indicate it cannot parse the given string with
 * a given formatter.
 */
private fun <T : Temporal> parseString(input: String, formatters: List<DateTimeFormatter>, parseFunction: (String, DateTimeFormatter) -> T): T? =
        formatters
                .mapNotNull {
                    try {
                        parseFunction(input, it)
                    } catch (e: DateTimeParseException) {
                        null
                    }
                }
                .firstOrNull()
