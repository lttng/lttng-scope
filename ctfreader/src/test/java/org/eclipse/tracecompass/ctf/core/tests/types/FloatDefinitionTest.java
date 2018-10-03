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
import org.eclipse.tracecompass.ctf.core.event.types.FloatDeclaration;
import org.eclipse.tracecompass.ctf.core.event.types.FloatDefinition;
import org.eclipse.tracecompass.ctf.core.event.types.IntegerDefinition;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * The class <code>IntegerDefinitionTest</code> contains tests for the class
 * <code>{@link IntegerDefinition}</code>.
 *
 * @author ematkho
 * @version $Revision: 1.0 $
 */
@SuppressWarnings("javadoc")
class FloatDefinitionTest {

    private FloatDefinition fixture;
    private FloatDefinition singleFixture;
    private FloatDefinition doubleFixture; // all the way.
    private FloatDeclaration parent;
    private static final @NotNull String fieldName = "float";

    /**
     * Perform pre-test initialization.
     *
     * @throws CTFException
     *             error creating floats
     */
    @BeforeEach
    void setUp() throws CTFException {
        testFloat248();
        testFloat5311();
    }

    @Test
    void testFloat248() throws CTFException {
        parent = new FloatDeclaration(8, 24, ByteOrder.nativeOrder(), 0);
        BitBuffer bb = create32BitFloatByteBuffer();
        singleFixture = parent.createDefinition(null, fieldName, bb);
        assertNotNull(singleFixture);
    }

    @Test
    void testFloat5311() throws CTFException {
        parent = new FloatDeclaration(11, 53, ByteOrder.nativeOrder(), 0);
        BitBuffer bb = create64BitFloatByteBuffer();
        doubleFixture = parent.createDefinition(null, fieldName, bb);
        assertNotNull(doubleFixture);
    }

    @Test
    void testFloat32Bit() throws CTFException {
        for (int i = 1; i < 31; i++) {
            parent = new FloatDeclaration(i, 32 - i, ByteOrder.nativeOrder(), 0);

            fixture = parent.createDefinition(null, fieldName, create32BitFloatByteBuffer());
            assertNotNull(fixture);
            assertEquals("2.0", fixture.toString(), "test" + i);
        }
    }

    @Test
    void testFloat64Bit() throws CTFException {
        for (int i = 1; i < 63; i++) {
            parent = new FloatDeclaration(i, 64 - i, ByteOrder.nativeOrder(), 0);
            fixture = parent.createDefinition(null, fieldName, create64BitFloatByteBuffer());
            assertNotNull(fixture);
            if (i <= 32) {
                assertEquals("2.0", fixture.toString(), "test" + i);
            } else if (i == 33) {
                assertEquals("1.0", fixture.toString(), "test" + i);
            } else {
                assertNotNull(fixture.getValue());
            }

        }
    }

    @Test
    void testFloat48Bit() throws CTFException {
        parent = new FloatDeclaration(12, 32, ByteOrder.nativeOrder(), 0);
        fixture = parent.createDefinition(null, fieldName, create64BitFloatByteBuffer());
        assertNotNull(fixture);
        assertEquals(Double.NaN, fixture.getValue(), 0.1);
    }

    /**
     * Run the IntegerDeclaration getDeclaration() method test.
     */
    @Test
    void testGetDeclaration() {
        FloatDeclaration result = singleFixture.getDeclaration();
        assertNotNull(result);
    }

    /**
     * Run the long getValue() method test.
     */
    @Test
    void testGetValue() {
        double result = singleFixture.getValue();
        assertEquals(2.0, result, 0.1);
    }

    /**
     * Run the String toString() method test.
     */
    @Test
    void testToString() {
        String result = singleFixture.toString();
        assertNotNull(result);
        assertEquals("2.0", result);
    }

    private static @NotNull BitBuffer create32BitFloatByteBuffer() {
        float[] data = new float[2];
        data[0] = 2.0f;
        data[1] = 3.14f;
        ByteBuffer byb = ByteBuffer.allocate(128);
        byb.order(ByteOrder.nativeOrder());
        byb.mark();
        byb.putFloat(data[0]);
        byb.putFloat(data[1]);
        byb.reset();
        BitBuffer bb = new BitBuffer(byb);
        return bb;
    }

    private static @NotNull BitBuffer create64BitFloatByteBuffer() {
        double[] data = new double[2];
        data[0] = 2.0f;
        data[1] = 3.14f;
        ByteBuffer byb = ByteBuffer.allocate(128);
        byb.order(ByteOrder.nativeOrder());
        byb.mark();
        byb.putDouble(data[0]);
        byb.putDouble(data[1]);
        byb.reset();
        BitBuffer bb = new BitBuffer(byb);
        return bb;
    }
}
