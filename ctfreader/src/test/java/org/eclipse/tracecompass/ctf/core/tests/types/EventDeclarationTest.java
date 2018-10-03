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

package org.eclipse.tracecompass.ctf.core.tests.types;

import org.eclipse.tracecompass.ctf.core.CTFException;
import org.eclipse.tracecompass.ctf.core.event.IEventDeclaration;
import org.eclipse.tracecompass.ctf.core.event.IEventDefinition;
import org.eclipse.tracecompass.ctf.core.event.scope.IDefinitionScope;
import org.eclipse.tracecompass.ctf.core.event.types.StructDeclaration;
import org.eclipse.tracecompass.ctf.core.tests.shared.CtfTestTraceExtractor;
import org.eclipse.tracecompass.ctf.core.trace.CTFTrace;
import org.eclipse.tracecompass.ctf.core.trace.CTFTraceReader;
import org.eclipse.tracecompass.internal.ctf.core.event.EventDeclaration;
import org.eclipse.tracecompass.internal.ctf.core.event.LostEventDeclaration;
import org.eclipse.tracecompass.internal.ctf.core.trace.CTFStream;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.lttng.scope.ttt.ctf.CtfTestTrace;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The class <code>EventDeclarationTest</code> contains tests for the class
 * <code>{@link EventDeclaration}</code>.
 *
 * @author ematkho
 * @version $Revision: 1.0 $
 */
@SuppressWarnings("javadoc")
class EventDeclarationTest {

    private static final CtfTestTrace testTrace = CtfTestTrace.KERNEL;
    private static CtfTestTraceExtractor testTraceWrapper;

