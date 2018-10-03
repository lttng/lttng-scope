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

package org.eclipse.tracecompass.ctf.core.tests.io;

import org.eclipse.tracecompass.ctf.core.CTFException;
import org.eclipse.tracecompass.ctf.core.event.io.BitBuffer;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The class <code>BitBufferTest</code> contains tests for the class
 * <code>{@link BitBuffer}</code>.
 *
 * @author ematkho
 * @version $Revision: 1.0 $
 */
class BitBufferTest {

    private BitBuffer fixture;

    /**
     * Perform pre-test initialization.
     *
     * @throws CTFException
     *             An error that cannot happen (position is under 128)
     */
    @BeforeEach
    void setUp() throws CTFException {
        fixture = new BitBuffer(Util.testMemory(ByteBuffer.allocateDirect(1)));
        fixture.setByteOrder(ByteOrder.BIG_ENDIAN);
        fixture.position(1);
    }

    /**
     * Run the BitBuffer() constructor test.
     */
    @Test
    void testBitBuffer() {
        BitBuffer result = new BitBuffer();

        assertNotNull(result);
        assertEquals(0, result.position());
        assertNotNull(result.getByteBuffer());
    }

    /**
     * Run the BitBuffer(ByteBuffer) constructor test.
     */
    @Test
    void testBitBuffer_fromByteBuffer() {
        BitBuffer result = new BitBuffer(Util.testMemory(ByteBuffer.allocate(0)));
        assertNotNull(result);
        assertEquals(0, result.position());
    }

    /**
     * Run the boolean canRead(int) method test.
     */
    @Test
    void testCanRead_1param() {
        int length = 1;
        boolean result = fixture.canRead(length);

        assertTrue(result);
    }

    /**
     * Run the void clear() method test.
     */
    @Test
    void testClear() {
        fixture.clear();
    }

    /**
     * Run the ByteBuffer getByteBuffer() method test.
     */
    @Test
    void testGetByteBuffer() {
        ByteBuffer result = fixture.getByteBuffer();

        assertNotNull(result);
        assertEquals("java.nio.DirectByteBuffer[pos=0 lim=1 cap=1]", result.toString());
        assertTrue(result.isDirect());
        assertFalse(result.hasArray());
        assertEquals(1, result.limit());
        assertEquals(1, result.remaining());
        assertEquals(0, result.position());
        assertEquals(1, result.capacity());
        assertTrue(result.hasRemaining());
        assertFalse(result.isReadOnly());
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
     * Run the ByteOrder order() method test.
     */
    @Test
    void testGetOrder() {
        ByteOrder result = fixture.getByteOrder();

        assertNotNull(result);
        assertEquals("BIG_ENDIAN", result.toString());
    }

    /**
     * Run the void order(ByteOrder) method test.
     */
    @Test
    void testSetOrder() {
        ByteOrder order = ByteOrder.BIG_ENDIAN;

        fixture.setByteOrder(order);
    }

    /**
     * Run the int position() method test.
     */
    @Test
    void testGetPosition() {
        long result = fixture.position();

        assertEquals(1, result);
    }

    /**
     * Run the void position(int) method test.
     *
     * @throws CTFException
     *             out of bounds? won't happen
     */
    @Test
    void testSetPosition() throws CTFException {
        int newPosition = 1;
        fixture.position(newPosition);
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
     * Test the get function
     */
    @Test
    void testGetBytes() {
        @NotNull byte[] data = new byte[2];
        ByteBuffer bb = ByteBuffer.allocate(10);
        bb.put((byte) 0);
        bb.put((byte) 1);
        bb.put((byte) 1);
        bb.put((byte) 0);
        fixture = new BitBuffer(bb);
        fixture.get(data);
        assertEquals(0, data[0]);
        assertEquals(1, data[1]);
        fixture.get(data);
        assertEquals(1, data[0]);
        assertEquals(0, data[1]);
    }

    /**
     * Test the get function
     *
     * @throws CTFException
     *             won't happen but we seek in a buffer
     */
    @Test
    void testGetBytesMiddle() throws CTFException {
        @NotNull byte[] data = new byte[5];
        // this string has been carefully selected and tested... don't change
        // the string and expect the result to be the same.
        fixture = new BitBuffer(Util.testMemory(ByteBuffer.wrap(new String("hello world").getBytes())));
        fixture.position(6 * 8);
        fixture.get(data);
        String actual = new String(data);
        assertEquals("world", actual);
    }
}