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

import org.eclipse.tracecompass.ctf.core.event.types.FloatDeclaration;
import org.junit.jupiter.api.Test;

import java.nio.ByteOrder;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("javadoc")
class FloatDeclarationTest {
    private FloatDeclaration fixture;

    @Test
    void ctorTest() {
        for (int i = 1; i < 20; i++) {
            fixture = new FloatDeclaration(i, 32 - i, ByteOrder.nativeOrder(), 0);
            assertNotNull(fixture);
        }
    }

    @Test
    void getterTest() {
        fixture = new FloatDeclaration(8, 24, ByteOrder.nativeOrder(), 1);
        assertEquals(fixture.getAlignment(), 1);
        assertEquals(fixture.getByteOrder(), ByteOrder.nativeOrder());
        assertEquals(fixture.getExponent(), 8);
        assertEquals(fixture.getMantissa(), 24);
    }

    @Test
    void toStringTest() {
        fixture = new FloatDeclaration(8, 24, ByteOrder.nativeOrder(), 0);
        assertTrue(fixture.toString().contains("float"));
    }

    /**
     * Test the hashcode
     */
    @Test
    void hashcodeTest() {
        FloatDeclaration floatDeclaration = new FloatDeclaration(8, 24, ByteOrder.BIG_ENDIAN, 0);
        FloatDeclaration a = new FloatDeclaration(8, 24, ByteOrder.BIG_ENDIAN, 0);
        FloatDeclaration b = new FloatDeclaration(8, 24, ByteOrder.LITTLE_ENDIAN, 0);
        assertEquals(a.hashCode(), floatDeclaration.hashCode());
        assertNotEquals(b.hashCode(), floatDeclaration.hashCode());
        assertEquals(floatDeclaration.hashCode(), floatDeclaration.hashCode());
    }

    /**
     * Test the equals
     */
    @Test
    void equalsTest() {
        FloatDeclaration a = new FloatDeclaration(8, 24, ByteOrder.BIG_ENDIAN, 0);
        FloatDeclaration b = new FloatDeclaration(8, 24, ByteOrder.LITTLE_ENDIAN, 0);
        FloatDeclaration c = new FloatDeclaration(8, 24, ByteOrder.BIG_ENDIAN, 8);
        FloatDeclaration d = new FloatDeclaration(8, 8, ByteOrder.BIG_ENDIAN, 0);
        FloatDeclaration e = new FloatDeclaration(24, 24, ByteOrder.BIG_ENDIAN, 0);
        FloatDeclaration f = new FloatDeclaration(8, 24, ByteOrder.BIG_ENDIAN, 0);
        assertNotEquals(a, null);
        assertNotEquals(a, new Object());
        assertNotEquals(a, b);
        assertNotEquals(a, c);
        assertNotEquals(a, d);
        assertNotEquals(b, a);
        assertNotEquals(c, a);
        assertNotEquals(d, a);
        assertNotEquals(e, a);
        assertNotEquals(a, e);

        assertEquals(a, f);
        assertEquals(f, a);
        assertEquals(a, a);
    }
}
