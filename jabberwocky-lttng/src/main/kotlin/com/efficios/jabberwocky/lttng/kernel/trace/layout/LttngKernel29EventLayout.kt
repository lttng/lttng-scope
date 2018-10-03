/*
 * Copyright (C) 2018 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 * Copyright (C) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.efficios.jabberwocky.lttng.kernel.trace.layout

/**
 * This file defines all the known event and field names for LTTng kernel
 * traces, for versions of lttng-modules 2.9 and above.
 */
open class LttngKernel29EventLayout protected constructor() : LttngKernel28EventLayout() {

    companion object {
        val instance = LttngKernel29EventLayout()

        private const val FIELD_VARIANT_SELECTED = "Any"
    }

    override val fieldPathTcpSeq = listOf("network_header", FIELD_VARIANT_SELECTED, "transport_header", "tcp", "seq")
    override val fieldPathTcpAckSeq = listOf("network_header", FIELD_VARIANT_SELECTED, "transport_header", "tcp", "ack_seq")
    override val fieldPathTcpFlags = listOf("network_header", FIELD_VARIANT_SELECTED, "transport_header", "tcp", "flags")

}