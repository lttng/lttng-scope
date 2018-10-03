/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.efficios.jabberwocky.trace.event

import java.util.*

sealed class FieldValue(open val attributes: Map<String, String>?) {

    /**
     * Utility method to avoid having to add parentheses around checked casts.
     * For example you can use either
     *
     * (event.fields["field_1"] as? IntegerValue)!!.value
     *
     * or
     *
     * event.fields["field_1"]?.asType<IntegerValue>()!!.value
     *
     */
    inline fun <reified T : FieldValue> asType(): T? = this as? T


    data class ArrayValue<T : FieldValue>(val elements: Array<T>,
                                          override val attributes: Map<String, String>? = null) : FieldValue(attributes) {
        val size: Int = elements.size

        fun getElement(index: Int): T {
            return elements[index]
        }

        override fun hashCode() = Objects.hash(elements)

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other?.javaClass != javaClass) return false

            other as ArrayValue<*>

            if (!(elements contentDeepEquals other.elements)) return false

            return true
        }

        override fun toString() = elements.contentDeepToString()
    }

    data class EnumValue(val stringValue: String,
                         val longValue: Long,
                         override val attributes: Map<String, String>? = null) : FieldValue(attributes) {

        override fun toString() = stringValue + '(' + longValue.toString() + ')'
    }

    data class FloatValue(val value: Double,
                          override val attributes: Map<String, String>? = null) : FieldValue(attributes) {

        override fun toString() = value.toString()
    }

    data class IntegerValue(val value: Long,
                            val base: Int = 10,
                            override val attributes: Map<String, String>? = null) : FieldValue(attributes) {

        override fun toString() = if (base == 16) {
            "0x" + java.lang.Long.toHexString(value)
        } else {
            value.toString()
        }
    }

    data class StringValue(val value: String,
                           override val attributes: Map<String, String>? = null) : FieldValue(attributes) {

        override fun toString() = value
    }

    data class StructValue(val elements: Map<String, FieldValue>,
                           override val attributes: Map<String, String>? = null) : FieldValue(attributes) {

        override fun toString() = elements.toString()
    }

}
