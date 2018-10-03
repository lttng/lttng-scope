/*******************************************************************************
 * Copyright (c) 2013, 2014 Ericsson
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Khouzam - Initial API and implementation
 *     Marc-Andre Laperle - Test in traces directory recursively
 *     Simon Delisle - Add test for getCallsite(eventName, ip)
 *******************************************************************************/

package org.eclipse.tracecompass.ctf.core.tests.trace;

import com.google.common.collect.ImmutableMap;
import org.eclipse.tracecompass.ctf.core.CTFException;
import org.eclipse.tracecompass.ctf.core.event.CTFClock;
import org.eclipse.tracecompass.ctf.core.event.metadata.ParseException;
import org.eclipse.tracecompass.ctf.core.event.types.IDefinition;
import org.eclipse.tracecompass.ctf.core.event.types.StructDeclaration;
import org.eclipse.tracecompass.ctf.core.tests.shared.CtfTestTraceExtractor;
import org.eclipse.tracecompass.ctf.core.trace.CTFTrace;
import org.eclipse.tracecompass.ctf.core.trace.ICTFStream;
import org.eclipse.tracecompass.internal.ctf.core.trace.CTFStream;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.lttng.scope.ttt.ctf.CtfTestTrace;

import java.io.File;
import java.nio.ByteOrder;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The class <code>CTFTraceTest</code> contains tests for the class
 * <code>{@link CTFTrace}</code>.
 *
 * @author ematkho
 */
class CTFTraceTest {

    private static final CtfTestTrace testTrace = CtfTestTrace.KERNEL;
    private static CtfTestTraceExtractor testTraceWrapper;

    private CTFTrace fixture;

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
     */
    @BeforeEach
    void setUp() {
        fixture = testTraceWrapper.getTrace();

        fixture.setMinor(1L);
        fixture.setUUID(UUID.randomUUID());
        fixture.setPacketHeader(new StructDeclaration(1L));
        fixture.setMajor(1L);
        fixture.setByteOrder(ByteOrder.BIG_ENDIAN);
    }

    /**
     * Run the CTFTrace(File) constructor test with a known existing trace.
     */
    @Test
    void testOpen_existing() {
        CTFTrace result = testTraceWrapper.getTrace();
        assertNotNull(result.getUUID());
    }

    /**
     * Run the CTFTrace(File) constructor test with an invalid path.
     *
     * @throws CTFException
     *             is expected
     */
    @Test
    void testOpen_invalid() throws CTFException {
        File path = new File("");
        assertThrows(CTFException.class, () -> new CTFTrace(path));
    }

    /**
     * Run the boolean UUIDIsSet() method test.
     */
    @Test
    void testUUIDIsSet() {
        boolean result = fixture.uuidIsSet();
        assertTrue(result);
    }

    /**
     * Run the void addStream(Stream) method test.
     */
    @Test
    void testAddStream() {
        // test number of streams
        int nbStreams = fixture.nbStreams();
        assertEquals(1, nbStreams);

        // Add a stream
        try {
            CTFStream stream = new CTFStream(testTraceWrapper.getTrace());
            stream.setId(1234);
            fixture.addStream(stream);
        } catch (ParseException e) {
            fail();
        }

        // test number of streams
        nbStreams = fixture.nbStreams();
        assertEquals(2, nbStreams);
    }

    /**
     * Run the boolean byteOrderIsSet() method test.
     */
    @Test
    void testByteOrderIsSet() {
        boolean result = fixture.byteOrderIsSet();
        assertTrue(result);
    }

    /**
     * Run the ByteOrder getByteOrder() method test.
     */
    @Test
    void testGetByteOrder_1() {
        ByteOrder result = fixture.getByteOrder();
        assertNotNull(result);
    }

    /**
     * Run the long getMajor() method test.
     */
    @Test
    void testGetMajor() {
        long result = fixture.getMajor();
        assertEquals(1L, result);
    }

    /**
     * Run the long getMinor() method test.
     */
    @Test
    void testGetMinor() {
        long result = fixture.getMinor();
        assertEquals(1L, result);
    }

    /**
     * Run the StructDeclaration getPacketHeader() method test.
     */
    @Test
    void testGetPacketHeader() {
        StructDeclaration result = fixture.getPacketHeader();
        assertNotNull(result);
    }

    /**
     * Run the String getPath() method test.
     */
    @Test
    void testGetPath() {
        String result = fixture.getPath();
        assertNotNull(result);
    }

    /**
     * Run the Stream getStream(Long) method test.
     */
    @Test
    void testGetStream() {
        Long id = new Long(0L);
        ICTFStream result = fixture.getStream(id);
        assertNotNull(result);
    }

    /**
     * Run the File getTraceDirectory() method test.
     */
    @Test
    void testGetTraceDirectory() {
        File result = fixture.getTraceDirectory();
        assertNotNull(result);
    }

