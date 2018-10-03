/*
 * Copyright (C) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package ca.polymtl.dorsal.libdelorean;

import ca.polymtl.dorsal.libdelorean.backend.IStateHistoryBackend;
import ca.polymtl.dorsal.libdelorean.backend.StateHistoryBackendFactory;
import ca.polymtl.dorsal.libdelorean.exceptions.AttributeNotFoundException;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test the AttributeTree class
 *
 * @author Patrick Tasse
 */
@SuppressWarnings("nls")
class AttributeTreeTest {

    private static final String THREADS = "Threads";
    private static final String[] NAMES = {
        "",      // ''
        "\0",    // 'Null'
        "a",     // 'a'
        "\"",    // '"'
        "/",     // '/'
        "\\",    // '\'
        "ab",    // 'ab'
        "\"\"",  // '""'
        "a/",    // 'a/'
        "a\\",   // 'a\'
        "/a",    // '/a'
        "//",    // '//'
        "/\\",   // '/\'
        "\\a",   // '\a'
        "\\/",   // '\/'
        "\\\\",  // '\\'
        "abc",   // 'abc'
        "\"/\"", // '"/"'
        "ab/",   // 'ab/'
        "ab\\",  // 'ab\'
        "a/b",   // 'a/b'
        "a//",   // 'a//'
        "a/\\",  // 'a/\'
        "a\\b",  // 'a\b'
        "a\\/",  // 'a\/'
        "a\\\\", // 'a\\'
        "/ab",   // '/ab'
        "/a/",   // '/a/'
        "/a\\",  // '/a\'
        "//a",   // '//a'
        "///",   // '///'
        "//\\",  // '//\'
        "/\\a",  // '/\a'
        "/\\/",  // '/\/'
        "/\\\\", // '/\\'
        "\\ab",  // '\ab'
        "\\a/",  // '\a/'
        "\\a\\", // '\a\'
        "\\/a",  // '\/a'
        "\\//",  // '\//'
        "\\/\\", // '\/\'
        "\\\\a", // '\\a'
        "\\\\/", // '\\/'
        "\\\\\\" // '\\\'
    };
    private static final String STATUS = "Status";

    /**
     * Test attribute tree file storage.
     * <p>
     * Tests that an attribute tree written to file is read back correctly.
     * <p>
     * Tests AttributeTree#writeSelf(File, long) and
     * AttributeTree#AttributeTree(StateSystem, FileInputStream).
     *
     * @throws IOException
     *             if there is an error accessing the test file
     * @throws AttributeNotFoundException
     *             if the test fails
     */
    @Test
    void testAttributeTreeFileStorage() throws IOException, AttributeNotFoundException {
        File file = File.createTempFile("AttributeTreeTest", ".ht");
        IStateHistoryBackend backend1 = StateHistoryBackendFactory.createNullBackend("test");
        StateSystem ss1 = new StateSystem(backend1);
        AttributeTree attributeTree1 = new AttributeTree(ss1);
        for (String name : NAMES) {
            String[] path = new String[] { THREADS, name, STATUS };
            attributeTree1.getQuarkAndAdd(-1, path);
        }
        attributeTree1.writeSelf(file, 0L);
        ss1.dispose();

        IStateHistoryBackend backend2 = StateHistoryBackendFactory.createNullBackend("test");
        StateSystem ss2 = new StateSystem(backend2);
        try (FileInputStream fis = new FileInputStream(file)) {
            AttributeTree attributeTree2 = new AttributeTree(ss2, fis);
            for (String name : NAMES) {
                String[] path = new String[] { THREADS, name, STATUS };
                int quark = attributeTree2.getQuarkDontAdd(-1, path);
                assertArrayEquals(path, attributeTree2.getFullAttributePathArray(quark));
                assertEquals(name, attributeTree2.getAttributeName(attributeTree2.getParentAttributeQuark(quark)));
            }
        } finally {
            ss2.dispose();
            file.delete();
        }
    }
}
