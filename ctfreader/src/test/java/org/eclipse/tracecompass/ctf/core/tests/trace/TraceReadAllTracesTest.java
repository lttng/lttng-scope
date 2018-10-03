/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Matthew Khouzam - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.ctf.core.tests.trace;

import org.eclipse.tracecompass.ctf.core.CTFException;
import org.eclipse.tracecompass.ctf.core.CTFStrings;
import org.eclipse.tracecompass.ctf.core.event.IEventDefinition;
import org.eclipse.tracecompass.ctf.core.event.types.IntegerDefinition;
import org.eclipse.tracecompass.ctf.core.tests.shared.CtfTestTraceExtractor;
import org.eclipse.tracecompass.ctf.core.trace.CTFTraceReader;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.lttng.scope.ttt.ctf.CtfTestTrace;

import java.time.Duration;
import java.util.Arrays;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Read all the traces and verify some metrics. Nominally the event count and
 * the duration of the trace (not the time to parse it).
 *
 * @author Matthew Khouzam
 */
class TraceReadAllTracesTest {

    /**
     * Get the list of traces
     *
     * @return the list of traces
     */
    private static Iterable<Arguments> tracePaths() {
        return Arrays.stream(CtfTestTrace.values())
                .map(value -> Arguments.of(value.name(), value))
                .collect(Collectors.toList());
    }

    /**
     * Reads all the traces
     */
    @ParameterizedTest
    @MethodSource("tracePaths")
    void readTraces(String name, CtfTestTrace traceEnum) {
        assertTimeout(Duration.ofMinutes(1), () -> {
            if (traceEnum.getNbEvents() == -1) {
                fail("Trace did not specify events count");
            }
            try (CtfTestTraceExtractor testTraceWrapper = CtfTestTraceExtractor.extractTestTrace(traceEnum);
                 CTFTraceReader reader = new CTFTraceReader(testTraceWrapper.getTrace());) {
                IEventDefinition currentEventDef = reader.getCurrentEventDef();
                double start = currentEventDef.getTimestamp();
                long count = 0;
                double end = start;
                while (reader.hasMoreEvents()) {
                    reader.advance();
                    count++;
                    currentEventDef = reader.getCurrentEventDef();
                    if (currentEventDef != null) {
                        end = currentEventDef.getTimestamp();
                        if (currentEventDef.getDeclaration().getName().equals(CTFStrings.LOST_EVENT_NAME)) {
                            count += ((IntegerDefinition) currentEventDef.getFields()
                                    .getDefinition(CTFStrings.LOST_EVENTS_FIELD)).getValue() - 1;
                        }
                    }
                }
                assertEquals(traceEnum.getNbEvents(), count, "Event count");
                assertEquals(traceEnum.getDuration(), (end - start) / 1000000000.0, 1.0, "Trace duration");
            } catch (CTFException e) {
                fail(traceEnum.name() + " " + e.getMessage());
            }
        });
    }
}
