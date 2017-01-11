/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Matthew Khouzam - Initial API and implementation
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.project.dialogs.offset.internal;

import org.eclipse.osgi.util.NLS;

/**
 * Messages for the offset dialog
 *
 * @noreference Messages class
 */
@SuppressWarnings("javadoc")
public class Messages extends NLS {

    private static final String BUNDLE_NAME = Messages.class.getPackage().getName() + ".messages"; //$NON-NLS-1$

    public static String OffsetDialog_AdvancedButton;
    public static String OffsetDialog_AdvancedMessage;
    public static String OffsetDialog_BasicButton;
    public static String OffsetDialog_BasicMessage;
    public static String OffsetDialog_OffsetTime;
    public static String OffsetDialog_ReferenceTime;
    public static String OffsetDialog_TargetTime;
    public static String OffsetDialog_Title;
    public static String OffsetDialog_TraceName;

    static {
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
