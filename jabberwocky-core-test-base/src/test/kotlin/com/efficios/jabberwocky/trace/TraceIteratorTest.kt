/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.efficios.jabberwocky.trace

class TraceIteratorTest : TraceIteratorTestBase() {

    override val trace = TraceStubs.TraceStub3()

    override val event1 = trace.events[0]
    override val event2 = trace.events[1]
    override val event3 = trace.events[2]
    override val timestampBetween1and2 = 101L

    override val lastEvent = trace.events.last()
    override val timestampAfterEnd = 210L

    override val middleEvent = trace.events[25]
    override val middleEventPosition = 25

}
