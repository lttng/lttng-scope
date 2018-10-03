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
 * Pseudo-try-with-resources. Taken from https://discuss.kotlinlang.org/t/kotlin-needs-try-with-resources/214/2
 *
 * Usage:
 *
 *     using {
 *       val closeable1 = MyObject1().autoClose()
 *       val closeable2 = MyObject2().autoClose()
 *       ...
 *     }
 *
 * All variables declared with `.autoClose()` will be `close()`'d upon exiting the `using{}` block.
 */

class ResourceHolder : AutoCloseable {

    private val resources = arrayListOf<AutoCloseable>()

    fun <T : AutoCloseable> T.autoClose(): T {
        resources.add(this)
        return this
    }

    override fun close() {
        resources.reversed().forEach { it.close() }
    }
}


fun <R> using(block: ResourceHolder.() -> R): R {
    val holder = ResourceHolder()
    try {
        return holder.block()
    } finally {
        holder.close()
    }
}
