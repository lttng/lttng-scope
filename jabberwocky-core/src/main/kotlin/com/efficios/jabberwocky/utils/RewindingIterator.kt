/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.efficios.jabberwocky.utils

/**
 * Extension of the [Iterator] interface which supports going backwards, using [previous()]
 * and [hasPrevious()] functions.
 *
 * Unlike ListIterator it does not track its current index, since the iterated data structure
 * could be larger than Int.MAX_VALUE.
 */
interface RewindingIterator<out E> : Iterator<E> {

    fun hasPrevious(): Boolean

    fun previous(): E

}