/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Matthew Khouzam - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.ctf.core.tests.trace;

import org.eclipse.tracecompass.ctf.core.CTFException;
import org.eclipse.tracecompass.ctf.core.event.IEventDefinition;
import org.eclipse.tracecompass.ctf.core.tests.shared.LttngTraceGenerator;
import org.eclipse.tracecompass.ctf.core.trace.CTFTrace;
import org.eclipse.tracecompass.ctf.core.trace.CTFTraceReader;
import org.eclipse.tracecompass.ctf.core.trace.Metadata;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for streaming support
 *
 * @author Matthew Khouzam
 *
 */
class CTFTraceGrowingTest {

    private static String fPathName;

    private final CTFTrace fixture = new CTFTrace();

    @BeforeAll
    static void setupClass() {
        fPathName = LttngTraceGenerator.generateTrace();
    }

    @AfterAll
    static void teardownClass() {
        LttngTraceGenerator.cleanupTrace();
    }

    /**
     * Init
     *
     * @throws IOException
     *             an IO error
     * @throws FileNotFoundException
     *             file's not there
     * @throws CTFException
     *             error in metadata
     */
    @BeforeEach
    void init() throws FileNotFoundException, IOException, CTFException {
        Metadata md = new Metadata(fixture);
        File metadata = new File(fPathName + "/" + "metadata");

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(metadata)))) {

            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            String result = sb.toString();
            md.parseText(result);
        }
    }

    /**
     * Empty trace test
     *
     * @throws CTFException
     *             will not happen
     */
    @Test
    void testEmptyStream() throws CTFException {
        try (CTFTraceReader reader = new CTFTraceReader(fixture);) {
            assertNull(reader.getCurrentEventDef());
        }
    }

    /**
     * Add a stream
     *
     * @throws CTFException
     *             should not happen
     */
    @Test
    void testAddStream() throws CTFException {
        File stream = new File(fPathName + "/" + "channel1");
        try (CTFTraceReader reader = new CTFTraceReader(fixture);) {
            fixture.addStreamFile(stream);
            reader.update();
            assertTrue(reader.advance());
            assertNotNull(reader.getCurrentEventDef());
        }
    }

    /**
     * Adds two a stream
     *
     * @throws CTFException
     *             should not happen
     */
    @Test
    void testAddTwoStreams1() throws CTFException {
        File stream = new File(fPathName + "/" + "channel1");
        try (CTFTraceReader reader = new CTFTraceReader(fixture);) {
            fixture.addStreamFile(stream);
            stream = new File(fPathName + "/" + "channel2");
            fixture.addStreamFile(stream);
            reader.update();
            assertTrue(reader.advance());
            IEventDefinition currentEventDef = reader.getCurrentEventDef();
            assertNotNull(reader.getCurrentEventDef());
            assertEquals(16518L, currentEventDef.getTimestamp());
        }
    }

    /**
     * Adds two a stream
     *
     * @throws CTFException
     *             should not happen
     */
    @Test
    void testAddTwoStreams2() throws CTFException {
        File stream = new File(fPathName + "/" + "channel1");
        try (CTFTraceReader reader = new CTFTraceReader(fixture);) {
            fixture.addStreamFile(stream);
            stream = new File(fPathName + "/" + "channel2");
            reader.update();
            assertTrue(reader.advance());
            fixture.addStreamFile(stream);
            reader.update();
            assertTrue(reader.advance());
            IEventDefinition currentEventDef = reader.getCurrentEventDef();
            assertNotNull(currentEventDef);
            assertEquals(223007L, currentEventDef.getTimestamp());
        }
    }

    /**
     * Tests that update does not change the position
     *
     * @throws CTFException
     *             should not happen
     */
    @Test
    void testAddTwoStreams3() throws CTFException {
        File stream = new File(fPathName + "/" + "channel1");
        try (CTFTraceReader reader = new CTFTraceReader(fixture);) {
            fixture.addStreamFile(stream);
            stream = new File(fPathName + "/" + "channel2");
            reader.update();
            reader.update();
            reader.update();
            assertTrue(reader.advance());
            fixture.addStreamFile(stream);
            reader.update();
            reader.update();
            reader.update();
            reader.update();
            assertTrue(reader.advance());
            IEventDefinition currentEventDef = reader.getCurrentEventDef();
            assertNotNull(currentEventDef);
            assertEquals(223007L, currentEventDef.getTimestamp());
        }
    }

    /**
     * Test adding a bad stream
     *
     * @throws CTFException
     *             should happen
     */
    @Test
    void testAddStreamFail() throws CTFException {
        assertThrows(CTFException.class, () -> {
            File stream = new File(fPathName + "/" + "metadata");
            try (CTFTraceReader reader = new CTFTraceReader(fixture);) {
                fixture.addStreamFile(stream);
                assertNull(reader.getCurrentEventDef());
            }
        });
    }

}
