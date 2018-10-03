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
import org.eclipse.tracecompass.ctf.core.event.types.*;
import org.eclipse.tracecompass.ctf.core.tests.shared.CtfTestTraceExtractor;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.lttng.scope.ttt.ctf.CtfTestTrace;

import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The class <code>VariantDeclarationTest</code> contains tests for the class
 * <code>{@link VariantDeclaration}</code>.
 *
 * @author ematkho
 */
class VariantDeclarationTest {

    private static final CtfTestTrace testTrace = CtfTestTrace.KERNEL;
    private static CtfTestTraceExtractor testTraceWrapper;

    private VariantDeclaration fixture;

    @BeforeAll
    static void setupClass() {
        testTraceWrapper = CtfTestTraceExtractor.extractTestTrace(testTrace);
    }

    @AfterAll
    static void teardownClass() {
        testTraceWrapper.close();
    }

    /**
     * Perform pre-test initialization.
     */
    @BeforeEach
    void setUp() {
        fixture = new VariantDeclaration();
    }

    private static IDefinitionScope createDefinitionScope() throws CTFException {
        StructDeclaration declaration = new StructDeclaration(8);
        VariantDeclaration variantDeclaration = new VariantDeclaration();
        variantDeclaration.addField("a", IntegerDeclaration.INT_32B_DECL);
        variantDeclaration.addField("b", IntegerDeclaration.INT_32L_DECL);
        variantDeclaration.setTag("a");

        EnumDeclaration enumDeclaration = new EnumDeclaration(IntegerDeclaration.UINT_8_DECL);
        enumDeclaration.add(0, 1, "a");
        enumDeclaration.add(2, 2, "b");
        declaration.addField("tag", enumDeclaration);
        declaration.addField("variant", variantDeclaration);
        EnumDefinition tagDef = new EnumDefinition(
                enumDeclaration,
                null,
                "tag",
                new IntegerDefinition(
                        IntegerDeclaration.UINT_8_DECL,
                        null,
                        "test",
                        0)
                );
        VariantDefinition variantDefinition = new VariantDefinition(
                variantDeclaration,
                testTraceWrapper.getTrace(),
                "tag",
                "tag",
                new StringDefinition(
                        StringDeclaration.getStringDeclaration(Encoding.UTF8),
                        null,
                        "f",
                        "tag"
                ));

        IDefinitionScope definitionScope = new StructDefinition(
                declaration,
                variantDefinition,
                "",
                new Definition[] { tagDef, variantDefinition }
                );

        return definitionScope;
    }

    /**
     * Run the VariantDeclaration() constructor test.
     */
    @Test
    void testVariantDeclaration() {
        assertNotNull(fixture);
        assertFalse(fixture.isTagged());
        String left = "[declaration] variant[";
        assertEquals(left, fixture.toString().substring(0, left.length()));
    }

    /**
     * Run the void addField(String,Declaration) method test.
     */
    @Test
    void testAddField() {
        fixture.setTag("");
        String tag = "";
        IDeclaration declaration = StringDeclaration.getStringDeclaration(Encoding.UTF8);
        fixture.addField(tag, declaration);
    }

    /**
     * Run the VariantDefinition createDefinition(DefinitionScope,String) method
     * test.
     *
     * @throws CTFException
     *             Should not happen
     */
    @Test
    void testCreateDefinition() throws CTFException {
        fixture.setTag("tag");
        fixture.addField("a", IntegerDeclaration.UINT_64B_DECL);
        IDefinitionScope definitionScope = createDefinitionScope();
        String fieldName = "";
        ByteBuffer allocate = ByteBuffer.allocate(100);
        BitBuffer bb = new BitBuffer(allocate);
        VariantDefinition result = fixture.createDefinition(definitionScope, fieldName, bb);

        assertNotNull(result);
    }

    /**
     * Run the boolean hasField(String) method test.
     */
    @Test
    void testHasField() {
        fixture.setTag("");
        String tag = "";
        boolean result = fixture.hasField(tag);

        assertFalse(result);
    }

