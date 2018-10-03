/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.efficios.jabberwocky.analysis.statesystem

import ca.polymtl.dorsal.libdelorean.IStateSystemReader
import ca.polymtl.dorsal.libdelorean.IStateSystemWriter
import ca.polymtl.dorsal.libdelorean.StateSystemFactory
import ca.polymtl.dorsal.libdelorean.backend.StateHistoryBackendFactory
import com.efficios.jabberwocky.analysis.IAnalysis
import com.efficios.jabberwocky.analysis.IAnalysis.Companion.ANALYSES_DIRECTORY
import com.efficios.jabberwocky.collection.TraceCollection
import com.efficios.jabberwocky.common.TimeRange
import com.efficios.jabberwocky.project.TraceProject
import com.efficios.jabberwocky.task.JabberwockyTask
import com.efficios.jabberwocky.trace.event.TraceEvent
import java.io.IOException
import java.nio.file.Files

abstract class StateSystemAnalysis : IAnalysis {

    companion object {
        private const val HISTORY_FILE_EXTENSION = ".ht"
    }

    final override fun execute(project: TraceProject<*, *>, range: TimeRange?, extraParams: String?): IStateSystemReader {
        if (range != null) throw UnsupportedOperationException("Partial ranges for state system analysis not yet implemented")
//        if (extraParams != null) logWarning("Ignoring extra parameters: $extraParams")

        /* Wrap this in an in-band JabberwockyTask so that progress can be reported. */
        val task = JabberwockyTask<IStateSystemReader>("Processing states for analysis ${this.javaClass.simpleName}") {
            /* Determine the path of the history tree backing file we expect */
            val analysisClassName = javaClass.toString()
            val analysesDirectory = project.directory.resolve(ANALYSES_DIRECTORY)
            if (!Files.exists(analysesDirectory)) Files.createDirectory(analysesDirectory)

            val stateSystemFile = analysesDirectory.resolve(analysisClassName + HISTORY_FILE_EXTENSION)
            var newFile = !Files.exists(stateSystemFile)

            /* Create the history tree backend we will use */
            val htBackend = if (Files.exists(stateSystemFile)) {
                try {
                    StateHistoryBackendFactory.createHistoryTreeBackendExistingFile(analysisClassName, stateSystemFile.toFile(), providerVersion)
                } catch (e: IOException) {
                    /* The expected provider version may not match what we have on disk. Try building the file from scratch instead */
                    newFile = true
                    StateHistoryBackendFactory.createHistoryTreeBackendNewFile(analysisClassName, stateSystemFile.toFile(), providerVersion, project.startTime)
                }
            } else {
                StateHistoryBackendFactory.createHistoryTreeBackendNewFile(analysisClassName, stateSystemFile.toFile(), providerVersion, project.startTime)
            }

            val ss = StateSystemFactory.newStateSystem(htBackend, newFile)
            /* If there was a history file already built, it should be good to go. If not, build it */
            if (newFile) buildForProject(project, ss)
            ss
        }
        task.run()
        return task.get()
    }

    private fun buildForProject(project: TraceProject<*, *>, stateSystem: IStateSystemWriter) {
        val traces = filterTraces(project)
        val trackedState = trackedState()
        // TODO This iteration could eventually move to a central location, so that the events are
        // read once then dispatched to several "state providers".
        // However some analyses may not need all events from all traces in a project. We'll see...
        var latestTimestamp = project.startTime
        traces.iterator().use {
            while (it.hasNext()) {
                val event = it.next()
                handleEvent(stateSystem, event, trackedState)
                latestTimestamp = event.timestamp
            }
        }
        stateSystem.closeHistory(latestTimestamp)
    }

    protected abstract val providerVersion: Int

    protected abstract fun filterTraces(project: TraceProject<*, *>): TraceCollection<*, *>

    /**
     * Override this to specify tracked state objects. This exact array
     * will be passed to each call to handleEvent(), so the implementation
     * can use it to track state between every call.
     */
    protected open fun trackedState(): Array<Any>? = null

    protected abstract fun handleEvent(ss: IStateSystemWriter, event: TraceEvent, trackedState: Array<Any>?)

}