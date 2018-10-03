/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.efficios.jabberwocky.ctf.trace.event

import com.efficios.jabberwocky.trace.event.FieldValue
import com.efficios.jabberwocky.trace.event.FieldValue.*
import org.eclipse.tracecompass.ctf.core.event.types.*

object CtfTraceEventFieldParser {

    fun parseField(fieldDef: IDefinition): FieldValue = when (fieldDef) {

        is IntegerDefinition -> IntegerValue(fieldDef.value, fieldDef.declaration.base)
        is FloatDefinition -> FloatValue(fieldDef.value)
        is StringDefinition -> StringValue(fieldDef.value)

        is AbstractArrayDefinition -> {
            val decl = fieldDef.declaration as? CompoundDeclaration ?: throw IllegalArgumentException("Array definitions should only come from sequence or array declarations") //$NON-NLS-1$
            val elemType = decl.elementType
            if (elemType is IntegerDeclaration) {
                /*
                 * Array of integers => either an array of integers values or a
                 * string.
                 */
                /* Are the integers characters and encoded? */
                when {
                    elemType.isCharacter -> StringValue(fieldDef.toString()) /* it's a banal string */
                    fieldDef is ByteArrayDefinition -> {
                        /*
                         * Unsigned byte array, consider this field an array of integers
                         */
                        val elements = (0 until fieldDef.length)
                                .map { idx -> java.lang.Byte.toUnsignedLong(fieldDef.getByte(idx)) }
                                .map { longVal -> IntegerValue(longVal, elemType.base) }
                                .toTypedArray()
                        ArrayValue(elements)

                    }
                    else -> {
                        /* Consider this a straight array of integers */
                        val elements = fieldDef.definitions
                                .filterNotNull()
                                .map { elem ->
                                    val integerDef = elem as IntegerDefinition
                                    val value = integerDef.value
                                    val base = integerDef.declaration.base
                                    IntegerValue(value, base)
                                }
                                .toTypedArray()
                        ArrayValue(elements)
                    }
                }

            } else {
                /* Arrays of elements of any other type */
                val elements = fieldDef.definitions
                        .map { parseField(it) }
                        .toTypedArray()
                ArrayValue(elements)
            }
        }

        is ICompositeDefinition -> {
            /*
             * Recursively parse the fields, and save the results in a struct value.
             */
            val structElements = fieldDef.fieldNames.associateBy({ it }, { parseField(fieldDef.getDefinition(it)) })
            StructValue(structElements)
        }

        is EnumDefinition -> EnumValue(fieldDef.value, fieldDef.integerValue)
        is VariantDefinition -> parseField(fieldDef.currentField)

        else -> throw IllegalArgumentException("Field type: " + fieldDef.javaClass.toString()
                    + " is not supported.")
    }

}
