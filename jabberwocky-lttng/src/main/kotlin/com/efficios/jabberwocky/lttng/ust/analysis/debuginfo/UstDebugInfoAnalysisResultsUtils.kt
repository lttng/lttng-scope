/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.efficios.jabberwocky.lttng.ust.analysis.debuginfo

import com.efficios.jabberwocky.ctf.trace.CtfTrace
import com.efficios.jabberwocky.lttng.ust.trace.getUstEventLayout
import com.efficios.jabberwocky.trace.event.FieldValue
import com.efficios.jabberwocky.trace.event.TraceEvent

/**
 * Extensions functions for UstDebugInfoAnalysisResults. To merge into that
 * class once it moves to the Kotlin package.
 */

fun UstDebugInfoAnalysisResults.getCallsiteOfEvent(event: TraceEvent): BinaryCallsite? {
    /* This aspect only supports UST traces */
    val trace = event.trace as? CtfTrace ?: return null
    val layout = trace.getUstEventLayout() ?: return null

    /* We need both the vpid and ip contexts */
    val vpid = event.fields[layout.contextVpid()]?.asType<FieldValue.IntegerValue>()?.value ?: return null
    val ip = event.fields[layout.contextIp()]?.asType<FieldValue.IntegerValue>()?.value ?: return null
    val ts = event.timestamp

    return getBinaryCallsite(vpid, ts, ip)
}

/**
 * Get the binary callsite (which means binary file and offset in this file)
 * corresponding to the given instruction pointer, for the given PID and
 * timetamp.
 *
 * @param pid
 *            The PID for which we want the symbol
 * @param ts
 *            The timestamp of the query
 * @param ip
 *            The instruction pointer address
 * @return The {@link BinaryCallsite} object with the relevant information
 */
fun UstDebugInfoAnalysisResults.getBinaryCallsite(pid: Long, ts: Long, ip: Long): BinaryCallsite? {
    val file = getMatchingFile(ts, pid, ip) ?: return null

    // TODO If path prefixes or other conversion had to be done it would be done here
    // String fullPath = (trace.getSymbolProviderConfig().getActualRootDirPath() + file.getFilePath());
    val fullPath = file.filePath

    val offset = if (file.isPic) {
        ip - file.baseAddress
    } else {
        /*
         * In the case of the object being non-position-independent, we
         * must pass the actual 'ip' address directly to addr2line.
         */
        ip
    }
    return BinaryCallsite(fullPath, file.buildId, offset, file.isPic)
}
