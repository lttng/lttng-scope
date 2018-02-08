/*
 * Copyright (C) 2018 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.application

import org.lttng.scope.common.TimestampFormat

/**
 * Application-wide configuration options.
 */
object ScopeOptions {

    val timestampFormat = TimestampFormat.SECONDS_POINT_NANOS
}
