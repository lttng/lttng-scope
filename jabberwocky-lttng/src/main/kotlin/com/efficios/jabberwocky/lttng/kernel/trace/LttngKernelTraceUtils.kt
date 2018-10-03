/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.efficios.jabberwocky.lttng.kernel.trace

import com.efficios.jabberwocky.ctf.trace.CtfTrace
import com.efficios.jabberwocky.ctf.trace.getTracerMajorVersion
import com.efficios.jabberwocky.ctf.trace.getTracerMinorVersion
import com.efficios.jabberwocky.ctf.trace.getTracerName
import com.efficios.jabberwocky.lttng.kernel.trace.layout.*

fun CtfTrace.isKernelTrace() =
        /* Check if the CTF metadata advertises "domain = kernel" */
        environment["domain"] == "\"kernel\""

/**
 * Retrieve the kernel event layout applicable to this trace,
 * or null if this does not look like a kernel trace.
 */
fun CtfTrace.getKernelEventLayout(): LttngKernelEventLayout? {
    if (!isKernelTrace()) return null

    val defaultLayout = LttngKernel20EventLayout.instance
    val tracerName = getTracerName()
    val majorVersion = getTracerMajorVersion() ?: -1
    val minorVersion = getTracerMinorVersion() ?: -1

    return when (tracerName) {
        "perf" -> PerfEventLayout.instance
        "lttng-modules" -> when {
            majorVersion >= 2 -> when {
                minorVersion >= 9 -> LttngKernel29EventLayout.instance
                minorVersion >= 8 -> LttngKernel28EventLayout.instance
                minorVersion >= 7 -> LttngKernel27EventLayout.instance
                minorVersion >= 6 -> LttngKernel26EventLayout.instance
                else -> LttngKernel20EventLayout.instance
            }
            else -> defaultLayout
        }
        else -> defaultLayout
    }
}