/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.efficios.jabberwocky.views.common


data class ColorDefinition @JvmOverloads constructor(val red: Int,
                                                     val green: Int,
                                                     val blue: Int,
                                                     val alpha: Int = ColorDefinition.MAX) {

    companion object {
        const val MIN = 0
        const val MAX = 255

        private fun checkValue(param: Int) {
            if (param < MIN || param > MAX) throw IllegalArgumentException()
        }
    }

    init {
        checkValue(red)
        checkValue(green)
        checkValue(blue)
        checkValue(alpha)
    }
}