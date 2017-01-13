/*******************************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Marc-Andre Laperle - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.views.internal;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.tracecompass.tmf.ui.activator.internal.Activator;
import org.eclipse.tracecompass.tmf.ui.activator.internal.ITmfImageConstants;
import org.eclipse.tracecompass.tmf.ui.activator.internal.ITmfUIPreferences;
import org.eclipse.tracecompass.tmf.ui.activator.internal.Messages;
import org.eclipse.tracecompass.tmf.ui.views.ITmfTimeAligned;

/**
 * An action that toggles time alignment of views.
 *
 * @see ITmfTimeAligned
 */
public class TimeAlignViewsAction extends Action {
    /**
     * Creates a AlignViewsAction
     */
    public TimeAlignViewsAction() {
        super(Messages.TmfView_AlignViewsActionNameText, IAction.AS_CHECK_BOX);

        setId("org.eclipse.tracecompass.tmf.ui.views.TimeAlignViewsAction"); //$NON-NLS-1$
        setToolTipText(Messages.TmfView_AlignViewsActionToolTipText);
        setImageDescriptor(Activator.instance().getImageDescripterFromPath(ITmfImageConstants.IMG_UI_LINK));
        setChecked(isPreferenceEnabled());
    }

    @Override
    public void run() {
        boolean newValue = !isPreferenceEnabled();
        InstanceScope.INSTANCE.getNode(Activator.instance().getPluginId()).putBoolean(ITmfUIPreferences.PREF_ALIGN_VIEWS, newValue);
        setChecked(newValue);
    }

    private static boolean isPreferenceEnabled() {
        return InstanceScope.INSTANCE.getNode(Activator.instance().getPluginId()).getBoolean(ITmfUIPreferences.PREF_ALIGN_VIEWS, true);
    }
}
