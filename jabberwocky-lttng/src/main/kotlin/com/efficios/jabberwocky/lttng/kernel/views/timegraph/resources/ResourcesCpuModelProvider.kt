/*
 * Copyright (C) 2018 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.efficios.jabberwocky.lttng.kernel.views.timegraph.resources

import com.efficios.jabberwocky.lttng.kernel.analysis.os.Attributes
import com.efficios.jabberwocky.lttng.kernel.views.timegraph.resources.elements.ResourcesCpuTreeElement
import com.efficios.jabberwocky.views.timegraph.model.render.tree.TimeGraphTreeElement
import com.efficios.jabberwocky.views.timegraph.model.render.tree.TimeGraphTreeRender

class ResourcesCpuModelProvider : ResourcesBaseModelProvider(PROVIDER_NAME, SS_TO_TREE_RENDER_FUNCTION) {

    companion object {
        private const val PROVIDER_NAME = "CPU"
        private val CPUS_QUARK_PATTERN = arrayOf(Attributes.CPUS, "*")

        /**
         * Get the tree element name for every cpu.
         */
        private val SS_TO_TREE_RENDER_FUNCTION = java.util.function.Function { treeContext: TreeRenderContext ->
            val ss = treeContext.ss
            val treeElems = ss.getQuarks(*CPUS_QUARK_PATTERN)
                    .map { cpuQuark ->
                        val cpu = ss.getAttributeName(cpuQuark).toIntOrNull() ?: return@map null
                        ResourcesCpuTreeElement(cpu, emptyList(), ss, cpuQuark)
                    }
                    .filterNotNull()
                    /* Sort entries according to their CPU number (not just an alphabetical sort!) */
                    .sortedBy { it.cpu }
                    .toList()

            val rootElement = TimeGraphTreeElement(treeContext.traceName, treeElems)
            return@Function TimeGraphTreeRender(rootElement)
        }
    }

}
