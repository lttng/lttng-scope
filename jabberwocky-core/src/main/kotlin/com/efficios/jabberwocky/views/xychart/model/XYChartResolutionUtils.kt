/*
 * Copyright (C) 2018 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.efficios.jabberwocky.views.xychart.model

import java.util.*

object XYChartResolutionUtils {

    val GRID_RESOLUTIONS: NavigableSet<Long> = TreeSet<Long>().apply {
        val seconds_mantissas = setOf(1L, 2L, 5L)
        val seconds_oom = generateSequence(1L) { it * 10 }.take(9).toSet()
        val list = seconds_mantissas.flatMap { m -> seconds_oom.map { o -> m * o } }
        list.forEach { this.add(it) }

        // seconds
        this.add(1 * 1000000000L)
        this.add(2 * 1000000000L)
        this.add(5 * 1000000000L)
        this.add(10 * 1000000000L)
        this.add(15 * 1000000000L)
        this.add(30 * 1000000000L)

        // minutes
        this.add(1 * 60 * 1000000000L)
        this.add(2 * 60 * 1000000000L)
        this.add(5 * 60 * 1000000000L)
        this.add(10 * 60 * 1000000000L)
        this.add(15 * 60 * 1000000000L)
        this.add(30 * 60 * 1000000000L)

        // hours
        this.add(1 * 60 * 60 * 1000000000L)
        this.add(2 * 60 * 60 * 1000000000L)
        this.add(3 * 60 * 60 * 1000000000L)
        this.add(6 * 60 * 60 * 1000000000L)
        this.add(12 * 60 * 60 * 1000000000L)

        // days
        this.add(1 * 24 * 60 * 60 * 1000000000L)
        this.add(2 * 24 * 60 * 60 * 1000000000L)
        this.add(7 * 24 * 60 * 60 * 1000000000L)
        this.add(14 * 24 * 60 * 60 * 1000000000L)
        this.add(30 * 24 * 60 * 60 * 1000000000L)
    }

}