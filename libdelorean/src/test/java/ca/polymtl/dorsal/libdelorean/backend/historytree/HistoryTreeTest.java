/*
 * Copyright (C) 2015 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package ca.polymtl.dorsal.libdelorean.backend.historytree;

import ca.polymtl.dorsal.libdelorean.statevalue.StateValue;
import com.google.common.collect.Iterables;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests the history tree
 *
 * @author Geneviève Bastien
 */
class HistoryTreeTest {

    /* Minimal allowed blocksize */
    private static final int BLOCK_SIZE = HistoryTree.TREE_HEADER_SIZE;

    private static final int NULL_INTERVAL_SIZE = (new HTInterval(0, 1, 1, StateValue.nullValue())).getSizeOnDisk();

    /* String with 23 characters, interval in file will be 25 bytes long */
    private static final String TEST_STRING = "abcdefghifklmnopqrstuvw"; //$NON-NLS-1$
    private static final @NotNull StateValue STRING_VALUE = StateValue.newValueString(TEST_STRING);
    private static final int STRING_INTERVAL_SIZE = (new HTInterval(0, 1, 1, STRING_VALUE)).getSizeOnDisk();

    private static final @NotNull StateValue LONG_VALUE = StateValue.newValueLong(10L);
    private static final int LONG_INTERVAL_SIZE = (new HTInterval(0, 1, 1, LONG_VALUE)).getSizeOnDisk();

    private static final @NotNull StateValue INT_VALUE = StateValue.newValueInt(1);
    private static final int INT_INTERVAL_SIZE = (new HTInterval(0, 1, 1, INT_VALUE)).getSizeOnDisk();

    private File fTempFile;

