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

package ca.polymtl.dorsal.libdelorean.backend.historytree

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.google.common.cache.LoadingCache
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.nio.channels.ClosedChannelException
import java.nio.channels.FileChannel
import java.util.concurrent.ExecutionException
import java.util.logging.Level
import java.util.logging.Logger

/**
 * This class abstracts inputs/outputs of the HistoryTree nodes.
 *
 * It contains all the methods and descriptors to handle reading/writing nodes
 * to the tree-file on disk and all the caching mechanisms.
 *
 * This abstraction is mainly for code isolation/clarification purposes. Every
 * HistoryTree must contain 1 and only 1 HT_IO element.
 *
 * @author Alexandre Montplaisir
 *
 * @param stateFile
 *            The name of the history file
 * @param blockSize
 *            The size of each "block" on disk. One node will always fit in
 *            one block.
 * @param maxChildren
 *            The maximum number of children allowed per core (non-leaf)
 *            node.
 * @param newFile
 *            Flag indicating that the file must be created from scratch
 * @throws IOException
 *             An exception can be thrown when file cannot be accessed
 */
internal class HT_IO(private val stateFile: File,
                     private val blockSize: Int,
                     private val maxChildren: Int,
                     newFile: Boolean) {

    companion object {
        private val LOGGER = Logger.getLogger(HT_IO::class.java.name)

        // TODO test/benchmark optimal cache size
        private const val CACHE_SIZE = 256

        private data class CacheKey(val stateHistory: HT_IO, val seqNumber: Int)

        private val NODE_CACHE: LoadingCache<CacheKey, HistoryTreeNode> = CacheBuilder.newBuilder()
                .maximumSize(CACHE_SIZE.toLong())
                .build(object : CacheLoader<CacheKey, HistoryTreeNode>() {
                    override fun load(key: CacheKey): HistoryTreeNode {
                        val io = key.stateHistory
                        val seqNb = key.seqNumber

                        LOGGER.finest { "[HtIo:CacheMiss] seqNum=$seqNb" }

                        synchronized(io) {
                            io.seekFCToNodePos(io.fcIn, seqNb);
                            return HistoryTreeNode.readNode(io.blockSize, io.maxChildren, io.fcIn)
                        }
                    }
                })
    }

    /* Properties related to file I/O */
    private val fis: FileInputStream
    private val fos: FileOutputStream
    private val fcIn: FileChannel
    val fcOut: FileChannel

    init {
        if (newFile) {
            /* Create a new empty History Tree file */
            if (stateFile.exists()) {
                if (!stateFile.delete()) throw IOException("Cannot delete existing file $stateFile to replace it.")
            }
            if (!stateFile.createNewFile()) throw IOException("Cannot create new file $stateFile.")

            fis = FileInputStream(stateFile)
            fos = FileOutputStream(stateFile, false)
        } else {
            /*
             * We want to open an existing file, make sure we don't squash the
             * existing content when opening the fos!
             */
            fis = FileInputStream(stateFile)
            fos = FileOutputStream(stateFile, true)
        }
        fcIn = fis.channel
        fcOut = fos.channel
    }

    /**
     * Read a node from the file on disk.
     *
     * @param seqNumber
     *            The sequence number of the node to read.
     * @return The object representing the node
     * @throws ClosedChannelException
     *             Usually happens because the file was closed while we were
     *             reading. Instead of using a big reader-writer lock, we'll
     *             just catch this exception.
     */
    fun readNode(seqNumber: Int): HistoryTreeNode {
        /* Do a cache lookup. If it's not present it will be loaded from disk */
        LOGGER.finest { "[HtIo:CacheLookup] seqNum=$seqNumber" }
        val key = CacheKey(this, seqNumber);
        try {
            return NODE_CACHE.get(key)

        } catch (e: ExecutionException) {
            /* Get the inner exception that was generated */
            val cause = e.cause
            if (cause is ClosedChannelException) {
                throw cause
            }
            /* Other types of IOExceptions shouldn't happen at this point though. */
            throw IllegalStateException()
        }
    }

    fun writeNode(node: HistoryTreeNode) {
        try {
            val seqNumber = node.seqNumber

            /* "Write-back" the node into the cache */
            val key = CacheKey(this, seqNumber);
            NODE_CACHE.put(key, node);

            /* Position ourselves at the start of the node and write it */
            synchronized(this) {
                seekFCToNodePos(fcOut, seqNumber);
                node.writeSelf(fcOut);
            }

        } catch (e: IOException) {
            /* If we were able to open the file, we should be fine now... */
            throw IllegalStateException(e)
        }
    }

    fun supplyATReader(nodeOffset: Int): FileInputStream {
        try {
            /*
             * Position ourselves at the start of the Mapping section in the
             * file (which is right after the Blocks)
             */
            seekFCToNodePos(fcIn, nodeOffset)
        } catch (e: IOException) {
            LOGGER.log(Level.SEVERE, e.message, e)
        }
        return fis
    }

    @Synchronized
    fun closeFile() {
        try {
            fis.close()
            fos.close()
        } catch (e: IOException) {
            LOGGER.log(Level.SEVERE, e.message, e)
        }
    }

    @Synchronized
    fun deleteFile() {
        closeFile()

        if (!stateFile.delete()) {
            /* We didn't succeed in deleting the file */
            LOGGER.severe("Failed to delete" + stateFile.name)
        }
    }

    /**
     * Seek the given FileChannel to the position corresponding to the node that
     * has seqNumber
     *
     * @param fc
     *            the channel to seek
     * @param seqNumber
     *            the node sequence number to seek the channel to
     * @throws IOException
     *             If some other I/O error occurs
     */
    private fun seekFCToNodePos(fc: FileChannel, seqNumber: Int) {
        /*
         * Cast to (long) is needed to make sure the result is a long too and
         * doesn't get truncated
         */
        fc.position(HistoryTree.TREE_HEADER_SIZE
                + seqNumber.toLong() * blockSize)
    }
}