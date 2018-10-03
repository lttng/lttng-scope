/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *      Matthew Khouzam - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.ctf.core.event.scope;

import org.jetbrains.annotations.Nullable;

/**
 * A lttng specific speedup node a root with accelerated returns for some scopes
 * of a lexical scope
 *
 * @author Matthew Khouzam
 */
public final class RootScope extends LexicalScope {

    /**
     * The scope constructor
     */
    public RootScope() {
        super();
    }

    @Override
    public @Nullable ILexicalScope getChild(String name) {
        /*
         * This happens ~40 % of the time
         */
        if (name.equals(EVENT_HEADER.getPath())) {
            return EVENT_HEADER;
        }
        /*
         * This happens ~30 % of the time
         */
        if (name.equals(FIELDS.getPath())) {
            return FIELDS;
        }
        /*
         * This happens ~30 % of the time
         */
        if (name.equals(CONTEXT.getPath())) {
            return CONTEXT;
        }
        /*
         * This happens ~1 % of the time
         */
        if (name.equals(PACKET_HEADER.getPath())) {
            return PACKET_HEADER;
        }
        return super.getChild(name);
    }

}
