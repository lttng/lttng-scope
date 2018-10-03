/*******************************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.tracecompass.internal.ctf.core.event.metadata.tsdl.trace;

import org.antlr.runtime.tree.CommonTree;
import org.eclipse.tracecompass.ctf.core.event.metadata.ParseException;
import org.eclipse.tracecompass.ctf.parser.CTFParser;
import org.eclipse.tracecompass.internal.ctf.core.event.metadata.ICommonTreeParser;
import org.eclipse.tracecompass.internal.ctf.core.event.metadata.tsdl.stream.StreamScopeParser;

import java.util.List;

import static org.eclipse.tracecompass.internal.ctf.core.event.metadata.tsdl.TsdlUtils.concatenateUnaryStrings;

/**
 * TSDL uses three different types of scoping: a lexical scope is used for
 * declarations and type definitions, and static and dynamic scopes are used for
 * variants references to tag fields (with relative and absolute path lookups)
 * and for sequence references to length fields.
 *
 * @author Matthew Khouzam
 * @author Efficios - Description
 *
 */
public final class TraceScopeParser implements ICommonTreeParser {

    /**
     * Parameter object
     *
     * @author Matthew Khouzam
     *
     */
    public static final class Param implements ICommonTreeParserParameter {
        private final List<CommonTree> fList;

        /**
         * Parameter object constructor
         *
         * @param list
         *            the list of subtrees
         */
        public Param(List<CommonTree> list) {
            fList = list;

        }

    }

    /**
     * Instance
     */
    public static final TraceScopeParser INSTANCE = new TraceScopeParser();

    private TraceScopeParser() {
    }

    /**
     * Parse a trace scope to get a concatenated scope name. Like
     * "trace.version.minor"
     *
     * @param unused
     *            unused
     * @param param
     *            a {@link Param} containing the list of ASTs to make the scope
     * @return a {@link String} of the scope
     * @throws ParseException
     *             an AST was malformed
     *
     */
    @Override
    public String parse(CommonTree unused, ICommonTreeParserParameter param) throws ParseException {
        if (!(param instanceof Param)) {
            throw new IllegalArgumentException("Param must be a " + Param.class.getCanonicalName()); //$NON-NLS-1$
        }
        List<CommonTree> lengthChildren = ((Param) param).fList;
        CommonTree nextElem = (CommonTree) lengthChildren.get(1).getChild(0);
        switch (nextElem.getType()) {
        case CTFParser.IDENTIFIER:
            return concatenateUnaryStrings(lengthChildren.subList(1, lengthChildren.size()));
        case CTFParser.STREAM:
            return StreamScopeParser.INSTANCE.parse(null, new StreamScopeParser.Param(lengthChildren.subList(1, lengthChildren.size())));
        default:
            throw new ParseException("Unsupported scope trace." + nextElem); //$NON-NLS-1$
        }
    }

}
