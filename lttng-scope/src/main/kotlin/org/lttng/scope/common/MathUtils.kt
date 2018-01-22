/*
 * Copyright (C) 2017-2018 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.common

import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min

/**
 * Find the multiple of 'multipleOf' that is greater but closest to
 * 'this'. If 'this' is already a multiple of 'multipleOf', the same
 * value will be returned.
 *
 * @param multipleOf
 *            We want the returned value to be a multiple of this number
 * @return The closest, greater multiple
 */
fun Long.roundToClosestHigherMultiple(multipleOf: Long): Long {
    return (ceil(this.toDouble() / multipleOf) * multipleOf).toLong()
}

fun Long.clampMin(lowerLimit: Long): Long = max(this, lowerLimit)
fun Long.clampMax(upperLimit: Long): Long = min(this, upperLimit)
fun Long.clamp(lowerLimit: Long, upperLimit: Long): Long = max(lowerLimit, min(this, upperLimit))
