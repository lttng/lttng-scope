/*
 * Copyright (C) 2018 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.application

import com.efficios.jabberwocky.common.ConfigOption
import org.lttng.scope.common.TimestampFormat

/**
 * Application-wide configuration options.
 */
object ScopeOptions {

    /* Format to use for timestamp formatting */
    private val timestampFormatProperty: ConfigOption<TimestampFormat> = ConfigOption(TimestampFormat.HMS_N)
    fun timestampFormatProperty() = timestampFormatProperty
    var timestampFormat: TimestampFormat
        get() = timestampFormatProperty.get()
        set(value) = timestampFormatProperty.set(value)
}
