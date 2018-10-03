/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 * Copyright (C) 2012-2014 Ericsson
 * Copyright (C) 2010-2011 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package ca.polymtl.dorsal.libdelorean.backend

import ca.polymtl.dorsal.libdelorean.interval.StateInterval
import ca.polymtl.dorsal.libdelorean.statevalue.StateValue
import java.io.File
import java.io.FileInputStream

interface IStateHistoryBackend {

    /** The ID of the state system that populates this backend. */
    val SSID: String

    /**
     * The start time of this state history. This is usually the same as the
     * start time of the originating trace.
     */
    val startTime: Long

    /**
     * The current end time of the state history. It will change as the
     * history is being built.
     */
    val endTime: Long

    /**
     * Main method to insert state intervals into the history
     */
    fun insertPastState(stateStartTime: Long,
                        stateEndTime: Long,
                        quark: Int,
                        value: StateValue)

    /**
     * Indicate to the provider that we are done building the history (so it can
     * close off, stop threads, etc.)
     *
     * @param endTime
     *            The end time to assign to this state history. It could be
     *            farther in time than the last state inserted, for example.
     */
    fun finishBuilding(endTime: Long)

    /**
     * It is the responsibility of the backend to define where to save the
     * Attribute Tree (since it's only useful to "reopen" an Attribute Tree if
     * we have the matching History).
     *
     * This method defines where to read for the attribute tree when opening an
     * already-existing history. Refer to the file format documentation.
     *
     * @return A FileInputStream object pointing to the correct file/location in
     *         the file where to read the attribute tree information.
     */
     fun supplyAttributeTreeReader() : FileInputStream?

    // FIXME change to FOS too?
    /**
     * Supply the File object to which we will write the attribute tree. The
     * position in this file is supplied by -TreeWriterFilePosition.
     */
    fun supplyAttributeTreeWriterFile() : File?

    /**
     * Supply the position in the file where we should write the attribute tree
     * when asked to.
     */
    fun supplyAttributeTreeWriterFilePosition() : Long

    /**
     * Delete any generated files or anything that might have been created by
     * the history backend (either temporary or save files). By calling this, we
     * return to the state as it was before ever building the history.
     *
     * You might not want to call automatically if, for example, you want an
     * index file to persist on disk. This could be limited to actions
     * originating from the user.
     */
    fun removeFiles()

    /**
     * Notify the state history back-end that the trace is being closed, so it
     * should release its file descriptors, close its connections, etc.
     */
    fun dispose()

    // ------------------------------------------------------------------------
    // Query methods
    // ------------------------------------------------------------------------

    /**
     * Complete "give me the state at a given time" method 'currentStateInfo' is
     * an "out" parameter, that is, write to it the needed information and
     * return. DO NOT 'new' currentStateInfo, it will be lost and nothing will
     * be returned!
     *
     * @param currentStateInfo
     *            List of StateValues (index == quark) to fill up
     * @param t
     *            Target timestamp of the query
     */
    fun doQuery(stateInfo: MutableList<StateInterval?>, t: Long)

    /**
     * Some providers might want to specify a different way to obtain just a
     * single StateValue instead of updating the whole list. If the method to
     * use is the same, then feel free to just implement this as a wrapper using
     * doQuery().
     *
     * @param t
     *            The target timestamp of the query.
     * @param attributeQuark
     *            The single attribute for which you want the state interval
     * @return The state interval matching this timestamp/attribute pair
     */
     fun doSingularQuery(t: Long, attributeQuark: Int): StateInterval?

    /**
     * Do a query for the specified quarks only. The results will be inserted
     * into the 'results' map passed in parameter.
     *
     * The default implementation simply does successive calls to
     * {@link #doSingularQuery}, but backends that might provide it more
     * efficiently are welcome to do so.
     *
     * @param t
     *            The timestamp of the query
     * @param quarks
     *            The quarks to query
     * @param results
     *            The results will be written in this map, with quarks as keys
     */
    fun doPartialQuery(t: Long,
                       quarks: Set<Int>,
                       results: MutableMap<Int, StateInterval>) {
        quarks.map { doSingularQuery(t, it) }
                .filterNotNull()
                .forEach { results.put(it.attribute, it) }

    }
}