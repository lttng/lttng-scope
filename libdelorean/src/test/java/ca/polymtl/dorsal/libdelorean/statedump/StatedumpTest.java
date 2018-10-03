/*******************************************************************************
 * Copyright (c) 2016 EfficiOS Inc., Alexandre Montplaisir
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package ca.polymtl.dorsal.libdelorean.statedump;

import ca.polymtl.dorsal.libdelorean.statevalue.StateValue;
import com.google.common.io.MoreFiles;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link Statedump}.
 *
 * @author Alexandre Montplaisir
 */
public class StatedumpTest {

    /**
     * Test that a manually-written statedump is serialized then read correctly.
     */
    @Test
    public void testSimpleStatedump() {
        Path dir = null;
        try {
            dir = requireNonNull(Files.createTempDirectory("ss-serialization-test"));
            String ssid = "test-ssid";
            final int nbAttributes = 7;
            final int version = 0;

            List<String[]> initialAttributes = Arrays.asList(
                    new String[] { "Threads" },
                    new String[] { "Threads", "1000" },
                    new String[] { "Threads", "1000", "Status" },
                    new String[] { "Threads", "2000" },
                    new String[] { "Threads", "2000", "Status" },
                    new String[] { "Threads", "2000", "PPID" },
                    new String[] { "Threads", "2000", "Active" });

            List<StateValue> initialValues = Arrays.asList(
                    StateValue.nullValue(),
                    StateValue.nullValue(),
                    StateValue.newValueString("Running"),
                    StateValue.nullValue(),
                    StateValue.newValueInt(1),
                    StateValue.newValueLong(1000L),
                    StateValue.newValueBoolean(true));

            Statedump statedump = new Statedump(initialAttributes, initialValues, version);
            statedump.dumpState(dir, ssid);

            Statedump results = Statedump.loadState(dir, ssid);
            assertNotNull(results);

            assertEquals(version, results.getVersion());

            List<String[]> newAttributes = results.getAttributes();
            List<StateValue> newValues = results.getStates();
            assertEquals(nbAttributes, newAttributes.size());
            assertEquals(nbAttributes, newValues.size());

            for (int initialIdx = 0; initialIdx < nbAttributes; initialIdx++) {
                String[] attribute = initialAttributes.get(initialIdx);
                int newIdx = indexOfArray(newAttributes, attribute);
                assertArrayEquals(attribute, newAttributes.get(newIdx));
                assertEquals(initialValues.get(initialIdx), newValues.get(newIdx));
            }

        } catch (IOException e) {
            fail(e.getMessage());

        } finally {
            if (dir != null) {
                try {
                    MoreFiles.deleteRecursively(dir);
                } catch (IOException e) {
                }
            }
        }
    }

    /**
     * Util method to replace {@link List#indexOf} for a list of arrays.
     * {@link Object#equals} doesn't work with arrays, unfortunately.
     */
    private static <T> int indexOfArray(List<T[]> list, T[] o) {
        for (int i = 0; i < list.size(); i++)
            if (Arrays.equals(o, list.get(i))) {
                return i;
            }
        return -1;
    }
}
