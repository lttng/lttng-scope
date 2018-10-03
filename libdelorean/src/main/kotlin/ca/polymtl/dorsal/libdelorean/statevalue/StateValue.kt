/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package ca.polymtl.dorsal.libdelorean.statevalue

sealed class StateValue(val isNull: Boolean = false) {

    companion object {
        private const val CACHE_SIZE = 128
        private const val STRING_CACHE_SIZE = 256
        private val INT_CACHE = arrayOfNulls<IntegerStateValue>(CACHE_SIZE)
        private val LONG_CACHE = arrayOfNulls<LongStateValue>(CACHE_SIZE)
        private val DOUBLE_CACHE = arrayOfNulls<DoubleStateValue>(CACHE_SIZE)
        private val STRING_CACHE = arrayOfNulls<StringStateValue>(STRING_CACHE_SIZE)

        private val NULL_VALUE = NullStateValue()
        private val BOOLEAN_VALUE_TRUE = BooleanStateValue(true)
        private val BOOLEAN_VALUE_FALSE = BooleanStateValue(false)

        @JvmStatic
        fun nullValue(): StateValue = NULL_VALUE

        @JvmStatic
        fun newValueBoolean(boolValue: Boolean): StateValue =
                if (boolValue) BOOLEAN_VALUE_TRUE else BOOLEAN_VALUE_FALSE

        @JvmStatic
        fun newValueInt(intValue: Int): StateValue {
            /* Lookup in cache for the existence of the same value. */
            val offset = intValue and (CACHE_SIZE - 1)
            val cached = INT_CACHE[offset]
            if (cached != null && cached.value == intValue) {
                return cached
            }

            /* Not in cache, create a new value and cache it. */
            val newValue = IntegerStateValue(intValue)
            INT_CACHE[offset] = newValue
            return newValue
        }

        @JvmStatic
        fun newValueLong(longValue: Long): StateValue {
            /* Lookup in cache for the existence of the same value. */
            val offset = (longValue and (CACHE_SIZE - 1L)).toInt()
            val cached = LONG_CACHE[offset]
            if (cached != null && cached.value == longValue) {
                return cached
            }

            /* Not in cache, create a new value and cache it. */
            val newValue = LongStateValue(longValue)
            LONG_CACHE[offset] = newValue
            return newValue
        }

        @JvmStatic
        fun newValueDouble(doubleValue: Double): StateValue {
            /* Lookup in cache for the existence of the same value. */
            val offset = (java.lang.Double.doubleToLongBits(doubleValue) and (CACHE_SIZE - 1L)).toInt()
            val cached = DOUBLE_CACHE[offset]

            /*
             * We're using Double.compareTo() instead of .equals(), because .compare()
             * works when both values are Double.NaN.
             */
            if (cached != null && (cached.value.compareTo(doubleValue) == 0)) {
                return cached
            }

            /* Not in cache, create a new value and cache it. */
            val newValue = DoubleStateValue(doubleValue)
            DOUBLE_CACHE[offset] = newValue
            return newValue
        }

        @JvmStatic
        fun newValueString(strValue: String?): StateValue {
            if (strValue == null) {
                return nullValue()
            }
            /* Check if this string is in the cache */
            val offset = strValue.hashCode() and (STRING_CACHE_SIZE - 1)
            val cached = STRING_CACHE[offset]
            if (cached != null && cached.value == strValue) {
                return cached
            }

            /*
             * Make sure the String does not contain "weird" things, like ISO
             * control characters.
             */
            if (strValue.toCharArray().any { Character.isISOControl(it) }) {
                throw IllegalArgumentException("Trying to use invalid string: $strValue")
            }
            val newValue = StringStateValue(strValue)
            STRING_CACHE[offset] = newValue
            return newValue
        }

    }

}

class NullStateValue internal constructor() : StateValue(true) {
    override fun hashCode() = 0
    override fun equals(other: Any?) = (other is NullStateValue)
    override fun toString() = "nullValue"
}

data class BooleanStateValue internal constructor(val value: Boolean) : StateValue()
data class IntegerStateValue internal constructor(val value: Int) : StateValue()
data class LongStateValue internal constructor(val value: Long) : StateValue()
data class DoubleStateValue internal constructor(val value: Double) : StateValue()
data class StringStateValue internal constructor(val value: String) : StateValue()