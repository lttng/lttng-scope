/*******************************************************************************
 * Copyright (c) 2013, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marc-Andre Laperle - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.trace.indexer.internal;

import org.eclipse.osgi.util.NLS;

/**
 * Message bundle
 *
 * @author Marc-Andre Laperle
 * @noreference Messages class
 */
@SuppressWarnings("javadoc")
public class Messages extends NLS {

    private static final String BUNDLE_NAME = Messages.class.getPackage().getName() + ".messages"; //$NON-NLS-1$

    public static String ErrorOpeningIndex;
    public static String BTree_IOErrorAllocatingNode;
    public static String IOErrorClosingIndex;
    public static String IOErrorReadingHeader;
    public static String IOErrorWritingHeader;
    public static String BTreeNode_IOErrorLoading;
    public static String BTreeNode_IOErrorWriting;
    public static String FlatArray_IOErrorReading;
    public static String FlatArray_IOErrorWriting;

    static {
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
