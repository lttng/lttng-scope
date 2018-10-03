/*******************************************************************************
 * Copyright (c) 2013, 2014 Ericsson
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Khouzam - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.ctf.core.tests.trace;

import org.eclipse.tracecompass.ctf.core.CTFException;
import org.eclipse.tracecompass.ctf.core.event.IEventDefinition;
import org.eclipse.tracecompass.ctf.core.event.types.*;
import org.eclipse.tracecompass.ctf.core.tests.shared.CtfTestTraceExtractor;
import org.eclipse.tracecompass.ctf.core.trace.*;
import org.eclipse.tracecompass.internal.ctf.core.event.EventDeclaration;
import org.eclipse.tracecompass.internal.ctf.core.event.EventDefinition;
import org.eclipse.tracecompass.internal.ctf.core.trace.CTFStream;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.lttng.scope.ttt.ctf.CtfTestTrace;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The class <code>StreamInputReaderTest</code> contains tests for the class
 * <code>{@link CTFStreamInputReader}</code>.
 *
 * @author Matthew Khouzam
 */
@SuppressWarnings("javadoc")
class CTFStreamInputReaderTest {

    private static final CtfTestTrace testTrace = CtfTestTrace.KERNEL;
    private static CtfTestTraceExtractor testTraceWrapper;

    private CTFStreamInputReader fixture;

    @BeforeAll
    static void setupClass() {
        testTraceWrapper = CtfTestTraceExtractor.extractTestTrace(testTrace);
    }

    @AfterAll
    static void teardownClass() {
        testTraceWrapper.close();
    }

    /**
     * Perform pre-test initialization.
     *
     * @throws CTFException
     */
    @BeforeEach
    void setUp() throws CTFException {
        fixture = getStreamInputReader();
        fixture.setName(1);
        fixture.setCurrentEvent(new EventDefinition(new EventDeclaration(),
                fixture.getCPU(), 0, null, null, null,
                new StructDefinition(
                        new StructDeclaration(0),
                        null,
                        "packet",
                        new Definition[] { new StringDefinition(StringDeclaration.getStringDeclaration(Encoding.UTF8), null, "field", "test") }),
                null, fixture.getCurrentPacketReader().getCurrentPacket()));
    }

    private static CTFStreamInputReader getStreamInputReader() throws CTFException {
        ICTFStream s = testTraceWrapper.getTrace().getStream((long) 0);
        Set<CTFStreamInput> streamInput = s.getStreamInputs();
        CTFStreamInputReader retVal = null;
        for (CTFStreamInput si : streamInput) {
            /*
             * For the tests, we'll use the stream input corresponding to the
             * CPU 0
             */
            if (si.getFilename().endsWith("0_0")) {
                retVal = new CTFStreamInputReader(si);
                break;
            }
        }
        return retVal;
    }

    /**
     * Run the StreamInputReader(StreamInput) constructor test, with a valid
     * trace.
     */
    @Test
    void testStreamInputReader_valid() {
        assertNotNull(fixture);
    }

    /**
     * Run the StreamInputReader(StreamInput) constructor test, with an invalid
     * trace.
     *
     * @throws CTFException
     * @throws IOException
     */
    @Test
    void testStreamInputReader_invalid() throws CTFException, IOException {
        assertThrows(CTFException.class, () -> {
            CTFStreamInput streamInput = new CTFStreamInput(new CTFStream(new CTFTrace("")), new File(""));
            try (CTFStreamInputReader result = new CTFStreamInputReader(streamInput)) {
                assertNotNull(result);
            }
        });
    }

    /**
     * Run the int getCPU() method test.
     */
    @Test
    void testGetCPU() {
        int result = fixture.getCPU();
        assertEquals(0, result);
    }

    /**
     * Run the EventDefinition getCurrentEvent() method test.
     */
    @Test
    void testGetCurrentEvent() {
        assertNotNull(fixture.getCurrentEvent());
    }

    /**
     * Run the StructDefinition getCurrentPacketContext() method test.
     */
    @Test
    void testGetCurrentPacketContext() {
        IEventDefinition currentEvent = fixture.getCurrentEvent();
        assertNotNull(currentEvent);
        ICompositeDefinition result = currentEvent.getPacketContext();
        assertNotNull(result);
    }

    /**
     * Run the int getName() method test.
     */
    @Test
    void testGetName() {
        int result = fixture.getName();
        assertEquals(1, result);
    }

    /**
     * Run the void goToLastEvent() method test.
     *
     * @throws CTFException
     *             error
     */
    @Test
    void testGoToLastEvent1() throws CTFException {
        final long endTimestamp = goToEnd();
        final long endTime = 4287422460315L;
        assertEquals(endTime, endTimestamp);
    }

    /**
     * Run the void goToLastEvent() method test.
     *
     * @throws CTFException
     *             error
     */
    @Test
    void testGoToLastEvent2() throws CTFException {
        long timestamp = -1;
        while (fixture.readNextEvent().equals(CTFResponse.OK)) {
            IEventDefinition currentEvent = fixture.getCurrentEvent();
            assertNotNull(currentEvent);
            timestamp = currentEvent.getTimestamp();
        }
        long endTimestamp = goToEnd();
        assertEquals(0, timestamp - endTimestamp);
    }

    private long goToEnd() throws CTFException {
        fixture.goToLastEvent();
        IEventDefinition currentEvent = fixture.getCurrentEvent();
        assertNotNull(currentEvent);
        return currentEvent.getTimestamp();
    }

    /**
     * Run the boolean readNextEvent() method test.
     *
     * @throws CTFException
     *             error
     */
    @Test
    void testReadNextEvent() throws CTFException {
        assertEquals(CTFResponse.OK, fixture.readNextEvent());
    }

    /**
     * Run the void seek(long) method test. Seek by direct timestamp
     *
     * @throws CTFException
     *             error
     */
    @Test
    void testSeek_timestamp() throws CTFException {
        long timestamp = 1L;
        fixture.seek(timestamp);
    }

    /**
     * Run the seek test. Seek by passing an EventDefinition to which we've
     * given the timestamp we want.
     *
     * @throws CTFException
     *             error
     * @throws IOException
     *             file not there
     */
    @Test
    void testSeek_eventDefinition() throws CTFException, IOException {
        try (CTFStreamInputReader streamInputReader = getStreamInputReader()) {
            EventDefinition eventDefinition = new EventDefinition(
                    new EventDeclaration(), streamInputReader.getCPU(), 1L, null, null, null, null, null, streamInputReader.getCurrentPacketReader().getCurrentPacket());
            fixture.setCurrentEvent(eventDefinition);
        }
    }
}
