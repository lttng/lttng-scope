/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 * Copyright (C) 2012-2015 Ericsson
 * Copyright (C) 2010-2011 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package ca.polymtl.dorsal.libdelorean.backend.historytree

import ca.polymtl.dorsal.libdelorean.backend.IStateHistoryBackend
import ca.polymtl.dorsal.libdelorean.exceptions.StateSystemDisposedException
import ca.polymtl.dorsal.libdelorean.exceptions.TimeRangeException
import ca.polymtl.dorsal.libdelorean.interval.StateInterval
import ca.polymtl.dorsal.libdelorean.statevalue.StateValue
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.nio.channels.ClosedChannelException


/**
 * History Tree backend for storing a state history. This is the basic version
 * that runs in the same thread as the class creating it.
 *
 * @author Alexandre Montplaisir
 */
class HistoryTreeBackend private constructor(override val SSID: String,
                                             private val sht: HistoryTree,
                                             @Volatile private var isFinishedBuilding: Boolean): IStateHistoryBackend {

    /**
     * Constructor for new history files. Use this when creating a new history
     * from scratch.
     *
     * @param ssid
     *            The state system's ID
     * @param newStateFile
     *            The filename/location where to store the state history (Should
     *            end in .ht)
     * @param providerVersion
     *            Version of of the state provider. We will only try to reopen
     *            existing files if this version matches the one in the
     *            framework.
     * @param startTime
     *            The earliest time stamp that will be stored in the history
     * @param blockSize
     *            The size of the blocks in the history file. This should be a
     *            multiple of 4096.
     * @param maxChildren
     *            The maximum number of children each core node can have
     */
    constructor(ssid: String,
                newStateFile: File,
                providerVersion: Int,
                startTime: Long,
                blockSize: Int,
                maxChildren: Int) : this(ssid, HistoryTree(newStateFile, blockSize, maxChildren, providerVersion, startTime), false)

    /**
     * Constructor for new history files. Use this when creating a new history
     * from scratch. This version supplies sane defaults for the configuration
     * parameters.
     *
     * @param ssid
     *            The state system's id
     * @param newStateFile
     *            The filename/location where to store the state history (Should
     *            end in .ht)
     * @param providerVersion
     *            Version of of the state provider. We will only try to reopen
     *            existing files if this version matches the one in the
     *            framework.
     * @param startTime
     *            The earliest time stamp that will be stored in the history
     */
    constructor(ssid: String,
                newStateFile: File,
                providerVersion: Int,
                startTime: Long) : this(ssid, newStateFile, providerVersion, startTime, 64 * 1024, 50)

    /**
     * Existing history constructor. Use this to open an existing state-file.
     *
     * @param ssid
     *            The state system's id
     * @param existingStateFile
     *            Filename/location of the history we want to load
     * @param providerVersion
     *            Expected version of of the state provider plugin.
     * @throws IOException
     *             If we can't read the file, if it doesn't exist, is not
     *             recognized, or if the version of the file does not match the
     *             expected providerVersion.
     */
    constructor(ssid: String,
                existingStateFile: File,
                providerVersion: Int) : this(ssid, HistoryTree(existingStateFile, providerVersion), true)

    override val startTime get() = sht.treeStart
    override val endTime get() = sht.treeEnd

    val fileSize get() = sht.fileSize

    override fun insertPastState(stateStartTime: Long,
                                 stateEndTime: Long,
                                 quark: Int,
                                 value: StateValue) {
        val interval = HTInterval(stateStartTime, stateEndTime, quark, value)
        /* Start insertions at the "latest leaf" */
        sht.insertInterval(interval)
    }

    override fun finishBuilding(endTime: Long) {
        sht.closeTree(endTime)
        isFinishedBuilding = true
    }

    override fun supplyAttributeTreeReader(): FileInputStream = sht.supplyATReader()
    override fun supplyAttributeTreeWriterFile(): File = sht.supplyATWriterFile()
    override fun supplyAttributeTreeWriterFilePosition(): Long = sht.supplyATWriterFilePos()

    override fun removeFiles() = sht.deleteFile()

    override fun dispose() {
        if (isFinishedBuilding) {
            sht.closeFile()
        } else {
            /*
             * The build is being interrupted, delete the file we partially
             * built since it won't be complete, so shouldn't be re-used in the
             * future (.deleteFile() will close the file first)
             */
            sht.deleteFile()
        }
    }

    override fun doQuery(stateInfo: MutableList<StateInterval?>, t: Long) {
        checkValidTime(t)

        /* We start by reading the information in the root node */
        var currentNode = sht.rootNode
        currentNode.writeInfoFromNode(stateInfo, t)

        /* Then we follow the branch down in the relevant children */
        try {
            while (currentNode is CoreNode) {
                currentNode = sht.selectNextChild(currentNode, t)
                currentNode.writeInfoFromNode(stateInfo, t)
            }
        } catch (e: ClosedChannelException) {
            throw StateSystemDisposedException(e)
        }
    }

    override fun doSingularQuery(t: Long, attributeQuark: Int): HTInterval {
        checkValidTime(t)

        var currentNode = sht.rootNode
        var interval = currentNode.getRelevantInterval(attributeQuark, t)

        try {
            while (interval == null && currentNode is CoreNode) {
                currentNode = sht.selectNextChild(currentNode, t)
                interval = currentNode.getRelevantInterval(attributeQuark, t)
            }
        } catch (e: ClosedChannelException) {
            throw StateSystemDisposedException(e)
        }
        return requireNotNull(interval)
    }


    override fun doPartialQuery(t: Long, quarks: Set<Int>, results: MutableMap<Int, StateInterval>) {
        checkValidTime(t)
        var remaining = quarks.size

        /* We start by reading the information in the root node. */
        var currentNode = sht.rootNode
        synchronized(currentNode) {
            currentNode.intervalIterator(t, quarks)
                    .forEach {
                        results.put(it.attribute, it)
                        remaining--
                    }
        }

        /* Then we follow the branch down in the relevant children. */
        try {
            while (remaining > 0 && currentNode is CoreNode) {
                currentNode = sht.selectNextChild(currentNode, t)
                synchronized(currentNode) {
                    currentNode.intervalIterator(t, quarks)
                            .forEach {
                                results.put(it.attribute, it)
                                remaining--
                            }
                }
            }
        } catch (e: ClosedChannelException) {
            throw StateSystemDisposedException(e)
        }
    }

    private fun checkValidTime(t: Long) {
        if (t < startTime || t > endTime) {
            throw TimeRangeException(String.format("%s Time:%d, Start:%d, End:%d", SSID, t, startTime, endTime))
        }
    }

}
