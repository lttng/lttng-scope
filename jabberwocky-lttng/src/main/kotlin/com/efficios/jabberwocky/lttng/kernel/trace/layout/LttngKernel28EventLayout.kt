/*
 * Copyright (C) 2018 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 * Copyright (C) 2015 Ericsson
 * Copyright (C) 2015 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.efficios.jabberwocky.lttng.kernel.trace.layout

/**
 * This file defines all the known event and field names for LTTng kernel
 * traces, for versions of lttng-modules 2.8 and above.
 */
open class LttngKernel28EventLayout protected constructor() : LttngKernel27EventLayout() {

    companion object {
        val instance = LttngKernel28EventLayout()
    }

    // TODO Technically, this event was added with LTTng 2.8, it should not be part of the super-classes
    override val eventSchedProcessWaking = "sched_waking"
}