package com.efficios.jabberwocky.ctf.trace

import com.efficios.jabberwocky.trace.TraceInitializationException
import org.lttng.scope.ttt.ctf.CtfTestTrace
import java.nio.file.Paths

internal class ExtractedCtfTestTrace(private val testTrace: CtfTestTrace) : AutoCloseable {

    private val testTraceExtractor: CtfTestTraceExtractor = CtfTestTraceExtractor.extractTestTrace(testTrace)
    val trace: CtfTrace

    init {
        val tracePath = testTraceExtractor.trace.path
        try {
            trace = CtfTrace(Paths.get(tracePath))
        } catch (e: TraceInitializationException) {
            throw IllegalArgumentException(e)
        }
    }

    override fun close() {
        testTraceExtractor.close()
    }

}