    /**
     * Run the boolean isTagged() method test.
     */
    @Test
    void testIsTagged() {
        fixture.setTag("");
        boolean result = fixture.isTagged();

        assertTrue(result);
    }

    /**
     * Run the boolean isTagged() method test.
     */
    @Test
    void testIsTagged_null() {
        fixture.setTag((String) null);
        boolean result = fixture.isTagged();

        assertFalse(result);
    }

    /**
     * Run the void setTag(String) method test.
     */
    @Test
    void testSetTag() {
        fixture.setTag("");
        String tag = "";
        fixture.setTag(tag);
    }

    /**
     * Run the String toString() method test.
     */
    @Test
    void testToString() {
        fixture.setTag("");
        String result = fixture.toString();
        String left = "[declaration] variant[";
        String right = result.substring(0, left.length());

        assertEquals(left, right);
    }

    /**
     * Test the hashcode
     */
    @Test
    void hashcodeTest() {
        VariantDeclaration a = new VariantDeclaration();
        assertEquals(fixture.hashCode(), a.hashCode());

        VariantDeclaration b = new VariantDeclaration();
        b.addField("hi", StringDeclaration.getStringDeclaration(Encoding.UTF8));
        VariantDeclaration c = new VariantDeclaration();
        c.addField("hi", StringDeclaration.getStringDeclaration(Encoding.UTF8));
        assertEquals(b.hashCode(), c.hashCode());
    }

    /**
     * Test the equals
     */
    @Test
    void equalsTest() {
        VariantDeclaration a = new VariantDeclaration();
        VariantDeclaration b = new VariantDeclaration();
        b.addField("hi", StringDeclaration.getStringDeclaration(Encoding.UTF8));
        VariantDeclaration c = new VariantDeclaration();
        c.addField("hi", StringDeclaration.getStringDeclaration(Encoding.UTF8));
        VariantDeclaration d = new VariantDeclaration();
        assertNotEquals(a, null);
        assertNotEquals(a, new Object());
        assertNotEquals(a, b);
        assertNotEquals(a, c);
        assertEquals(a, d);
        assertEquals(a, a);
        assertEquals(b, c);
        assertNotEquals(b, a);
        assertNotEquals(c, a);
        assertEquals(d, a);
        assertEquals(c, b);
        b.setTag("hi");
        assertNotEquals(b, c);
        c.setTag("Hello");
        assertNotEquals(b, c);
        c.setTag("hi");
        assertEquals(b, c);
        b.addField("hello", IntegerDeclaration.INT_32B_DECL);
        d.addField("hello", IntegerDeclaration.INT_32B_DECL);
        d.addField("hi", StringDeclaration.getStringDeclaration(Encoding.UTF8));
        d.setTag("hi");
        assertEquals(b, d);
        assertEquals(d, b);
    }

    /**
     * Test the equals out of order
     */
    @Test
    void equalsOutOfOrderTest() {
        VariantDeclaration a = new VariantDeclaration();
        VariantDeclaration b = new VariantDeclaration();
        b.addField("hi", StringDeclaration.getStringDeclaration(Encoding.UTF8));
        b.addField("hello", new VariantDeclaration());
        a.addField("hello", new VariantDeclaration());
        a.addField("hi", StringDeclaration.getStringDeclaration(Encoding.UTF8));
        assertEquals(b, a);
    }

    /**
     * Test the equals out of order
     */
    @Test
    void equalsAddTwiceTest() {
        VariantDeclaration a = new VariantDeclaration();
        VariantDeclaration b = new VariantDeclaration();
        b.addField("hi", StringDeclaration.getStringDeclaration(Encoding.UTF8));
        a.addField("hi", StringDeclaration.getStringDeclaration(Encoding.UTF8));
        assertEquals(b, a);
        b.addField("hi", new VariantDeclaration());
        assertNotEquals(b, a);
    }

}