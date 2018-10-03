/*
 * Copyright (C) 2018 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 * Copyright (C) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.efficios.jabberwocky.lttng.kernel.trace.layout

/**
 * This file defines all the known event and field names for LTTng kernel
 * traces, for versions of lttng-modules 2.6 and above.
 */
open class LttngKernel26EventLayout protected constructor() : LttngKernel20EventLayout() {

    companion object {
        val instance = LttngKernel26EventLayout()
    }

    /* New event names in this version */
    override val eventSyscallEntryPrefix = "syscall_entry_"
    override val eventCompatSyscallEntryPrefix = "compat_syscall_entry_"
    override val eventSyscallExitPrefix = "syscall_exit_"
    override val eventCompatSyscallExitPrefix = "compat_syscall_exit_"

}
