/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package ca.polymtl.dorsal.libdelorean.backend.historytree

import ca.polymtl.dorsal.libdelorean.interval.StateInterval
import ca.polymtl.dorsal.libdelorean.statevalue.StateValue
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel

/**
 * <pre>
 *  1 - byte (type)
 * 16 - 2x long (start time, end time)
 * 16 - 4x int (seq number, parent seq number, intervalcount,
 *              strings section pos.)
 *  1 - byte (done or not)
 * </pre>
 */
private const val COMMON_HEADER_SIZE = 34

/**
 * The base class for all the types of nodes that go in the History Tree.
 *
 * @author Alexandre Montplaisir
 */
sealed class HistoryTreeNode(val blockSize: Int,
                             val seqNumber: Int,
                             var parentSeqNumber: Int,
                             val nodeStart: Long) {

    var nodeEnd: Long? = null
        private set

    /* Sum of bytes of all intervals in the node */
    private var sizeOfIntervalSection = 0

    /* True if this node was read from disk (meaning its end time is now fixed) */
    @Volatile
    var isOnDisk = false
        private set

    /* Vector containing all the intervals contained in this node */
    private var intervals: MutableList<HTInterval>? = mutableListOf()

    private var nodePayload: ByteBuffer? = null
    private var intervalCount: Int? = null

    fun intervalIterator(targetTimestamp: Long,
                         targetQuarks: Set<Int>?): Iterator<HTInterval> {
        val intervals = intervals
        val nodePayload = nodePayload

        return if (intervals != null) {
            intervals.iterator().asSequence()
                    .filter { targetQuarks?.contains(it.attribute) ?: true}
                    .filter { it.intersects(targetTimestamp) }
                    .iterator()
        } else if (nodePayload != null) {
            RawIntervalIterator(nodePayload, intervalCount!!, targetTimestamp, targetQuarks)
        } else {
            throw IllegalStateException()
        }
    }

    companion object {
        /**
         * Reader factory method. Build a Node object (of the right type) by reading
         * a block in the file.
         *
         * @param blockSize
         *            The size of each "block" on disk. One node will always fit in
         *            one block.
         * @param maxChildren
         *            The maximum number of children allowed per core (non-leaf)
         *            node.
         * @param fc
         *            FileChannel to the history file, ALREADY SEEKED at the start
         *            of the node.
         * @return The node object
         * @throws IOException
         *             If there was an error reading from the file channel
         */
        @JvmStatic
        fun readNode(blockSize: Int, maxChildren: Int, fc: FileChannel): HistoryTreeNode {
            /* Absolute position in 'fc' of the start of this node */
            val nodeStartPos = fc.position()
            val buffer = fc.map(FileChannel.MapMode.READ_ONLY, nodeStartPos, blockSize.toLong());
            buffer.order(ByteOrder.LITTLE_ENDIAN)

            /* Read the common header part */
            val typeByte = buffer.get()
            val start = buffer.getLong()
            val end = buffer.getLong()
            val seqNb = buffer.getInt()
            val parentSeqNb = buffer.getInt()
            val intervalCount = buffer.getInt()

            /* Now the rest of the header depends on the node type */
            val newNode = when (typeByte) {
                CoreNode.CORE_TYPE_BYTE -> CoreNode(blockSize, maxChildren, seqNb, parentSeqNb, start)
                LeafNode.LEAF_TYPE_BYTE -> LeafNode(blockSize, seqNb, parentSeqNb, start)
                else -> throw IOException()
            }
            newNode.readSpecificHeader(buffer)

            /*
             * At this point, we should be done reading the header and 'buffer'
             * should only have the intervals left.
             *
             * The primary ctor initializes "intervals" and sets 'rawIntervals'
             * to null. Flip that around for nodes created through this factory
             * function.
             */
            newNode.nodePayload = buffer.slice().order(ByteOrder.LITTLE_ENDIAN)
            newNode.intervalCount = intervalCount
            newNode.intervals = null

            /* Assign the node's other information we have read previously */
            newNode.nodeEnd = end
            newNode.isOnDisk = true

            return newNode
        }
    }

    /**
     * Write this node to the given file channel.
     */
    @Synchronized
    fun writeSelf(fc: FileChannel) {
        /* We shouldn't writeSelf() a node that was read from disk */
        val intervals = intervals ?: throw IllegalStateException()

        val buffer = ByteBuffer.allocate(blockSize)
        buffer.order(ByteOrder.LITTLE_ENDIAN)
        buffer.clear()

        /* Write the common header part */
        buffer.put(nodeByte)
        buffer.putLong(nodeStart)
        buffer.putLong(nodeEnd ?: 0L)
        buffer.putInt(seqNumber)
        buffer.putInt(parentSeqNumber)
        buffer.putInt(intervals.size)

        /* Now call the inner method to write the specific header part */
        writeSpecificHeader(buffer)

        /* Back to us, we write the intervals */
        intervals.forEach { it.writeInterval(buffer) }

        /* Fill the rest of the block with zeroes. */
        while (buffer.position() < blockSize) {
            buffer.put(0.toByte())
        }

        /* Finally, write everything in the Buffer to disk */
        buffer.flip()
        val res = fc.write(buffer)
        if (res != blockSize) {
            throw IllegalStateException("Wrong size of block written: Actual: $res, Expected: $blockSize")
        }

        isOnDisk = true
    }

    /**
     * Add an interval to this node
     */
    @Synchronized
    fun addInterval(newInterval: HTInterval) {
        /* We shouldn't add to a node that was read from disk */
        val intervals = intervals ?: throw IllegalStateException()

        /* Just in case, should be checked before even calling this function */
        assert (newInterval.sizeOnDisk <= nodeFreeSpace)

        /* Find the insert position to keep the list sorted */
        var index = intervals.size
        while (index > 0 && newInterval.end < intervals[index - 1].end) {
            index--
        }

        intervals.add(index, newInterval)
        sizeOfIntervalSection += newInterval.sizeOnDisk
    }

    /**
     * We've received word from the containerTree that newest nodes now exist to
     * our right. (Puts isDone = true and sets the endtime)
     */
    @Synchronized
    fun closeThisNode(endTime: Long) {
        /* Should not be called on a node that was read from disk */
        val intervals = intervals ?: throw IllegalStateException()

        /**
         * FIXME: was assert (endtime >= fNodeStart); but that exception
         * is reached with an empty node that has start time endtime + 1
         */
//      if (endtime < fNodeStart) {
//          throw new IllegalArgumentException("Endtime " + endtime + " cannot be lower than start time " + fNodeStart);
//      }

        if (intervals.isNotEmpty()) {
            /*
             * Make sure there are no intervals in this node with their
             * EndTime > the one requested. Only need to check the last one
             * since they are sorted
             */
            if (endTime < intervals.last().end) {
                throw IllegalArgumentException("Closing end time should be greater than or equal to the end time of the intervals of this node")
            }
        }
        nodeEnd = endTime
    }

    /**
     * The method to fill up the stateInfo (passed on from the Current State
     * Tree when it does a query on the SHT). We'll replace the data in that
     * vector with whatever relevant we can find from this node
     *
     * @param stateInfo
     *            The same stateInfo that comes from SHT's doQuery()
     * @param t
     *            The timestamp for which the query is for. Only return
     *            intervals that intersect t.
     */
    @Synchronized
    fun writeInfoFromNode(stateInfo: MutableList<StateInterval?>, t: Long) {
        intervalIterator(t, null).forEach { stateInfo[it.attribute] = it }
    }

    /**
     * Get a single Interval from the information in this node If the
     * key/timestamp pair cannot be found, we return null.
     *
     * @param key
     *            The attribute quark to look for
     * @param t
     *            The timestamp
     * @return The Interval containing the information we want, or null if it
     *         wasn't found
     */
    @Synchronized
    fun getRelevantInterval(key: Int, t: Long): HTInterval? {
        return intervalIterator(t, setOf(key)).asSequence().firstOrNull()
    }

    val totalHeaderSize get() = COMMON_HEADER_SIZE + specificHeaderSize
    private val dataSectionEndOffset get() = totalHeaderSize + sizeOfIntervalSection

    val nodeFreeSpace: Int
        @Synchronized get() = (blockSize - dataSectionEndOffset)

    protected abstract val nodeByte: Byte
    protected abstract val specificHeaderSize: Int

    protected abstract fun readSpecificHeader(buffer: ByteBuffer)
    protected abstract fun writeSpecificHeader(buffer: ByteBuffer)
}



