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

import org.eclipse.tracecompass.ctf.core.event.types.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.ByteOrder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * The class <code>EnumDefinitionTest</code> contains tests for the class
 * <code>{@link EnumDefinition}</code>.
 *
 * @author ematkho
 * @version $Revision: 1.0 $
 */
class EnumDefinitionTest {

    private EnumDefinition fixtureA;
    private EnumDefinition fixtureB;

    /**
     * Perform pre-test initialization.
     */
    @BeforeEach
    void setUp() {
        IntegerDeclaration integerDeclaration = IntegerDeclaration.createDeclaration(1, false, 1, ByteOrder.BIG_ENDIAN,
                Encoding.ASCII, "", 8);
        EnumDeclaration declaration = new EnumDeclaration(
                integerDeclaration);
        declaration.add(0, 10, "a");
        declaration.add(11, 20, "b");
        String fieldName = "";

        fixtureA = new EnumDefinition(declaration, null, fieldName, new IntegerDefinition(integerDeclaration, null, fieldName, 4));
        fixtureB = new EnumDefinition(declaration, null, fieldName, new IntegerDefinition(integerDeclaration, null, fieldName, 12));
    }

    /**
     * Run the EnumDefinition(EnumDeclaration,DefinitionScope,String)
     * constructor test.
     */
    @Test
    void testEnumDefinition() {
        assertNotNull(fixtureA);
        assertNotNull(fixtureB);
    }

    /**
     * Run the String getValue() method test.
     */
    @Test
    void testGetValue() {
        String result = fixtureA.getValue();

        assertNotNull(result);
        assertEquals("a", result);
    }

    /**
     * Run the long getIntegerValue() method test.
     */
    @Test
    void testGetIntegerValue_one() {
        long result = fixtureA.getIntegerValue();
        assertEquals(4L, result);
    }

    /**
     * Run the String toString() method test.
     */
    @Test
    void testToString() {
        String result = fixtureB.toString();

        assertEquals("{ value = b, container = 12 }", result);
    }
}