    /**
     * Run the UUID getUUID() method test.
     */
    @Test
    void testGetUUID() {
        UUID result = fixture.getUUID();
        assertNotNull(result);
    }

    /**
     * Run the Definition lookupDefinition(String) method test.
     */
    @Test
    void testLookupDefinition() {
        String lookupPath = "trace.packet.header";
        IDefinition result = fixture.lookupDefinition(lookupPath);
        assertNotNull(result);
    }

    /**
     * Run the boolean majorIsSet() method test.
     */
    @Test
    void testMajorIsSet() {
        boolean result = fixture.majorIsSet();
        assertTrue(result);
    }

    /**
     * Run the boolean minorIsSet() method test.
     */
    @Test
    void testMinorIsSet() {
        boolean result = fixture.minorIsSet();
        assertTrue(result);
    }

    /**
     * Run the boolean packetHeaderIsSet() method test with a valid header set.
     */
    @Test
    void testPacketHeaderIsSet_valid() {
        boolean result = fixture.packetHeaderIsSet();
        assertTrue(result);
    }

    /**
     * Run the boolean packetHeaderIsSet() method test, without having a valid
     * header set.
     */
    @Test
    void testPacketHeaderIsSet_invalid() {
        CTFTrace fixture2 = testTraceWrapper.getTrace();
        fixture2.setMinor(1L);
        fixture2.setUUID(UUID.randomUUID());
        /*
         * it's null here!
         */
        fixture2.setPacketHeader((StructDeclaration) null);
        fixture2.setMajor(1L);
        fixture2.setByteOrder(ByteOrder.BIG_ENDIAN);

        boolean result = fixture2.packetHeaderIsSet();
        assertFalse(result);
    }

    /**
     * Run the void setByteOrder(ByteOrder) method test.
     */
    @Test
    void testSetByteOrder() {
        ByteOrder byteOrder = ByteOrder.BIG_ENDIAN;
        fixture.setByteOrder(byteOrder);
    }

    /**
     * Run the void setMajor(long) method test.
     */
    @Test
    void testSetMajor() {
        long major = 1L;
        fixture.setMajor(major);
    }

    /**
     * Run the void setMinor(long) method test.
     */
    @Test
    void testSetMinor() {
        long minor = 1L;
        fixture.setMinor(minor);
    }

    /**
     * Run the void setPacketHeader(StructDeclaration) method test.
     */
    @Test
    void testSetPacketHeader() {
        StructDeclaration packetHeader = new StructDeclaration(1L);
        fixture.setPacketHeader(packetHeader);
    }

    /**
     * Run the void setUUID(UUID) method test.
     */
    @Test
    void testSetUUID() {
        UUID uuid = UUID.randomUUID();
        fixture.setUUID(uuid);
    }

    /**
     * Run the CTFClock getClock/setClock method test.
     */
    @Test
    void testGetSetClock_1() {
        String name = "clockyClock";
        fixture.addClock(name, new CTFClock());
        CTFClock result = fixture.getClock(name);

        assertNotNull(result);
    }

    /**
     * Run the CTFClock getClock/setClock method test.
     */
    @Test
    void testGetSetClock_2() {
        String name = "";
        CTFClock ctfClock = new CTFClock();
        ctfClock.addAttribute("name", "Bob");
        ctfClock.addAttribute("pi", new Double(java.lang.Math.PI));
        fixture.addClock(name, ctfClock);
        CTFClock result = fixture.getClock(name);

        assertNotNull(result);
        assertTrue((Double) ctfClock.getProperty("pi") > 3.0);
        assertEquals("Bob", ctfClock.getName());
    }

    /**
     * Run the String lookupEnvironment(String) method test.
     */
    @Test
    void testLookupEnvironment_1() {
        String key = "";
        String result = fixture.getEnvironment().get(key);
        assertNull(result);
    }

    /**
     * Run the String lookupEnvironment(String) method test.
     */
    @Test
    void testLookupEnvironment_2() {
        String key = "otherTest";
        String result = fixture.getEnvironment().get(key);
        assertNull(result);
    }

    /**
     * Run the String lookupEnvironment(String) method test.
     */
    @Test
    void testLookupEnvironment_3() {
        String key = "test";
        fixture.setEnvironment(ImmutableMap.<String, String> of(key, key));
        String result = fixture.getEnvironment().get(key);
        assertNotNull(result);
        assertEquals(key, result);
    }

    /**
     * Run the String lookupEnvironment(String) method test.
     */
    @Test
    void testLookupEnvironment_4() {
        String key = "test";
        fixture.setEnvironment(ImmutableMap.<String, String> of(key, "bozo"));
        String result = fixture.getEnvironment().get(key);
        assertNotNull(result);
    }
}
