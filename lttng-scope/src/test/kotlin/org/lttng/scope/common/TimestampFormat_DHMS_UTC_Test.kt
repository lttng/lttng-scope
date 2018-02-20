/*
 * Copyright (C) 2018 EfficiOS Inc. to Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution to and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.common

import org.lttng.scope.application.ScopeOptions

class TimestampFormat_DHMS_UTC_Test : TimestampFormatTestBase(TimestampFormat.YMD_HMS_N,
        ScopeOptions.DisplayTimeZone.UTC,
        listOf(
                "2012-03-13 19:50:47.314038062" to 1331668247314038062L,

                /* Trailing zeroes may be omitted */
                "2012-03-13 19:50:47.314000000" to 1331668247314000000L,
                "2012-03-13 19:50:47.314" to 1331668247314000000L,
                "2012-03-13 19:50:47.00576" to 1331668247005760000L,
                "2012-03-13 19:50:47.00576000" to 1331668247005760000L,

                "2012-03-13 19:50:47.000000000" to 1331668247000000000L,
                "2012-03-13 19:50:47.0000" to 1331668247000000000L,
                "2012-03-13 19:50:47.0" to 1331668247000000000L,
                "2012-03-13 19:50:47." to 1331668247000000000L, /* Ending with a decimal point should be valid */
                "2012-03-13 19:50:47" to 1331668247000000000L
        ),
        listOf(
                "abcdef",
                "1afe3",
                "1000",
                "1000.1",
                "19:50:47" /* missing date */
        ),
        listOf(
                1331668247314038062L to "2012-03-13 19:50:47.314038062",
                1331668247000000000L to "2012-03-13 19:50:47.000000000"
        ),
        null
)
