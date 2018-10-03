/*******************************************************************************
 * Copyright (c) 2013, 2014 Ericsson
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Khouzam - Initial API and implementation
 *     Marc-Andre Laperle - Add min/maximum for validation
 *******************************************************************************/

package org.eclipse.tracecompass.ctf.core.tests.types;

import org.eclipse.tracecompass.ctf.core.event.types.Encoding;
import org.eclipse.tracecompass.ctf.core.event.types.IntegerDeclaration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.nio.ByteOrder;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The class <code>IntegerDeclarationTest</code> contains tests for the class
 * <code>{@link IntegerDeclaration}</code>.
 *
 * @author ematkho
 * @version $Revision: 1.0 $
 */
class IntegerDeclarationTest {

    private IntegerDeclaration fixture;

    /**
     * Perform pre-test initialization.
     */
    @BeforeEach
    public void setUp() {
        fixture = IntegerDeclaration.createDeclaration(1, false, 1, ByteOrder.BIG_ENDIAN,
                Encoding.ASCII, "", 32);
    }

    /**
     * Run the IntegerDeclaration(int,boolean,int,ByteOrder,Encoding)
     * constructor test.
     */
    @Test
    void testIntegerDeclaration() {
        int len = 1;
        boolean signed = false;
        int base = 1;
        ByteOrder byteOrder = ByteOrder.BIG_ENDIAN;
        Encoding encoding = Encoding.ASCII;

        IntegerDeclaration result = IntegerDeclaration.createDeclaration(len, signed, base,
                byteOrder, encoding, "", 16);

        assertNotNull(result);
        assertEquals(1, result.getBase());
        assertFalse(result.isCharacter());
        String outputValue = "[declaration] integer[";
        assertEquals(outputValue,
                result.toString().substring(0, outputValue.length()));
        assertEquals(1, result.getLength());
        assertFalse(result.isSigned());
    }

