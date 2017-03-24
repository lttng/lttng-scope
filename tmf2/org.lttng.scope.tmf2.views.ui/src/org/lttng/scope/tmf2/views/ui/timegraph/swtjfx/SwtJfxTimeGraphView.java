/*******************************************************************************
 * Copyright (c) 2016 EfficiOS Inc., Alexandre Montplaisir
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.lttng.scope.tmf2.views.ui.timegraph.swtjfx;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tracecompass.tmf.ui.views.TmfView;
import org.lttng.scope.tmf2.views.core.timegraph.control.TimeGraphModelControl;
import org.lttng.scope.tmf2.views.core.timegraph.model.provider.ITimeGraphModelRenderProvider;

import com.google.common.annotations.VisibleForTesting;

public abstract class SwtJfxTimeGraphView extends TmfView {

    private final ITimeGraphModelRenderProvider fModelRenderProvider;
    private final TimeGraphModelControl fModelControl;

    private @Nullable SwtJfxTimeGraphViewer fViewer;

    protected SwtJfxTimeGraphView(String viewName, ITimeGraphModelRenderProvider modelRenderProvider) {
        super(viewName);
        fModelRenderProvider = modelRenderProvider;
        fModelControl = new TimeGraphModelControl(fModelRenderProvider);
    }

    @Override
    public void createPartControl(@Nullable Composite parent) {
        if (parent == null) {
            return;
        }
        SwtJfxTimeGraphViewer viewer = new SwtJfxTimeGraphViewer(parent, fModelControl);
        fModelControl.attachView(viewer);

        IToolBarManager toolbarMgr = getViewSite().getActionBars().getToolBarManager();
        toolbarMgr.add(ActionFactory.getSelectSortingModeAction(viewer));
        toolbarMgr.add(ActionFactory.getSelectFilterModesAction(viewer));
        toolbarMgr.add(ActionFactory.getInfoOnSelectedStateAction(viewer));

        fViewer = viewer;
    }

    @Override
    public void dispose() {
        fModelControl.dispose();
    }

    @Override
    public void setFocus() {
    }

    @VisibleForTesting
    TimeGraphModelControl getControl() {
        return fModelControl;
    }

    @VisibleForTesting
    @Nullable SwtJfxTimeGraphViewer getViewer() {
        return fViewer;
    }
}
