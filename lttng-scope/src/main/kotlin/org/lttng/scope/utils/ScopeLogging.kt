/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.utils

import com.efficios.jabberwocky.utils.using
import java.util.logging.Logger
import kotlin.reflect.KClass
import kotlin.reflect.full.companionObject

/**
 * Wrapper for instantiating a JUL Logger object for a given class.
 *
 * Example usage:
 *
 *     class MyClass {
 *         companion object {
 *             private val LOGGER by logger()
 *         }
 *
 *         fun myfunc() {
 *             LOGGER.info { "log message" }
 *         }
 *     }
 *
 */
fun <R : Any> R.logger(): Lazy<Logger> {
    using {  }
    return lazy { Logger.getLogger(unwrapCompanionClass(this.javaClass).name) }
}

/* Unwrap companion object of enclosing Java Class */
private fun <T : Any> unwrapCompanionClass(ofClass: Class<T>): Class<*> {
    return if (ofClass.enclosingClass != null && ofClass.enclosingClass.kotlin.companionObject?.java == ofClass) {
        ofClass.enclosingClass
    } else {
        ofClass
    }
}

/* Unwrap companion object of enclosing Kotlin Class */
private fun <T : Any> unwrapCompanionClass(ofClass: KClass<T>): KClass<*> {
    return unwrapCompanionClass(ofClass.java).kotlin
}