    /**
     * Create the temporary file for this history tree
     */
    @BeforeEach
    void setupTest() {
        try {
            fTempFile = File.createTempFile("tmpStateSystem", null); //$NON-NLS-1$
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

    /**
     * Delete the temporary history tree file after the test
     */
    @AfterEach
    void cleanup() {
        fTempFile.delete();
    }

    /**
     * Setup a history tree.
     *
     * @param maxChildren
     *            The max number of children per node in the tree (tree config
     *            option)
     */
    private HistoryTree setupSmallTree(int maxChildren) {
        HistoryTree ht = null;
        try {
            File newFile = fTempFile;
            assertNotNull(newFile);
            ht = new HistoryTree(newFile,
                    BLOCK_SIZE,
                    maxChildren, /* Number of children */
                    1, /* Provider version */
                    1); /* Start time */

        } catch (IOException e) {
            fail(e.getMessage());
        }

        assertNotNull(ht);
        return ht;
    }

    /**
     * Setup a history tree with config MAX_CHILDREN = 3.
     */
    private HistoryTree setupSmallTree() {
        return setupSmallTree(3);
    }

    private static long fillValues(HistoryTree ht, @NotNull StateValue value, int nbValues, long start) {
        for (int i = 0; i < nbValues; i++) {
            ht.insertInterval(new HTInterval(start + i, start + i + 1, 1, value));
        }
        return start + nbValues;
    }

    /**
     * Insert intervals in the tree to fill the current leaf node to capacity,
     * without exceeding it.
     *
     * This guarantees that the following insertion will create new nodes.
     *
     * @param ht
     *            The history tree in which to insert
     * @return Start time of the current leaf node. Future insertions should be
     *         greater than or equal to this to make sure the intervals go in
     *         the leaf node.
     */
    private static long fillNextLeafNode(HistoryTree ht, long leafNodeStart) {
        int prevCount = ht.getNodeCount();
        int prevDepth = getDepth(ht);

        /* Fill the following leaf node */
        HistoryTreeNode node = getLatestLeaf(ht);
        int nodeFreeSpace = node.getNodeFreeSpace();
        int nbIntervals = nodeFreeSpace / STRING_INTERVAL_SIZE;
        long ret = fillValues(ht, STRING_VALUE, nbIntervals, leafNodeStart);

        /* Make sure we haven't changed the depth or node count */
        assertEquals(prevCount, ht.getNodeCount());
        assertEquals(prevDepth, getDepth(ht));

        return ret;
    }

    /**
     * Test that nodes are filled
     *
     * It fills nodes with sequential intervals from one attribute only, so that
     * leafs should be filled.
     */
    @Test
    void testSequentialFill() {
        HistoryTree ht = setupSmallTree();

        HistoryTreeNode node = getLatestLeaf(ht);

        /* Add null intervals up to ~10% */
        int nodeFreeSpace = node.getNodeFreeSpace();
        int nbIntervals = nodeFreeSpace / 10 / NULL_INTERVAL_SIZE;
        long start = fillValues(ht, StateValue.nullValue(), nbIntervals, 1);
        assertEquals(nodeFreeSpace - nbIntervals * NULL_INTERVAL_SIZE, node.getNodeFreeSpace());

        /* Add integer intervals up to ~20% */
        nodeFreeSpace = node.getNodeFreeSpace();
        nbIntervals = nodeFreeSpace / 10 / INT_INTERVAL_SIZE;
        start = fillValues(ht, INT_VALUE, nbIntervals, start);
        assertEquals(nodeFreeSpace - nbIntervals * INT_INTERVAL_SIZE, node.getNodeFreeSpace());

        /* Add long intervals up to ~30% */
        nodeFreeSpace = node.getNodeFreeSpace();
        nbIntervals = nodeFreeSpace / 10 / LONG_INTERVAL_SIZE;
        start = fillValues(ht, LONG_VALUE, nbIntervals, start);
        assertEquals(nodeFreeSpace - nbIntervals * LONG_INTERVAL_SIZE, node.getNodeFreeSpace());

        /* Add string intervals up to ~40% */
        nodeFreeSpace = node.getNodeFreeSpace();
        nbIntervals = nodeFreeSpace / 10 / STRING_INTERVAL_SIZE;
        start = fillValues(ht, STRING_VALUE, nbIntervals, start);
        assertEquals(nodeFreeSpace - nbIntervals * STRING_INTERVAL_SIZE, node.getNodeFreeSpace());

    }

    /**
     * Test the addition of new nodes to the tree and make sure the tree is
     * built with the right structure
     */
    @Test
    void testDepth() {
        HistoryTree ht = setupSmallTree();

        /* Fill a first node */
        HistoryTreeNode node = getLatestLeaf(ht);
        int nodeFreeSpace = node.getNodeFreeSpace();
        int nbIntervals = nodeFreeSpace / STRING_INTERVAL_SIZE;
        long start = fillValues(ht, STRING_VALUE, nbIntervals, 1);

        /* Add intervals that should add a sibling to the node */
        assertEquals(1, ht.getNodeCount());
        assertEquals(1, getDepth(ht));
        start = fillValues(ht, STRING_VALUE, 1, start);
        assertEquals(3, ht.getNodeCount());
        assertEquals(2, getDepth(ht));

        /* Fill the latest leaf node (2nd child) */
        node = getLatestLeaf(ht);
        nodeFreeSpace = node.getNodeFreeSpace();
        nbIntervals = nodeFreeSpace / STRING_INTERVAL_SIZE;
        start = fillValues(ht, STRING_VALUE, nbIntervals, start);

        /*
         * Add an interval that should add another sibling to the previous nodes
         */
        start = fillValues(ht, STRING_VALUE, 1, start);
        assertEquals(4, ht.getNodeCount());
        assertEquals(2, getDepth(ht));

        /* Fill the latest leaf node (3rd and last child) */
        node = getLatestLeaf(ht);
        nodeFreeSpace = node.getNodeFreeSpace();
        nbIntervals = nodeFreeSpace / STRING_INTERVAL_SIZE;
        start = fillValues(ht, STRING_VALUE, nbIntervals, start);

        /* The new node created here should generate a new branch */
        start = fillValues(ht, STRING_VALUE, 1, start);
        assertEquals(7, ht.getNodeCount());
        assertEquals(3, getDepth(ht));
    }

    /**
     * Make sure the node sequence numbers and parent pointers are set correctly
     * when new nodes are created.
     *
     * <p>
     * We are building a tree whose node sequence numbers will look like this at
     * the end:
     * </p>
     *
     * <pre>
     *     3
     *    / \
     *   1   4
     *  / \   \
     * 0   2   5
     * </pre>
     *
     * <p>
     * However while building, the parent pointers may be different.
     * </p>
     *
     * @throws ClosedChannelException
     *             If the test fails
     */
    @Test
    void testNodeSequenceNumbers() throws ClosedChannelException {
        /* Represents the start time of the current leaf node */
        long start = 1;

        HistoryTree ht = setupSmallTree(2);
        start = fillNextLeafNode(ht, start);

        List<HistoryTreeNode> branch = ht.getLatestBranch();
        assertEquals(1, branch.size());
        assertEquals( 0, branch.get(0).getSeqNumber());
        assertEquals(-1, branch.get(0).getParentSeqNumber());

        /* Create a new branch */
        start = fillValues(ht, STRING_VALUE, 1, start);
        start = fillNextLeafNode(ht, start);
        assertEquals(3, ht.getNodeCount());
        assertEquals(2, getDepth(ht));

        /* Make sure the first node's parent was updated */
        HistoryTreeNode node = ht.readNode(0);
        assertEquals(0, node.getSeqNumber());
        assertEquals(1, node.getParentSeqNumber());

        /* Make sure the new branch is alright */
        branch = ht.getLatestBranch();
        assertEquals(2, branch.size());
        assertEquals( 1, branch.get(0).getSeqNumber());
        assertEquals(-1, branch.get(0).getParentSeqNumber());
        assertEquals( 2, branch.get(1).getSeqNumber());
        assertEquals( 1, branch.get(1).getParentSeqNumber());

        /* Create a third branch */
        start = fillValues(ht, STRING_VALUE, 1, start);
        start = fillNextLeafNode(ht, start);
        assertEquals(6, ht.getNodeCount());
        assertEquals(3, getDepth(ht));

        /* Make sure all previous nodes are still correct */
        node = ht.readNode(0);
        assertEquals(0, node.getSeqNumber());
        assertEquals(1, node.getParentSeqNumber());
        node = ht.readNode(1);
        assertEquals(1, node.getSeqNumber());
        assertEquals(3, node.getParentSeqNumber());
        node = ht.readNode(2);
        assertEquals(2, node.getSeqNumber());
        assertEquals(1, node.getParentSeqNumber());

        /* Verify the contents of the new latest branch */
        branch = ht.getLatestBranch();
        assertEquals(3, branch.size());
        assertEquals( 3, branch.get(0).getSeqNumber());
        assertEquals(-1, branch.get(0).getParentSeqNumber());
        assertEquals( 4, branch.get(1).getSeqNumber());
        assertEquals( 3, branch.get(1).getParentSeqNumber());
        assertEquals( 5, branch.get(2).getSeqNumber());
        assertEquals( 4, branch.get(2).getParentSeqNumber());
    }

    private static HistoryTreeNode getLatestLeaf(HistoryTree ht) {
        List<HistoryTreeNode> latest = ht.getLatestBranch();
        return Iterables.getLast(latest);
    }

    private static int getDepth(HistoryTree ht) {
        return ht.getLatestBranch().size();
    }
}
