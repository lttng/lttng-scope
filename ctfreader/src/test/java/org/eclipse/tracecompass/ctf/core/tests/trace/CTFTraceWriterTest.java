/*******************************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.ctf.core.tests.trace;

import org.eclipse.tracecompass.ctf.core.CTFException;
import org.eclipse.tracecompass.ctf.core.event.IEventDefinition;
import org.eclipse.tracecompass.ctf.core.tests.shared.CtfTestTraceExtractor;
import org.eclipse.tracecompass.ctf.core.trace.CTFTrace;
import org.eclipse.tracecompass.ctf.core.trace.CTFTraceReader;
import org.eclipse.tracecompass.ctf.core.trace.CTFTraceWriter;
import org.eclipse.tracecompass.internal.ctf.core.trace.Utils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.lttng.scope.ttt.ctf.CtfTestTrace;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;

import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.*;

/**
 * CTFTraceWriter test cases
 *
 * @author Bernd Hufmann
 */
class CTFTraceWriterTest {

    private static Path fTempDir;

        // Trace details
        private static final long CLOCK_OFFSET = 1332166405241713987L;
        private static final int TOTAL_NB_EVENTS = 695319;
        private static final long LAST_EVENT_TIME = 1332170692664579801L;

        // Stream 0 values
        private static final long STREAM0_FIRST_PACKET_TIME = CLOCK_OFFSET + 4277170993912L;
        private static final long STREAM0_FIRST_EVENT_TIME = 1332170682440316151L;
        private static final long STREAM0_LAST_EVENT_TIME = 1332170682702066969L;
        private static final int STREAM0_FIRST_PACKET_NB_EVENTS = 14219;

        // Stream 1 values
        private static final long STREAM1_FIRST_PACKET_TIME = CLOCK_OFFSET + 4277171555436L;
        private static final int STREAM1_FIRST_PACKET_NB_EVENTS = 8213;
        private static final long STREAM1_FIRST_EVENT_TIME = 1332170682440133097L;
        private static final long STREAM1_FIFTH_PACKET_TIME = CLOCK_OFFSET + 4277970712221L;
        private static final long STREAM1_TENTH_PACKET_TIME = CLOCK_OFFSET + 4279440048309L;
        private static final long STREAM1_FIFTH_PACKET_FIRST_EVENT_TIME = 1332170682702069762L;
        private static final long STREAM1_TENTH_PACKET_LAST_EVENT_TIME = 1332170685256508077L;

        // Miscellaneous
        private static final int NB_EVENTS_SEVERAL_PACKETS = 167585;

    /**
     * Gets a list of test case parameters.
     *
     * @return The list of test parameters
     */
    private static Iterable<Arguments> testParams() {
        final List<Arguments> params = new LinkedList<>();

        params.add(Arguments.of("WHOLE_TRACE",
                            0,
                            Long.MAX_VALUE,
                            TOTAL_NB_EVENTS,
                            STREAM1_FIRST_EVENT_TIME,
                            LAST_EVENT_TIME));

        params.add(Arguments.of("NO_EVENTS_USING_INVERTED_TIME",
                            Long.MAX_VALUE, Long.MIN_VALUE,
                            0,
                            -1,
                            -1));

        params.add(Arguments.of("STREAM0_FIRST_PACKET_TIME",
                            STREAM0_FIRST_PACKET_TIME,
                            STREAM0_FIRST_PACKET_TIME,
                            STREAM0_FIRST_PACKET_NB_EVENTS,
                            STREAM0_FIRST_EVENT_TIME,
                            STREAM0_LAST_EVENT_TIME));

        params.add(Arguments.of("BOTH_STREAMS_FIRST_PACKET_ONLY",
                            STREAM0_FIRST_PACKET_TIME,
                            STREAM1_FIRST_PACKET_TIME,
                            STREAM0_FIRST_PACKET_NB_EVENTS + STREAM1_FIRST_PACKET_NB_EVENTS,
                            STREAM1_FIRST_EVENT_TIME,
                            STREAM0_LAST_EVENT_TIME));

        params.add(Arguments.of("BOTH_STREAMS_SEVERAL_PACKETS",
                STREAM1_FIFTH_PACKET_TIME,
                STREAM1_TENTH_PACKET_TIME,
                NB_EVENTS_SEVERAL_PACKETS,
                STREAM1_FIFTH_PACKET_FIRST_EVENT_TIME,
                STREAM1_TENTH_PACKET_LAST_EVENT_TIME));

        return params;
    }

    @BeforeAll
    static void beforeClass() {
        try {
            fTempDir = Files.createTempDirectory("testcases"); //$NON-NLS-1$
            if (!Files.exists(fTempDir)) {
                Files.createDirectories(fTempDir);
            }
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

    @AfterAll
    static void afterClass() {
        if (fTempDir != null) {
            CtfTestTraceExtractor.deleteDirectoryRecursively(fTempDir);
        }
    }

    /**
     * Test various time ranges
     */
    @ParameterizedTest
    @MethodSource("testParams")
    void testKernelTrace(String name, long startTime, long endTime, int nbEvents, long firstEventTime, long lastEventTime) {
        try (CtfTestTraceExtractor testTrace = CtfTestTraceExtractor.extractTestTrace(CtfTestTrace.KERNEL)) {
            CTFTrace trace = testTrace.getTrace();
            CTFTraceWriter ctfWriter = new CTFTraceWriter(requireNonNull(trace));
            String traceName = createTraceName(name);
            ctfWriter.copyPackets(startTime, endTime, traceName);

            File metadata = new File(traceName + Utils.SEPARATOR + "metadata");
            assertTrue(metadata.exists(), "metadata");

            CTFTrace outTrace = new CTFTrace(traceName);
            int count = 0;
            Long start = null;
            long end = 0;
            try (CTFTraceReader reader = new CTFTraceReader(outTrace)) {
                while (reader.hasMoreEvents()) {
                    count++;
                    IEventDefinition def = reader.getCurrentEventDef();
                    end = def.getTimestamp();
                    if (start == null) {
                        start = outTrace.getClock().getClockOffset() + reader.getStartTime();
                    }
                    reader.advance();
                }
                end = outTrace.getClock().getClockOffset() + end;
            }

            if (firstEventTime >= 0) {
                assertEquals(Long.valueOf(firstEventTime), start, "first event time");
            }
            if (lastEventTime >= 0) {
                assertEquals(lastEventTime, end, "last event time");
            }
            assertEquals(nbEvents, count, toString());

            if (nbEvents == 0) {
                assertFalse(getChannelFile(traceName, 0).exists(), "channel0");
                assertFalse(getChannelFile(traceName, 1).exists(), "channel1");
            }

        } catch (CTFException e) {
            fail(e.getMessage());
        }
    }

    private static File getChannelFile(String path, int id) {
        File channel = new File(path + Utils.SEPARATOR + "channel_" + String.valueOf(id));
        return channel;
    }

    private static String createTraceName(String testCase) {
        return fTempDir.toFile().getAbsolutePath() + File.separator + testCase.toString();
    }

}
