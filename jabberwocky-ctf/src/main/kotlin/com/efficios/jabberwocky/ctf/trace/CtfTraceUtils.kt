/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.efficios.jabberwocky.ctf.trace

import com.efficios.jabberwocky.ctf.trace.event.CtfTraceEvent
import com.google.common.primitives.Ints

fun CtfTrace.getTracerName(): String? {
    /* Remove the "" at the start and end of the string */
    return environment["tracer_name"]?.replace("^\"|\"$".toRegex(), "")
}

fun CtfTrace.getTracerMajorVersion(): Int? {
    val str = environment["tracer_major"] ?: return null
    return Ints.tryParse(str)
}

fun CtfTrace.getTracerMinorVersion(): Int? {
    val str = environment["tracer_minor"] ?: return null
    return Ints.tryParse(str)
}
