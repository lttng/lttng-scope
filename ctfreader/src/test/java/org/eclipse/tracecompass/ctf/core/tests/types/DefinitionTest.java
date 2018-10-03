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

import org.eclipse.tracecompass.ctf.core.event.scope.IDefinitionScope;
import org.eclipse.tracecompass.ctf.core.event.types.*;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * The class <code>DefinitionTest</code> contains tests for the class
 * <code>{@link Definition}</code>.
 *
 * @author Matthew Khouzam
 */
class DefinitionTest {

    /**
     * Since Definition is abstract, we'll minimally extend it here to
     * instantiate it.
     */
    static class DefTest extends Definition {

        private static final @NotNull StringDeclaration STRINGDEC = StringDeclaration.getStringDeclaration(Encoding.UTF8);

        public DefTest(IDefinitionScope definitionScope, @NotNull String fieldName) {
            super(DefTest.STRINGDEC, definitionScope, fieldName);
        }

        @Override
        public @NotNull IDeclaration getDeclaration() {
            return DefTest.STRINGDEC;
        }

    }

    /**
     * Test a definition
     */
    @Test
    void testToString() {
        IDefinition fixture = new DefTest(null, "Hello");
        String result = fixture.toString();

        assertNotNull(result);
    }
}