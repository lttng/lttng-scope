/*
 * Copyright (C) 2017 EfficiOS Inc.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.ui.timeline.widgets.timegraph.toolbar.modelconfig;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.osgi.util.NLS;

/**
 * Message bundle for the package
 *
 * @noreference Messages class
 */
@NonNullByDefault({})
@SuppressWarnings("javadoc")
public class Messages extends NLS {

    private static final String BUNDLE_NAME = Messages.class.getPackage().getName() + ".messages"; //$NON-NLS-1$

    public static String modelConfigButtonName;
    public static String modelConfigDialogTitle;
    public static String modelConfigDialogHeader;

    public static String modelConfigDialogRowHeaderState;
    public static String modelConfigDialogRowHeaderColor;
    public static String modelConfigDialogRowHeaderLineThickness;

    public static String modelConfigDialogResetDefaultsButton;

    static {
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
