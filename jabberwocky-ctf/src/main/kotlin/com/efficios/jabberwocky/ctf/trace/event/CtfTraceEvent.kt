/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.efficios.jabberwocky.ctf.trace.event

import com.efficios.jabberwocky.ctf.trace.CtfTrace
import com.efficios.jabberwocky.trace.event.BaseTraceEvent
import com.efficios.jabberwocky.trace.event.FieldValue

open class CtfTraceEvent(override val trace: CtfTrace,
                         timestamp: Long,
                         cpu: Int,
                         eventName: String,
                         eventFields: Map<String, FieldValue>,
                         attributes: Map<String, String>? = null) : BaseTraceEvent(trace, timestamp, cpu, eventName, eventFields, attributes)
