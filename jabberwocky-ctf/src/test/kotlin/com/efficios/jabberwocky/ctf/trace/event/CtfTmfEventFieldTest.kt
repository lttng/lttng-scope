/*******************************************************************************
 * Copyright (c) 2012, 2014 Ericsson

 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html

 * Contributors:
 * Matthew Khouzam - Initial generation with CodePro tools
 * Alexandre Montplaisir - Clean up, consolidate redundant tests
 */

package com.efficios.jabberwocky.ctf.trace.event

import com.efficios.jabberwocky.trace.event.FieldValue.ArrayValue
import com.efficios.jabberwocky.trace.event.FieldValue.IntegerValue
import org.eclipse.tracecompass.ctf.core.CTFException
import org.eclipse.tracecompass.ctf.core.event.io.BitBuffer
import org.eclipse.tracecompass.ctf.core.event.types.*
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.UnsupportedEncodingException
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * The class `CtfTmfEventFieldTest` contains tests for the class
 * `CtfTmfEventField`.

 * @author Matthew Khouzam
 */
class CtfTmfEventFieldTest {

    companion object {
        private const val ROOT = "root"
        private const val SEQ = "seq"
        private const val ARRAY_STR = "array_str"
        private const val ARRAY_FLOAT = "array_float"
        private const val ARRAY_INT = "array_int"
        private const val ARRAY_STRUCT = "array_struct"
        private const val ARRAY_VARIANT = "array_variant"
        private const val ARRAY_ENUM = "array_enum"
        private const val STR = "str"
        private const val FLOAT = "float"
        private const val LEN = "len"
        private const val INT = "int"
        private const val STRUCT = "struct"
        private const val VARIANT = "variant"
        private const val ENUM = "enum"

        private const val TEST_NUMBER: Byte = 2
        private const val TEST_STRING = "two"

        private const val ARRAY_SIZE = 2
    }

    private lateinit var fixture: StructDefinition

    /**
     * Perform pre-test initialization.

     * @throws UnsupportedEncodingException
     * *             Thrown when UTF-8 encoding is not available.
     * *
     * @throws CTFException
     * *             error
     */
    @BeforeEach
    fun setUp() {
        val testStringBytes = TEST_STRING.toByteArray(Charsets.UTF_8)

        val capacity = 2048
        val bb = ByteBuffer.allocateDirect(capacity)

        val sDec = StructDeclaration(1L)
        val strDec = StringDeclaration.getStringDeclaration(Encoding.UTF8)
        val byteDec = IntegerDeclaration.UINT_8_DECL
        val intDec = IntegerDeclaration.INT_8_DECL
        val flDec = FloatDeclaration(8, 24,
                ByteOrder.BIG_ENDIAN, 8)
        val seqDec = SequenceDeclaration(LEN, byteDec)
        val structDec = StructDeclaration(8)
        val enumDec = EnumDeclaration(byteDec)
        val varDec = VariantDeclaration()
        val arrStrDec = ArrayDeclaration(ARRAY_SIZE, strDec)
        val arrFloatDec = ArrayDeclaration(ARRAY_SIZE, flDec)
        val arrIntDec = ArrayDeclaration(ARRAY_SIZE, intDec)
        val arrStructDec = ArrayDeclaration(ARRAY_SIZE, structDec)
        val arrVariantDec = ArrayDeclaration(ARRAY_SIZE, varDec)
        val arrEnumDec = ArrayDeclaration(ARRAY_SIZE, enumDec)

        sDec.addField(INT, byteDec)
        bb.put(TEST_NUMBER)

        sDec.addField(ARRAY_INT, arrIntDec)
        for (i in 0 until ARRAY_SIZE) {
            bb.put(TEST_NUMBER)
        }

        sDec.addField(LEN, byteDec)
        bb.put(TEST_NUMBER)

        sDec.addField(FLOAT, flDec)
        bb.putFloat(TEST_NUMBER.toFloat())

        sDec.addField(ARRAY_FLOAT, arrFloatDec)
        for (i in 0 until ARRAY_SIZE) {
            bb.putFloat(TEST_NUMBER.toFloat())
        }

        sDec.addField(STR, strDec)
        bb.put(testStringBytes)
        bb.put(0.toByte())

        sDec.addField(ARRAY_STR, arrStrDec)
        for (i in 0 until ARRAY_SIZE) {
            bb.put(testStringBytes)
            bb.put(0.toByte())
        }

        sDec.addField(SEQ, seqDec)
        bb.put(TEST_NUMBER)
        bb.put(TEST_NUMBER)

        structDec.addField(STR, strDec)
        structDec.addField(INT, byteDec)
        sDec.addField(STRUCT, structDec)
        bb.put(testStringBytes)
        bb.put(0.toByte())
        bb.put(TEST_NUMBER)

        sDec.addField(ARRAY_STRUCT, arrStructDec)
        for (i in 0 until ARRAY_SIZE) {
            bb.put(testStringBytes)
            bb.put(0.toByte())
            bb.put(TEST_NUMBER)
        }

        enumDec.add(0, 1, LEN)
        enumDec.add(2, 3, FLOAT)
        sDec.addField(ENUM, enumDec)
        bb.put(TEST_NUMBER)

        sDec.addField(ARRAY_ENUM, arrEnumDec)
        for (i in 0 until ARRAY_SIZE) {
            bb.put(TEST_NUMBER)
        }

        varDec.addField(LEN, byteDec)
        varDec.addField(FLOAT, flDec)
        varDec.tag = ENUM
        sDec.addField(VARIANT, varDec)
        bb.putFloat(TEST_NUMBER.toFloat())

        sDec.addField(ARRAY_VARIANT, arrVariantDec)
        for (i in 0 until ARRAY_SIZE) {
            bb.putFloat(TEST_NUMBER.toFloat())
        }

        fixture = sDec.createDefinition(null, ROOT, BitBuffer(bb))

    }

