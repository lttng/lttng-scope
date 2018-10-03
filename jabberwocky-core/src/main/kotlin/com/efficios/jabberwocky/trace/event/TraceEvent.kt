/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.efficios.jabberwocky.trace.event

import com.efficios.jabberwocky.trace.Trace

interface TraceEvent {

    val trace: Trace<TraceEvent>

    val timestamp: Long

    val cpu: Int

    val eventName: String

    val fields: Map<String, FieldValue>

    val attributes: Map<String, String>

}
