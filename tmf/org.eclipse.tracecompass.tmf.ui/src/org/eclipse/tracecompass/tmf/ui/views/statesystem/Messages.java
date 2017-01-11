/*******************************************************************************
 * Copyright (c) 2013, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.views.statesystem;

import org.eclipse.osgi.util.NLS;

/**
 * Localizable strings in the State System Visualizer.
 *
 * @author Alexandre Montplaisir
 * @noreference Messages class
 */
@SuppressWarnings("javadoc")
public class Messages extends NLS {

    private static final String BUNDLE_NAME = Messages.class.getPackage().getName() + ".messages"; //$NON-NLS-1$

    public static String TreeNodeColumnLabel;
    public static String QuarkColumnLabel;
    public static String ValueColumnLabel;
    public static String TypeColumnLabel;
    public static String StartTimeColumLabel;
    public static String EndTimeColumLabel;
    public static String AttributePathColumnLabel;

    public static String OutOfRangeMsg;
    public static String FilterButton;
    public static String TypeInteger;
    public static String TypeLong;
    public static String TypeDouble;
    public static String TypeString;
    public static String TypeCustom;

    static {
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {}
}
