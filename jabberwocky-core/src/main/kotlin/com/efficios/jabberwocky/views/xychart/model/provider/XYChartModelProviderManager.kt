/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.efficios.jabberwocky.views.xychart.model.provider

object XYChartModelProviderManager {

    interface XYChartModelProviderFactory : () -> XYChartModelProvider

    interface XYChartOutput {
        fun providerRegistered(factory: XYChartModelProviderFactory)
    }

    private val registeredProviderFactories = mutableSetOf<XYChartModelProviderFactory>()
    private val registeredOutputs = mutableSetOf<XYChartOutput>()

    /**
     * Register a time graph provider by specifying its
     * {@link ITimeGraphModelProviderFactory}. If a factory is already registered,
     * this method will have no effect.
     *
     * @param factory
     *            The provider's factory
     */
    @Synchronized
    fun registerProviderFactory(factory: XYChartModelProviderFactory) {
        val added = registeredProviderFactories.add(factory)
        if (added) {
            registeredOutputs.forEach { it.providerRegistered(factory) }
        }
    }

    /**
     * Register a time graph output to this manager. Upon registration,
     * notifications will be sent, using {@link TimeGraphOutput#providerRegistered},
     * for all existing providers. Additional notifications will be sent for future
     * registered providers.
     *
     * @param output
     *            The time graph output to register
     */
    @Synchronized
    fun registerOutput(output: XYChartOutput) {
        /* Send notifications for currently-registered factories */
        registeredProviderFactories.forEach { factory -> output.providerRegistered(factory) }
        registeredOutputs.add(output)
    }

    /**
     * Unregister a previously-registered output, so that it does not receive any
     * more notifications. Has no effect if the output was not already registered.
     *
     * @param output
     *            The output to unregister
     */
    @Synchronized
    fun unregisterOutput(output: XYChartOutput) {
        registeredOutputs.remove(output)
    }

}
