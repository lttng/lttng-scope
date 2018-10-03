/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 * Copyright (C) 2012-2015 Ericsson
 * Copyright (C) 2010-2011 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package ca.polymtl.dorsal.libdelorean.backend.historytree

import ca.polymtl.dorsal.libdelorean.exceptions.TimeRangeException
import ca.polymtl.dorsal.libdelorean.interval.StateInterval
import ca.polymtl.dorsal.libdelorean.statevalue.*
import java.io.IOException
import java.nio.ByteBuffer

class HTInterval : StateInterval {

    @Transient
    val sizeOnDisk: Int

    constructor(start: Long,
                end: Long,
                attribute: Int,
                stateValue: StateValue) : super(start, end, attribute, stateValue) {
        sizeOnDisk = computeIntervalSizeOnDisk(stateValue)

        /* We only support values up to 2^16 in length */
        if (sizeOnDisk > Short.MAX_VALUE) {
            throw IllegalArgumentException("Interval is too large for the state system: " + this.toString())
        }
    }

    private constructor(start: Long,
                        end: Long,
                        attribute: Int,
                        stateValue: StateValue,
                        size: Int) : super(start, end, attribute, stateValue) {
        sizeOnDisk = size
    }

    companion object {
        /**
         * Reader factory method. Builds the interval using an already-allocated
         * ByteBuffer, which normally comes from a NIO FileChannel.
         *
         * @param buffer
         *            The ByteBuffer from which to read the information
         * @return The interval object
         * @throws IOException
         *             If there was an error reading from the buffer
         */
        fun readFrom(buffer: ByteBuffer): HTInterval {
            val startPos: Int = buffer.position()

            /* Read the data common to all intervals */
            val valueType: Byte = buffer.get()
            val intervalStart: Long = buffer.getLong()
            val intervalEnd: Long = buffer.getLong()
            val attribute: Int = buffer.getInt()

            val value: StateValue = when (valueType) {
                TYPE_NULL -> StateValue.nullValue()
                TYPE_BOOLEAN_TRUE -> StateValue.newValueBoolean(true)
                TYPE_BOOLEAN_FALSE -> StateValue.newValueBoolean(false)
                TYPE_INTEGER -> StateValue.newValueInt(buffer.getInt())
                TYPE_LONG -> StateValue.newValueLong(buffer.getLong())
                TYPE_DOUBLE -> StateValue.newValueDouble(buffer.getDouble())
                TYPE_STRING -> {
                    /* the first byte = the size to read */
                    val strSize: Short = buffer.getShort()
                    val array = ByteArray(strSize.toInt())
                    buffer.get(array)

                    /* Confirm the 0'ed byte at the end */
                    val res: Byte = buffer.get()
                    if (res != 0.toByte()) {
                        throw IOException(errMsg)
                    }

                    StateValue.newValueString(String(array))
                }
                else -> {
                    /* Unknown data, better to not make anything up... */
                    throw IOException(errMsg)
                }
            }

            val intervalSize: Int = buffer.position() - startPos
            try {
                return HTInterval(intervalStart, intervalEnd, attribute, value, intervalSize)
            } catch (e: TimeRangeException) {
                throw IOException(errMsg, e)
            }

        }
    }

    /**
     * Antagonist of the readFrom() factory method, write the Data entry
     * corresponding to this interval in a ByteBuffer (mapped to a block in the
     * history-file, hopefully)
     *
     * @param buffer
     *            The already-allocated ByteBuffer corresponding to a SHT Node
     */
    fun writeInterval(buffer: ByteBuffer) {
        val startPos: Int = buffer.position()

        val typeByte: Byte = getByteFromType(stateValue)

        buffer.put(typeByte)
        buffer.putLong(start)
        buffer.putLong(end)
        buffer.putInt(attribute)

        when (stateValue) {
            is NullStateValue, is BooleanStateValue -> {} /* Nothing else to write, 'typeByte' carries all the information */
            is IntegerStateValue -> buffer.putInt(stateValue.value)
            is LongStateValue -> buffer.putLong(stateValue.value)
            is DoubleStateValue -> buffer.putDouble(stateValue.value)
            is StringStateValue -> {
                val str = stateValue.value
                val strArray = str.toByteArray()
                /* Write the string size, then the actual bytes, then \0 */
                buffer.putShort(strArray.size.toShort())
                buffer.put(strArray)
                buffer.put(0.toByte())
            }
        }

        val written: Int = buffer.position() - startPos
        if (written != sizeOnDisk) {
            throw IllegalStateException("Did not write the expected amount of bytes when serializing interval.")
        }
    }

    /*
     * The implementations from the super class are fine since we don't add
     * any (non-transient) fields.
     */
    override fun equals(other: Any?): Boolean = super.equals(other)
    override fun hashCode(): Int = super.hashCode()
    override fun toString(): String = super.toString()

}

/* 'Byte' equivalent for state values types */
private const val TYPE_NULL: Byte = -1
private const val TYPE_INTEGER: Byte = 0
private const val TYPE_STRING: Byte = 1
private const val TYPE_LONG: Byte = 2
private const val TYPE_DOUBLE: Byte = 3
private const val TYPE_BOOLEAN_TRUE: Byte = 4
private const val TYPE_BOOLEAN_FALSE: Byte = 5

private const val errMsg: String = "Invalid interval data. Maybe your file is corrupt?"

private fun computeIntervalSizeOnDisk(sv: StateValue): Int {
    /*
     * Minimum size is 2x long (start and end), 1x int (attribute) and 1x
     * byte (value type).
     */
    val minSize = java.lang.Long.BYTES + java.lang.Long.BYTES + Integer.BYTES + java.lang.Byte.BYTES

    val svSize = when (sv) {
        is NullStateValue, is BooleanStateValue -> 0
        is IntegerStateValue -> Integer.BYTES
        is LongStateValue -> java.lang.Long.BYTES
        is DoubleStateValue -> java.lang.Double.BYTES
        is StringStateValue -> sv.value.toByteArray().size + 3  /* String's length + 3 (2 bytes for size, 1 byte for \0 at the end */
    }

    return minSize + svSize
}

/**
 * Here we determine how state values "types" are written in the 8-bit field
 * that indicates the value type in the file.
 */
private fun getByteFromType(sv: StateValue): Byte {
    return when(sv) {
        is NullStateValue -> TYPE_NULL
        is BooleanStateValue -> if (sv.value) TYPE_BOOLEAN_TRUE else TYPE_BOOLEAN_FALSE
        is IntegerStateValue -> TYPE_INTEGER
        is LongStateValue -> TYPE_LONG
        is DoubleStateValue -> TYPE_DOUBLE
        is StringStateValue -> TYPE_STRING
    }
}