internal class CoreNode(blockSize: Int,
                        val maxChildren: Int,
                        seqNumber: Int,
                        parentSeqNumber: Int,
                        nodeStart: Long) : HistoryTreeNode(blockSize, seqNumber, parentSeqNumber, nodeStart) {

    companion object {
        const val CORE_TYPE_BYTE: Byte = 1
    }

    /** Nb. of children this node has */
    var nbChildren = 0
        private set

    /** Seq. numbers of the children nodes */
    private val children = IntArray(maxChildren)

    /** Start times of each of the children */
    private val childStart = LongArray(maxChildren)

    /** Seq number of this node's extension. -1 if none. Unused for now */
    private val extension = -1

    @Synchronized
    fun getChild(index: Int): Int = children[index]

    @Synchronized
    fun getLatestChild(): Int = children.last()

    @Synchronized
    fun getChildStart(index: Int): Long = childStart[index]

    @Synchronized
    fun getLatestChildStart(): Long = childStart.last()

    @Synchronized
    fun linkNewChild(childNode: HistoryTreeNode) {
        if (nbChildren >= maxChildren) throw IllegalStateException()
        children[nbChildren] = childNode.seqNumber
        childStart[nbChildren] = childNode.nodeStart
        nbChildren++
    }

    override val nodeByte = CORE_TYPE_BYTE
    override val specificHeaderSize: Int = (
            /* 1x int (extension node) */
            Integer.BYTES +
                    /* 1x int (nbChildren) */
                    Integer.BYTES +
                    /* MAX_NB * int ('children' table) */
                    Integer.BYTES * maxChildren +
                    /* MAX_NB * Timevalue ('childStart' table) */
                    java.lang.Long.BYTES * maxChildren)

    override fun readSpecificHeader(buffer: ByteBuffer) {
        /* Unused "extension", should be -1 */
        buffer.getInt()

        nbChildren = buffer.getInt()

        (0 until nbChildren).forEach { children[it] = buffer.getInt() }
        (nbChildren until maxChildren).forEach { buffer.getInt() }

        (0 until nbChildren).forEach { childStart[it] = buffer.getLong() }
        (nbChildren until maxChildren).forEach { buffer.getLong() }
    }

    override fun writeSpecificHeader(buffer: ByteBuffer) {
        buffer.putInt(extension)
        buffer.putInt(nbChildren)

        /* Write the "children's seq number" array */
        (0 until nbChildren).forEach { buffer.putInt(children[it]) }
        (nbChildren until maxChildren).forEach { buffer.putInt(0) }

        /* Write the "children's start times" array */
        (0 until nbChildren).forEach { buffer.putLong(childStart[it]) }
        (nbChildren until maxChildren).forEach { buffer.putLong(0L) }
    }

}

