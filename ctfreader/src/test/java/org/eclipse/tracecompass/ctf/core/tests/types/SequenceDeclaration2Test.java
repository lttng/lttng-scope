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
import org.eclipse.tracecompass.ctf.core.event.io.BitBuffer;
import org.eclipse.tracecompass.ctf.core.event.types.*;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The class <code>SequenceDeclarationTest</code> contains tests for the class
 * <code>{@link SequenceDeclaration}</code>.
 *
 * @author ematkho
 */
@SuppressWarnings("javadoc")
class SequenceDeclaration2Test {


    private static final @NotNull String FIELD_NAME = "LengthName";

    private SequenceDeclaration fixture;
    private @NotNull BitBuffer input = new BitBuffer();

    @BeforeEach
    void setUp() {
        fixture = new SequenceDeclaration(FIELD_NAME, StringDeclaration.getStringDeclaration(Encoding.UTF8));
        byte array[] = { 't', 'e', 's', 't', '\0', 't', 'h', 'i', 's', '\0' };
        ByteBuffer byb = ByteBuffer.wrap(array);
        input = new BitBuffer(byb);
    }

    /**
     * Run the SequenceDeclaration(String,Declaration) constructor test.
     */
    @Test
    void testSequenceDeclaration() {
        String lengthName = "";
        IDeclaration elemType = StringDeclaration.getStringDeclaration(Encoding.UTF8);

        SequenceDeclaration result = new SequenceDeclaration(lengthName, elemType);
        assertNotNull(result);
        String string = "[declaration] sequence[";
        assertEquals(string, result.toString().substring(0, string.length()));
    }

    /**
     * Run the SequenceDefinition createDefinition(DefinitionScope,String)
     * method test.
     *
     * @throws CTFException
     *             an error in the bitbuffer
     */
    @Test
    void testCreateDefinition() throws CTFException {
        long seqLen = 2;
        IntegerDeclaration id = IntegerDeclaration.createDeclaration(8, false, 8,
                ByteOrder.LITTLE_ENDIAN, Encoding.UTF8, "", 32);
        StructDeclaration structDec = new StructDeclaration(0);
        structDec.addField(FIELD_NAME, id);
        StructDefinition structDef = new StructDefinition(
                structDec,
                null,
                "x",
                new Definition[] {
                        new IntegerDefinition(
                                id,
                                null,
                                FIELD_NAME,
                                seqLen)
                });
        AbstractArrayDefinition result = fixture.createDefinition(structDef, FIELD_NAME, input);
        assertNotNull(result);
    }

    /**
     * Run the Declaration getElementType() method test.
     */
    @Test
    void testGetElementType() {
        IDeclaration result = fixture.getElementType();
        assertNotNull(result);
    }

    /**
     * Run the String toString() method test.
     */
    @Test
    void testToString() {
        String result = fixture.toString();
        String left = "[declaration] sequence[";
        assertEquals(left, result.substring(0, left.length()));
    }

    /**
     * Test the hashcode
     */
    @Test
    void hashcodeTest() {
        assertEquals(-1140774256, fixture.hashCode());
        SequenceDeclaration a = new SequenceDeclaration("Hi", IntegerDeclaration.INT_32B_DECL);
        SequenceDeclaration b = new SequenceDeclaration("Hello", IntegerDeclaration.INT_32B_DECL);
        SequenceDeclaration c = new SequenceDeclaration("Hi", StringDeclaration.getStringDeclaration(Encoding.UTF8));
        SequenceDeclaration d = new SequenceDeclaration("Hi", IntegerDeclaration.INT_32B_DECL);
        assertNotEquals(a.hashCode(), b.hashCode());
        assertNotEquals(a.hashCode(), c.hashCode());
        assertEquals(a.hashCode(), d.hashCode());
    }

    /**
     * Test the equals
     */
    @Test
    void equalsTest() {
        SequenceDeclaration a = new SequenceDeclaration("Hi", IntegerDeclaration.INT_32B_DECL);
        SequenceDeclaration b = new SequenceDeclaration("Hello", IntegerDeclaration.INT_32B_DECL);
        SequenceDeclaration c = new SequenceDeclaration("Hi", StringDeclaration.getStringDeclaration(Encoding.UTF8));
        SequenceDeclaration d = new SequenceDeclaration("Hi", IntegerDeclaration.INT_32B_DECL);
        assertNotEquals(a, null);
        assertNotEquals(a, new Object());
        assertNotEquals(a, b);
        assertNotEquals(a, c);
        assertEquals(a, d);
        assertNotEquals(b, a);
        assertNotEquals(c, a);
        assertEquals(d, a);
        assertEquals(a, a);
        assertFalse(a.isBinaryEquivalent(b));
        assertFalse(a.isBinaryEquivalent(c));
        assertTrue(a.isBinaryEquivalent(d));
        assertFalse(b.isBinaryEquivalent(a));
        assertFalse(c.isBinaryEquivalent(a));
        assertTrue(d.isBinaryEquivalent(a));
        assertTrue(a.isBinaryEquivalent(a));
    }

}
