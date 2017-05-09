/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.tmf2.views.ui.jfx.testapp;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.lttng.scope.tmf2.views.core.NestingBoolean;
import org.lttng.scope.tmf2.views.core.context.ViewGroupContext;
import org.lttng.scope.tmf2.views.core.timegraph.control.TimeGraphModelControl;
import org.lttng.scope.tmf2.views.core.timegraph.model.provider.ITimeGraphModelProvider;
import org.lttng.scope.tmf2.views.ui.timeline.widgets.timegraph.TimeGraphWidget;

import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class LttngScopeApplication extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(@Nullable Stage primaryStage) throws Exception {
        if (primaryStage == null) {
            return;
        }

        /* Set up the view context */
        ViewGroupContext viewCtx = ViewGroupContext.getCurrent();
        ITmfTrace trace = new DummyTrace();

        ITimeGraphModelProvider modelProvider = new TestModelProvider();
        TimeGraphModelControl control = new TimeGraphModelControl(viewCtx, modelProvider);
        TimeGraphWidget widget = new TimeGraphWidget(control, new NestingBoolean());
        control.attachView(widget);
        Parent root = widget.getRootNode();

        primaryStage.setScene(new Scene(root));
        primaryStage.show();

        viewCtx.setCurrentTrace(trace);
    }

}