internal class LeafNode(blockSize: Int,
                       seqNumber: Int,
                       parentSeqNumber: Int,
                       nodeStart: Long) : HistoryTreeNode(blockSize, seqNumber, parentSeqNumber, nodeStart) {

    companion object {
        const val LEAF_TYPE_BYTE: Byte = 2
    }

    override val nodeByte = LEAF_TYPE_BYTE
    override val specificHeaderSize: Int = 0 /* Empty */

    override fun readSpecificHeader(buffer: ByteBuffer) {
        /* No specific header part */
    }

    override fun writeSpecificHeader(buffer: ByteBuffer) {
        /* No specific header part */
    }
}

private class RawIntervalIterator(private val bb: ByteBuffer,
                                  expectedIntervalCount: Int,
                                  private val targetTimestamp: Long,
                                  private val targetQuarks: Set<Int>?): AbstractIterator<HTInterval>() {

    companion object {
        /* 'Byte' equivalent for state values types */
        private const val TYPE_NULL: Byte          = -1
        private const val TYPE_INTEGER: Byte       = 0
        private const val TYPE_STRING: Byte        = 1
        private const val TYPE_LONG: Byte          = 2
        private const val TYPE_DOUBLE: Byte        = 3
        private const val TYPE_BOOLEAN_TRUE: Byte  = 4
        private const val TYPE_BOOLEAN_FALSE: Byte = 5
    }

    init {
        bb.position(0)
    }

    private var remaining = expectedIntervalCount

    override fun computeNext() {
        while (remaining > 0) {
            remaining--
            val interval = considerNextInterval() ?: continue
            return setNext(interval)
        }
        return done()
    }

    private fun considerNextInterval(): HTInterval? {
        /* Start reading the interval's data, and exit as soon as we find a non-matching condition. */
        val valueType: Byte = bb.get()

        val start = bb.getLong()
        if (targetTimestamp < start) {
            /* Skip over "end" and "quark" */
            bb.skip(java.lang.Long.BYTES + Integer.BYTES)

            skipPayload(valueType)
            return null
        }

        val end = bb.getLong()
        if (targetTimestamp > end) {
            /* Skip over "quark" */
            bb.skip(Integer.BYTES)

            skipPayload(valueType)
            return null
        }

        val quark = bb.getInt()
        if (targetQuarks != null && !targetQuarks.contains(quark)) {
            /* No need to skip, we're at the beginning of the payload now. */
            skipPayload(valueType)
            return null
        }


        /* All conditions match, return this interval */
        val sv: StateValue = when (valueType) {
            TYPE_NULL -> StateValue.nullValue()
            TYPE_BOOLEAN_TRUE -> StateValue.newValueBoolean(true)
            TYPE_BOOLEAN_FALSE -> StateValue.newValueBoolean(false)
            TYPE_INTEGER -> StateValue.newValueInt(bb.getInt())
            TYPE_LONG -> StateValue.newValueLong(bb.getLong())
            TYPE_DOUBLE -> StateValue.newValueDouble(bb.getDouble())
            /* For strings the first "short" indicates the size */
            TYPE_STRING -> {
                val strSize = bb.getShort()
                val array = ByteArray(strSize.toInt())
                bb.get(array)
                /* Confirm the 0'ed byte at the end */
                if (bb.get() != 0.toByte()) throw IOException()

                StateValue.newValueString(String(array))
            }
            else -> throw IOException()
        }
        return HTInterval(start, end, quark, sv)
    }

    /** Skip the payload part, depending on its type. */
    private fun skipPayload(valueType: Byte) {
        val payloadSize: Int = when (valueType) {
            TYPE_NULL,
            TYPE_BOOLEAN_TRUE,
            TYPE_BOOLEAN_FALSE -> 0
            TYPE_INTEGER -> Integer.BYTES
            TYPE_LONG -> java.lang.Long.BYTES
            TYPE_DOUBLE -> java.lang.Double.BYTES
           /* For strings the first "short" indicates the size */
            TYPE_STRING -> bb.getShort().toInt() + 1
            else -> throw IOException()
        }
        if (payloadSize > 0) bb.skip(payloadSize)
    }
}

private fun ByteBuffer.skip(nbBytes: Int) {
    position(position() + nbBytes)
}