    private EventDeclaration fixture;

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
        fixture = new EventDeclaration();
        fixture.setContext(new StructDeclaration(1L));
        fixture.setId(1L);
        fixture.setFields(new StructDeclaration(1L));
        fixture.setStream(new CTFStream(testTraceWrapper.getTrace()));
        fixture.setName("");
    }

    /**
     * Run the EventDeclaration() constructor test.
     */
    @Test
    void testEventDeclaration() {
        EventDeclaration result = new EventDeclaration();
        assertNotNull(result);
    }

    /**
     * Run the boolean contextIsSet() method test.
     */
    @Test
    void testContextIsSet() {
        boolean result = fixture.contextIsSet();
        assertTrue(result);
    }

    /**
     * Run the boolean contextIsSet() method test.
     */
    @Test
    void testContextIsSet_null() {
        fixture.setContext((StructDeclaration) null);

        boolean result = fixture.contextIsSet();
        assertFalse(result);
    }

    /**
     * Run the boolean equals(Object) method test.
     *
     * @throws CTFException
     */
    @Test
    void testEquals() throws CTFException {
        EventDeclaration obj = new EventDeclaration();
        obj.setContext(new StructDeclaration(1L));
        obj.setId(1L);
        obj.setFields(new StructDeclaration(1L));
        obj.setStream(new CTFStream(testTraceWrapper.getTrace()));
        obj.setName("");

        assertEquals(fixture, fixture);
        boolean result = fixture.equals(obj);
        assertFalse(result);
    }

    /**
     * Run the boolean equals(Object) method test.
     */
    @Test
    void testEquals_null() {
        Object obj = null;

        boolean result = fixture.equals(obj);
        assertFalse(result);
    }

    /**
     * Run the boolean equals(Object) method test.
     */
    @Test
    void testEquals_emptyObject() {
        Object obj = new Object();

        boolean result = fixture.equals(obj);
        assertFalse(result);
    }

    /**
     * Run the boolean equals(Object) method test.
     */
    @Test
    void testEquals_other1() {
        EventDeclaration obj = new EventDeclaration();
        obj.setContext(fixture.getContext());

        boolean result = fixture.equals(obj);
        assertFalse(result);
    }

    /**
     * Run the boolean equals(Object) method test.
     */
    @Test
    void testEquals_other2() {
        EventDeclaration obj = new EventDeclaration();
        obj.setContext(new StructDeclaration(1L));
        obj.setFields(new StructDeclaration(1L));

        boolean result = fixture.equals(obj);
        assertFalse(result);
    }

    /**
     * Run the boolean equals(Object) method test.
     */
    @Test
    void testEquals_other3() {
        EventDeclaration obj = new EventDeclaration();
        obj.setContext(new StructDeclaration(1L));
        obj.setId(1L);
        obj.setFields(new StructDeclaration(1L));

        boolean result = fixture.equals(obj);
        assertFalse(result);
    }

    /**
     * Run the boolean equals(Object) method test.
     */
    @Test
    void testEquals_other4() {
        EventDeclaration obj = new EventDeclaration();
        obj.setContext(new StructDeclaration(1L));
        obj.setId(1L);
        obj.setFields(new StructDeclaration(1L));
        obj.setName("");

        boolean result = fixture.equals(obj);
        assertFalse(result);
    }

    /**
     * Run the boolean fieldsIsSet() method test.
     */
    @Test
    void testFieldsIsSet() {
        boolean result = fixture.fieldsIsSet();
        assertTrue(result);
    }

    /**
     * Run the boolean fieldsIsSet() method test.
     */
    @Test
    void testFieldsIsSet_null() {
        fixture.setFields((StructDeclaration) null);

        boolean result = fixture.fieldsIsSet();
        assertFalse(result);
    }

    /**
     * Run the StructDeclaration getFields() method test.
     */
    @Test
    void testGetFields() {
        StructDeclaration result = fixture.getFields();
        assertNotNull(result);
    }

    /**
     * Run the Long getId() method test.
     */
    @Test
    void testGetId() {
        assertEquals(1, fixture.id());
    }

    /**
     * Run the String getName() method test.
     */
    @Test
    void testGetName() {
        String result = fixture.getName();
        assertNotNull(result);
    }

    /**
     * Run the Stream getStream() method test.
     */
    @Test
    void testGetStream() {
        CTFStream result = fixture.getStream();
        assertNotNull(result);
    }

    /**
     * Run the int hashCode() method test.
     */
    @Test
    void testHashCode() {
        int result = fixture.hashCode();
        assertTrue(0 != result);
    }

    /**
     * Run the int hashCode() method test.
     */
    @Test
    void testHashCode_null() {
        fixture.setStream((CTFStream) null);
        fixture.setName((String) null);

        int result = fixture.hashCode();
        assertTrue(0 != result);
    }

    /**
     * Run the boolean idIsSet() method test.
     */
    @Test
    void testIdIsSet() {
        boolean result = fixture.idIsSet();
        assertTrue(result);
    }

    /**
     * Run the boolean nameIsSet() method test.
     */
    @Test
    void testNameIsSet() {
        boolean result = fixture.nameIsSet();
        assertTrue(result);
    }

    /**
     * Run the boolean nameIsSet() method test.
     */
    @Test
    void testNameIsSet_null() {
        fixture.setName((String) null);

        boolean result = fixture.nameIsSet();
        assertFalse(result);
    }

    /**
     * Run the boolean streamIsSet() method test.
     */
    @Test
    void testStreamIsSet() {
        boolean result = fixture.streamIsSet();
        assertTrue(result);
    }

    /**
     * Run the boolean streamIsSet() method test.
     */
    @Test
    void testStreamIsSet_null() {
        fixture.setStream((CTFStream) null);

        boolean result = fixture.streamIsSet();
        assertFalse(result);
    }

    /**
     * Test for the EventDefinition class
     *
     * @throws CTFException
     */
    @Test
    void testEventDefinition() throws CTFException {
        CTFTrace trace = testTraceWrapper.getTrace();
        IEventDefinition ed = null;
        try (CTFTraceReader tr = new CTFTraceReader(trace);) {
            tr.advance();
            ed = tr.getCurrentEventDef();
        }
        assertTrue(ed instanceof IDefinitionScope);
        IDefinitionScope ds = (IDefinitionScope)ed;
        assertNotNull(ed);
        assertNotNull(ds.getScopePath());
        assertNotNull(ed.getDeclaration());
        assertNotNull(ed.getFields());
        assertNull(ed.getContext());
        assertNotNull(ed.getPacketContext());
        assertNotNull(ed.getCPU());
        assertNull(ds.lookupDefinition("context"));
        assertNotNull(ds.lookupDefinition("fields"));
        assertNull(ds.lookupDefinition("other"));
        assertNotNull(ed.toString());
    }

    IEventDeclaration e1;
    IEventDeclaration e2;

    @Test
    void testEquals1() {
        e1 = new EventDeclaration();
        assertNotEquals(e1, null);
    }

    @Test
    void testEquals2() {
        e1 = LostEventDeclaration.INSTANCE;
        assertNotEquals(e1, 23L);
    }

    @Test
    void testEquals3() {
        e1 = LostEventDeclaration.INSTANCE;
        assertEquals(e1, e1);
    }

    @Test
    void testEquals4() {
        e1 = LostEventDeclaration.INSTANCE;
        e2 = LostEventDeclaration.INSTANCE;
        assertEquals(e1, e2);
    }

    @Test
    void testEquals5() {
        e1 = LostEventDeclaration.INSTANCE;
        e2 = new EventDeclaration();
        assertNotEquals(e1, e2);
    }
}
