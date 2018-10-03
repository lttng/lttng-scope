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
import org.eclipse.tracecompass.ctf.core.event.types.IDefinition;
import org.eclipse.tracecompass.ctf.core.tests.shared.CtfTestTraceExtractor;
import org.eclipse.tracecompass.ctf.core.trace.CTFStreamInput;
import org.eclipse.tracecompass.ctf.core.trace.ICTFStream;
import org.eclipse.tracecompass.internal.ctf.core.trace.CTFStream;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.lttng.scope.ttt.ctf.CtfTestTrace;

import java.io.File;
import java.io.FilenameFilter;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The class <code>StreamInputTest</code> contains tests for the class
 * <code>{@link CTFStreamInput}</code>.
 *
 * @author ematkho
 * @version $Revision: 1.0 $
 */
@SuppressWarnings("javadoc")
class CTFStreamInputTest {

    private static final CtfTestTrace testTrace = CtfTestTrace.KERNEL;
    private static CtfTestTraceExtractor testTraceWrapper;

    private CTFStreamInput fixture;

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
        fixture = new CTFStreamInput(new CTFStream(testTraceWrapper.getTrace()), createFile());
        fixture.setTimestampEnd(1L);
    }

    private static @NotNull File createFile() throws CTFException {
        File path = new File(testTraceWrapper.getTrace().getPath());
        final File[] listFiles = path.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                if (name.contains("hann")) {
                    return true;
                }
                return false;
            }
        });
        assertNotNull(listFiles);
        final File returnFile = listFiles[0];
        assertNotNull(returnFile);
        return returnFile;
    }

    /**
     * Run the StreamInput(Stream,FileChannel,File) constructor test.
     */
    @Test
    void testStreamInput() {
        assertNotNull(fixture);
    }

    /**
     * Run the String getFilename() method test.
     */
    @Test
    void testGetFilename() {
        String result = fixture.getFilename();
        assertNotNull(result);
    }

    /**
     * Run the String getPath() method test.
     */
    @Test
    void testGetPath() {
        String result = fixture.getScopePath().getPath();
        assertNotNull(result);
    }

    /**
     * Run the Stream getStream() method test.
     */
    @Test
    void testGetStream() {
        ICTFStream result = fixture.getStream();
        assertNotNull(result);
    }

    /**
     * Run the long getTimestampEnd() method test.
     */
    @Test
    void testGetTimestampEnd() {
        long result = fixture.getTimestampEnd();
        assertTrue(0L < result);
    }

    /**
     * Run the Definition lookupDefinition(String) method test.
     */
    @Test
    void testLookupDefinition() {
        IDefinition result = fixture.lookupDefinition("id");
        assertNull(result);
    }

    /**
     * Run the void setTimestampEnd(long) method test.
     */
    @Test
    void testSetTimestampEnd() {
        fixture.setTimestampEnd(1L);
        assertEquals(fixture.getTimestampEnd(), 1L);
    }

    CTFStreamInput s1;
    CTFStreamInput s2;

    @Test
    void testEquals1() throws CTFException {
        s1 = new CTFStreamInput(new CTFStream(testTraceWrapper.getTrace()),
                createFile());
        assertFalse(s1.equals(null));
    }

    @Test
    void testEquals2() throws CTFException {
        s1 = new CTFStreamInput(new CTFStream(testTraceWrapper.getTrace()),
                createFile());
        assertFalse(s1.equals(new Long(23L)));

    }

    @Test
    void testEquals3() throws CTFException {
        s1 = new CTFStreamInput(new CTFStream(testTraceWrapper.getTrace()),
                createFile());
        assertEquals(s1, s1);

    }

    @Test
    void testEquals4() throws CTFException {
        s1 = new CTFStreamInput(new CTFStream(testTraceWrapper.getTrace()),
                createFile());
        s2 = new CTFStreamInput(new CTFStream(testTraceWrapper.getTrace()),
                createFile());
        assertEquals(s1, s2);
    }
}