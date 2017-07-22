/*******************************************************************************
 * Copyright (c) 2016 EfficiOS Inc. and others
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.lttng.scope.lttng.ust.ui.analysis.debuginfo.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.osgi.util.NLS;

/**
 * Message bundle
 *
 * @noreference Messages class
 */
@NonNullByDefault({})
@SuppressWarnings("javadoc")
public class Messages extends NLS {

    private static final String BUNDLE_NAME = Messages.class.getPackage().getName() + ".messages"; //$NON-NLS-1$

    public static String PreferencePage_WindowDescription;

    public static String PreferencePage_CheckboxLabel;
    public static String PreferencePage_CheckboxTooltip;

    public static String PreferencePage_ButtonBrowse;
    public static String PreferencePage_ButtonClear;

    public static String PreferencePage_BrowseDialogTitle;
    public static String PreferencePage_ErrorDirectoryDoesNotExists;

    static {
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {}
}