    /**
     * Test the factory part more rigorously to make sure there are no
     * regressions
     */
    @Test
    void testIntegerDeclarationBruteForce() {
        ByteOrder[] bos = { ByteOrder.LITTLE_ENDIAN, ByteOrder.BIG_ENDIAN };
        Encoding[] encodings = { Encoding.ASCII, Encoding.NONE, Encoding.UTF8 };
        boolean[] signeds = { true, false }; // not a real word
        String[] clocks = { "something", "" };
        int[] bases = { 2, 4, 6, 8, 10, 12, 16 };
        for (int len = 2; len < 65; len++) {
            for (ByteOrder bo : bos) {
                for (boolean signed : signeds) {
                    for (int base : bases) {
                        for (Encoding enc : encodings) {
                            for (String clock : clocks) {
                                assertNotNull(enc);
                                assertNotNull(clock);
                                IntegerDeclaration intDec = IntegerDeclaration.createDeclaration(len, signed, base, bo, enc, clock, 8);
                                String title = Integer.toString(len) + " " + bo + " " + signed + " " + base + " " + enc;
                                assertEquals(signed, intDec.isSigned(), title);
                                assertEquals(base, intDec.getBase(), title);
                                // at len 8 le and be are the same
                                if (len != 8) {
                                    assertEquals(bo, intDec.getByteOrder(), title);
                                }
                                assertEquals(len, intDec.getLength(), title);
                                assertEquals(len, intDec.getMaximumSize(), title);
                                assertEquals(clock, intDec.getClock(), title);
                                assertEquals(!signed && len == 8, intDec.isUnsignedByte(), title);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Run the int getBase() method test.
     */
    @Test
    void testGetBase() {
        int result = fixture.getBase();
        assertEquals(1, result);
    }

    /**
     * Run the ByteOrder getByteOrder() method test.
     */
    @Test
    void testGetByteOrder() {
        ByteOrder result = fixture.getByteOrder();
        assertNotNull(result);
        assertEquals("BIG_ENDIAN", result.toString());
    }

    /**
     * Run the Encoding getEncoding() method test.
     */
    @Test
    void testGetEncoding() {
        Encoding result = fixture.getEncoding();
        assertNotNull(result);
        assertEquals("ASCII", result.name());
        assertEquals("ASCII", result.toString());
        assertEquals(1, result.ordinal());
    }

    /**
     * Run the int getLength() method test.
     */
    @Test
    void testGetLength() {
        int result = fixture.getLength();
        assertEquals(1, result);
    }

    /**
     * Run the boolean isCharacter() method test.
     */
    @Test
    void testIsCharacter() {
        boolean result = fixture.isCharacter();
        assertFalse(result);
    }

    /**
     * Run the boolean isCharacter() method test.
     */
    @Test
    void testIsCharacter_8bytes() {
        IntegerDeclaration fixture8 = IntegerDeclaration.createDeclaration(8, true, 1,
                ByteOrder.BIG_ENDIAN, Encoding.ASCII, "", 8);

        boolean result = fixture8.isCharacter();
        assertTrue(result);
    }

    /**
     * Run the boolean isSigned() method test.
     */
    @Test
    void testIsSigned_signed() {
        IntegerDeclaration fixtureSigned = IntegerDeclaration.createDeclaration(2, true,
                1, ByteOrder.BIG_ENDIAN, Encoding.ASCII, "", 8);
        boolean result = fixtureSigned.isSigned();
        assertTrue(result);
    }

    /**
     * Run the boolean isSigned() method test.
     */
    @Test
    void testIsSigned_unsigned() {
        boolean result = fixture.isSigned();
        assertFalse(result);
    }

    /**
     * Run the String toString() method test.
     */
    @Test
    void testToString() {
        String result = fixture.toString();
        String trunc = result.substring(0, 22);
        assertEquals("[declaration] integer[", trunc);
    }

    /**
     * Run the long getMaxValue() method test.
     */
    @Test
    void testMaxValue() {
        assertEquals(BigInteger.ONE, fixture.getMaxValue());

        IntegerDeclaration signed8bit = IntegerDeclaration.createDeclaration(8, true, 1, ByteOrder.BIG_ENDIAN, Encoding.ASCII, "", 32);
        assertEquals(BigInteger.valueOf(127), signed8bit.getMaxValue());

        IntegerDeclaration unsigned8bit = IntegerDeclaration.createDeclaration(8, false, 1, ByteOrder.BIG_ENDIAN, Encoding.ASCII, "", 32);
        assertEquals(BigInteger.valueOf(255), unsigned8bit.getMaxValue());

        IntegerDeclaration signed32bit = IntegerDeclaration.createDeclaration(32, true, 1, ByteOrder.BIG_ENDIAN, Encoding.ASCII, "", 32);
        assertEquals(BigInteger.valueOf(2147483647), signed32bit.getMaxValue());

        IntegerDeclaration unsigned32bit = IntegerDeclaration.createDeclaration(32, false, 1, ByteOrder.BIG_ENDIAN, Encoding.ASCII, "", 32);
        assertEquals(BigInteger.valueOf(4294967295l), unsigned32bit.getMaxValue());

        IntegerDeclaration signed64bit = IntegerDeclaration.createDeclaration(64, true, 1, ByteOrder.BIG_ENDIAN, Encoding.ASCII, "", 32);
        assertEquals(BigInteger.valueOf(9223372036854775807L), signed64bit.getMaxValue());

        IntegerDeclaration unsigned64bit = IntegerDeclaration.createDeclaration(64, false, 1, ByteOrder.BIG_ENDIAN, Encoding.ASCII, "", 32);
        assertEquals(BigInteger.valueOf(2).pow(64).subtract(BigInteger.ONE), unsigned64bit.getMaxValue());
    }

    /**
     * Run the long getMinValue() method test.
     */
    @Test
    void testMinValue() {
        assertEquals(BigInteger.ZERO, fixture.getMinValue());

        IntegerDeclaration signed8bit = IntegerDeclaration.createDeclaration(8, true, 1, ByteOrder.BIG_ENDIAN, Encoding.ASCII, "", 32);
        assertEquals(BigInteger.valueOf(-128), signed8bit.getMinValue());

        IntegerDeclaration unsigned8bit = IntegerDeclaration.createDeclaration(8, false, 1, ByteOrder.BIG_ENDIAN, Encoding.ASCII, "", 32);
        assertEquals(BigInteger.ZERO, unsigned8bit.getMinValue());

        IntegerDeclaration signed32bit = IntegerDeclaration.createDeclaration(32, true, 1, ByteOrder.BIG_ENDIAN, Encoding.ASCII, "", 32);
        assertEquals(BigInteger.valueOf(-2147483648), signed32bit.getMinValue());

        IntegerDeclaration unsigned32bit = IntegerDeclaration.createDeclaration(32, false, 1, ByteOrder.BIG_ENDIAN, Encoding.ASCII, "", 32);
        assertEquals(BigInteger.ZERO, unsigned32bit.getMinValue());

        IntegerDeclaration signed64bit = IntegerDeclaration.createDeclaration(64, true, 1, ByteOrder.BIG_ENDIAN, Encoding.ASCII, "", 32);
        assertEquals(BigInteger.valueOf(-9223372036854775808L), signed64bit.getMinValue());

        IntegerDeclaration unsigned64bit = IntegerDeclaration.createDeclaration(64, false, 1, ByteOrder.BIG_ENDIAN, Encoding.ASCII, "", 32);
        assertEquals(BigInteger.ZERO, unsigned64bit.getMinValue());
    }

    /**
     * Test the hashcode
     */
    @Test
    void hashcodeTest() {
        IntegerDeclaration a = IntegerDeclaration.createDeclaration(32, false, 10, ByteOrder.BIG_ENDIAN, Encoding.NONE, "", 32);
        IntegerDeclaration i = IntegerDeclaration.createDeclaration(32, false, 10, ByteOrder.BIG_ENDIAN, Encoding.NONE, "", 32);
        assertEquals(a.hashCode(), i.hashCode());
        assertEquals(a.hashCode(), a.hashCode());
    }

    /**
     * Test the equals
     */
    @Test
    void equalsTest() {
        IntegerDeclaration a = IntegerDeclaration.createDeclaration(32, false, 10, ByteOrder.BIG_ENDIAN, Encoding.NONE, "", 32);
        IntegerDeclaration b = IntegerDeclaration.createDeclaration(8, false, 10, ByteOrder.BIG_ENDIAN, Encoding.NONE, "", 32);
        IntegerDeclaration c = IntegerDeclaration.createDeclaration(32, true, 10, ByteOrder.BIG_ENDIAN, Encoding.NONE, "", 32);
        IntegerDeclaration d = IntegerDeclaration.createDeclaration(32, false, 16, ByteOrder.BIG_ENDIAN, Encoding.NONE, "", 32);
        IntegerDeclaration e = IntegerDeclaration.createDeclaration(32, false, 10, ByteOrder.LITTLE_ENDIAN, Encoding.NONE, "", 32);
        IntegerDeclaration f = IntegerDeclaration.createDeclaration(32, false, 10, ByteOrder.BIG_ENDIAN, Encoding.UTF8, "", 32);
        IntegerDeclaration g = IntegerDeclaration.createDeclaration(32, false, 10, ByteOrder.BIG_ENDIAN, Encoding.NONE, "hi", 32);
        IntegerDeclaration h = IntegerDeclaration.createDeclaration(32, false, 10, ByteOrder.BIG_ENDIAN, Encoding.NONE, "", 16);
        IntegerDeclaration i = IntegerDeclaration.createDeclaration(32, false, 10, ByteOrder.BIG_ENDIAN, Encoding.NONE, "", 32);
        assertNotEquals(a, null);
        assertNotEquals(a, new Object());
        assertNotEquals(a, b);
        assertNotEquals(a, c);
        assertNotEquals(a, d);
        assertNotEquals(a, e);
        assertNotEquals(a, f);
        assertNotEquals(a, g);
        assertNotEquals(a, h);
        assertEquals(a, i);
        assertNotEquals(b, a);
        assertNotEquals(c, a);
        assertNotEquals(d, a);
        assertNotEquals(e, a);
        assertNotEquals(f, a);
        assertNotEquals(g, a);
        assertNotEquals(h, a);
        assertEquals(i, a);
        assertEquals(a, a);
    }

}