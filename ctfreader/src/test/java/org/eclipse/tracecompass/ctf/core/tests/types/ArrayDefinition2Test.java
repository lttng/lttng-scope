/*******************************************************************************
 * Copyright (c) 2013, 2014 Ericsson
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Khouzam - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.ctf.core.tests.types;

import org.eclipse.tracecompass.ctf.core.CTFException;
import org.eclipse.tracecompass.ctf.core.event.io.BitBuffer;
import org.eclipse.tracecompass.ctf.core.event.scope.IDefinitionScope;
import org.eclipse.tracecompass.ctf.core.event.scope.LexicalScope;
import org.eclipse.tracecompass.ctf.core.event.types.*;
import org.eclipse.tracecompass.ctf.core.tests.io.Util;
import org.eclipse.tracecompass.ctf.core.trace.CTFTrace;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * The class <code>ArrayDefinition2Test</code> contains tests for the class
 * <code>{@link ArrayDefinition}</code>.
 *
 */
class ArrayDefinition2Test {

    private @NotNull CTFTrace trace = new CTFTrace();
    private ArrayDefinition charArrayFixture;
    private ArrayDefinition stringArrayFixture;
    private ArrayDefinition longArrayFixture;

    /**
     * Perform pre-test initialization.
     *
     * structDef shouldn't be null after parsing the CTFTraceReader object, so
     * we can ignore the warning.
     */
    @BeforeEach
    void setUp() {
        charArrayFixture = createCharArray();
        stringArrayFixture = createStringArray();
        longArrayFixture = createLongArray();
    }

    private ArrayDefinition createLongArray() {
        IntegerDeclaration decl = IntegerDeclaration.createDeclaration(32, false, 10, ByteOrder.BIG_ENDIAN, Encoding.NONE, "none", 8);
        List<Definition> defs = createIntDefs(10, 32);
        ArrayDefinition temp = setUpDeclaration(decl, defs);
        return temp;
    }

    private ArrayDefinition createCharArray() {
        IntegerDeclaration decl = IntegerDeclaration.createDeclaration(8, false, 10, ByteOrder.BIG_ENDIAN, Encoding.UTF8, "none", 8);
        List<Definition> defs = createIntDefs(4, 8);
        ArrayDefinition temp = setUpDeclaration(decl, defs);
        return temp;
    }

    private ArrayDefinition createStringArray() {
        StringDeclaration strDecl = StringDeclaration.getStringDeclaration(Encoding.UTF8);
        List<Definition> defs = createDefs();
        ArrayDefinition temp = setUpDeclaration(strDecl, defs);
        return temp;
    }

    private ArrayDefinition setUpDeclaration(@NotNull IDeclaration decl,
            @NotNull List<Definition> defs) {
        CompoundDeclaration ad = new ArrayDeclaration(0, decl);
        ArrayDefinition temp = new ArrayDefinition(ad, this.trace, "Testx", defs);
        return temp;
    }

    private static @NotNull List<Definition> createIntDefs(int size, int bits) {
        List<Definition> defs = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            String content = "test" + i;
            defs.add(new IntegerDefinition(IntegerDeclaration.createDeclaration(bits, false,
                    16, ByteOrder.LITTLE_ENDIAN, Encoding.UTF8, content, 24), null, content, i));
        }
        return defs;
    }

    private static @NotNull List<Definition> createDefs() {
        int size = 4;
        List<Definition> defs = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            String content = "test" + i;
            defs.add(new StringDefinition(
                    StringDeclaration.getStringDeclaration(Encoding.UTF8), null, content, content));
        }
        return defs;
    }

    /**
     * Run the ArrayDefinition(ArrayDeclaration,DefinitionScope,String)
     * constructor test.
     */
    @Test
    void testArrayDefinition_baseDeclaration() {
        CompoundDeclaration declaration = (CompoundDeclaration) charArrayFixture.getDeclaration();
        String fieldName = "";

        ArrayDefinition result = new ArrayDefinition(declaration, this.trace, fieldName, Arrays.asList(new Definition[0]));
        assertNotNull(result);
    }

    /**
     * Run the ArrayDefinition(ArrayDeclaration,DefinitionScope,String)
     * constructor test.
     */
    @Test
    public void testArrayDefinition_newDeclaration() {
        CompoundDeclaration declaration = new ArrayDeclaration(0,
                StringDeclaration.getStringDeclaration(Encoding.UTF8));
        IDefinitionScope definitionScope = getDefinitionScope();

        String fieldName = "";
        ArrayDefinition result = new ArrayDefinition(declaration, definitionScope, fieldName, Arrays.asList(new Definition[0]));
        assertNotNull(result);
    }

    /**
     * Run the ArrayDeclaration getDeclaration() method test.
     */
    @Test
    void testGetDeclaration() {
        CompoundDeclaration result = (CompoundDeclaration) charArrayFixture.getDeclaration();

        assertNotNull(result);
    }

    /**
     * Run the Definition getDefinitions().get(int) method test.
     */
    @Test
    public void testgetElem_noDefs() {
        int i = 0;
        IDefinition result = charArrayFixture.getDefinitions().get(i);

        assertNotNull(result);
    }

    /**
     * Run the Definition getDefinitions().get(int) method test.
     */
    @Test
    void testgetElem_withDefs() {
        List<Definition> defs = createDefs();
        IDefinitionScope definitionScope = getDefinitionScope();
        ArrayDefinition ad = new ArrayDefinition((CompoundDeclaration) charArrayFixture.getDeclaration(), definitionScope, "test", defs);
        int j = 1;

        IDefinition result = ad.getDefinitions().get(j);

        assertNotNull(result);
    }

    private static @NotNull IDefinitionScope getDefinitionScope() {
        return new IDefinitionScope() {

            @Override
            public Definition lookupDefinition(String lookupPath) {
                return null;
            }

            @Override
            public LexicalScope getScopePath() {
                return null;
            }
        };
    }

    /**
     * Run the void read(BitBuffer) method test.
     *
     * @throws CTFException
     *             error
     */
    @Test
    void testRead_noDefs() throws CTFException {
        BitBuffer input = new BitBuffer(Util.testMemory(ByteBuffer.allocateDirect(128)));
        charArrayFixture.getDeclaration().createDefinition(null, "test", input);
    }

    /**
     * Run the String toString() method test.
     */
    @Test
    void testToString_char() {
        String result = charArrayFixture.toString();
        assertNotNull(result);
    }

    /**
     * Run the String toString() method test.
     */
    @Test
    void testToString_long() {
        String result = longArrayFixture.toString();
        assertNotNull(result);
    }

    /**
     * Run the String toString() method test.
     */
    @Test
    void testToString_string() {
        String result = stringArrayFixture.toString();
        assertNotNull(result);
    }

    /**
     * Run the String toString() method test.
     */
    @Test
    void testToString_withDefs() {
        String result = charArrayFixture.toString();

        assertNotNull(result);
    }

    /**
     * Run the String toString() method test.
     */
    @Test
    void testToStringStringArray() {
        String result = stringArrayFixture.toString();

        assertNotNull(result);
    }
}
