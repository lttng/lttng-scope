/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.ui.timeline.widgets.timegraph;

import java.util.Collections;

import com.efficios.jabberwocky.trace.Trace;
import com.efficios.jabberwocky.trace.TraceIterator;
import com.efficios.jabberwocky.trace.event.BaseTraceEvent;
import com.efficios.jabberwocky.trace.event.TraceEvent;
import com.google.common.collect.Iterators;
import com.google.common.collect.UnmodifiableIterator;

class StubTrace extends Trace<TraceEvent> {

    public static final long FULL_TRACE_START_TIME = 100000L;
    public static final long FULL_TRACE_END_TIME = 200000L;

    private class StubTraceIterator implements TraceIterator<TraceEvent> {

        private final UnmodifiableIterator<TraceEvent> events = Iterators.forArray(
                new BaseTraceEvent(StubTrace.this, FULL_TRACE_START_TIME, 0, "StubEvent", Collections.emptyMap(), null),
                new BaseTraceEvent(StubTrace.this, FULL_TRACE_END_TIME, 0, "StubEvent", Collections.emptyMap(), null));

        @Override
        public boolean hasNext() {
            return events.hasNext();
        }

        @Override
        public TraceEvent next() {
            return events.next();
        }

        @Override
        public void close() {
        }
    }

    @Override
    public TraceIterator<TraceEvent> iterator() {
        return new StubTraceIterator();
    }

}