    /**
     * Run the CtfTmfEventField parseField(Definition,String) method test.
     */
    @Test
    fun testParseFieldFloat() {
        val fieldDef = fixture.lookupDefinition(FLOAT) as FloatDefinition
        val result = CtfTraceEventFieldParser.parseField(fieldDef)
        assertEquals("2.0", result.toString())
    }

    /**
     * Run the CtfTmfEventField parseField(Definition,String) method test for an
     * array of floats field.
     */
    @Test
    fun testParseFieldArrayFloat() {
        val fieldDef = fixture.lookupArrayDefinition(ARRAY_FLOAT)!!
        val result = CtfTraceEventFieldParser.parseField(fieldDef)
        assertEquals("[2.0, 2.0]", result.toString())
    }

    /**
     * Run the CtfTmfEventField parseField(Definition,String) method test.
     */
    @Test
    fun testParseFieldInt() {
        val fieldDef = fixture.lookupDefinition(INT)
        val result = CtfTraceEventFieldParser.parseField(fieldDef)
        assertEquals("2", result.toString())
    }

    /**
     * Run the CtfTmfEventField parseField(Definition,String) method test for an
     * array of integers field.
     */
    @Test
    fun testParseFieldArrayInt() {
        val fieldDef = fixture.lookupArrayDefinition(ARRAY_INT)!!
        val result = CtfTraceEventFieldParser.parseField(fieldDef)
        assertEquals("[2, 2]", result.toString())
    }

    /**
     * Run the CtfTmfEventField parseField(Definition,String) method test.
     */
    @Test
    fun testParseFieldSequence() {
        val fieldDef = fixture.lookupDefinition(SEQ)
        val result = CtfTraceEventFieldParser.parseField(fieldDef)
        assertEquals("[2, 2]", result.toString())
    }

    /**
     * Run the CtfTmfEventField parseField(Definition,String) method test.
     */
    @Test
    fun testParseFieldSequenceValue() {
        val fieldDef = fixture.lookupDefinition(SEQ)
        @SuppressWarnings("unchecked")
        val result = CtfTraceEventFieldParser.parseField(fieldDef).asType<ArrayValue<IntegerValue>>()!!
        val values = (0 until result.size)
                .map { result.getElement(it) }
                .map { it.value }
                .toLongArray()
        val expected = longArrayOf(2, 2)
        assertArrayEquals(expected, values)
    }

    /**
     * Run the CtfTmfEventField parseField(Definition,String) method test.
     */
    @Test
    fun testParseFieldString() {
        val fieldDef = fixture.lookupDefinition(STR)
        val result = CtfTraceEventFieldParser.parseField(fieldDef)
        assertEquals("two", result.toString())
    }

    /**
     * Run the CtfTmfEventField parseField(Definition,String) method test for an
     * array of strings field.
     */
    @Test
    fun testParseFieldArrayString() {
        val fieldDef = fixture.lookupArrayDefinition(ARRAY_STR)!!
        val result = CtfTraceEventFieldParser.parseField(fieldDef)
        assertEquals("[two, two]", result.toString())
    }

    /**
     * Run the CtfTmfEventField parseField(Definition,String) method test.
     */
    @Test
    fun testParseFieldStruct() {
        val fieldDef = fixture.lookupDefinition(STRUCT)
        val result = CtfTraceEventFieldParser.parseField(fieldDef)
        assertEquals("{str=two, int=2}", result.toString())
    }

    /**
     * Run the CtfTmfEventField parseField(Definition,String) method test for an
     * array of structs field.
     */
    @Test
    fun testParseFieldArrayStruct() {
        val fieldDef = fixture.lookupArrayDefinition(ARRAY_STRUCT)!!
        val result = CtfTraceEventFieldParser.parseField(fieldDef)
        assertEquals("[{str=two, int=2}, {str=two, int=2}]", result.toString())
    }

    /**
     * Run the CtfTmfEventField parseField(Definition,String) method test.
     */
    @Test
    fun testParseFieldEnum() {
        val fieldDef = fixture.lookupDefinition(ENUM)
        val result = CtfTraceEventFieldParser.parseField(fieldDef)
        assertEquals("float(2)", result.toString())
    }

    /**
     * Run the CtfTmfEventField parseField(Definition,String) method test for an
     * array of enums field.
     */
    @Test
    fun testParseFieldArrayEnum() {
        val fieldDef = fixture.lookupArrayDefinition(ARRAY_ENUM)!!
        val result = CtfTraceEventFieldParser.parseField(fieldDef)
        assertEquals("[float(2), float(2)]", result.toString())
    }

    /**
     * Run the CtfTmfEventField parseField(Definition,String) method test.
     */
    @Test
    fun testParseFieldVariant() {
        val fieldDef = fixture.lookupDefinition(VARIANT)
        val result = CtfTraceEventFieldParser.parseField(fieldDef)
        assertEquals("2.0", result.toString())
    }

    /**
     * Run the CtfTmfEventField parseField(Definition,String) method test for an
     * array of variants field.
     */
    @Test
    fun testParseFieldArrayVariant() {
        val fieldDef = fixture.lookupArrayDefinition(ARRAY_VARIANT)!!
        val result = CtfTraceEventFieldParser.parseField(fieldDef)
        assertEquals("[2.0, 2.0]", result.toString())
    }

}
