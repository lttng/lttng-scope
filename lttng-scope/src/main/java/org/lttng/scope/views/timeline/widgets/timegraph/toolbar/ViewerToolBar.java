/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.views.timeline.widgets.timegraph.toolbar;

import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import org.lttng.scope.views.jfx.JfxImageFactory;
import org.lttng.scope.views.jfx.JfxUtils;
import org.lttng.scope.views.timeline.widgets.timegraph.StateRectangle;
import org.lttng.scope.views.timeline.widgets.timegraph.TimeGraphWidget;
import org.lttng.scope.views.timeline.widgets.timegraph.toolbar.debugopts.DebugOptionsButton;
import org.lttng.scope.views.timeline.widgets.timegraph.toolbar.drawnevents.EventSeriesMenuButton;
import org.lttng.scope.views.timeline.widgets.timegraph.toolbar.modelconfig.ModelConfigButton;
import org.lttng.scope.views.timeline.widgets.timegraph.toolbar.nav.NavigationButtons;

/**
 * Toolbar for the time graph viewer.
 *
 * @author Alexandre Montplaisir
 */
public class ViewerToolBar extends ToolBar {

    private static final String HELP_ICON_PATH = "/icons/toolbar/help.gif"; //$NON-NLS-1$

    /**
     * Constructor
     *
     * @param viewer
     *            The time graph viewer to which this toolbar belongs.
     */
    public ViewerToolBar(TimeGraphWidget viewer) {
        super();

        NavigationButtons navButtons = new NavigationButtons(viewer);

        getItems().addAll(
                new Label(viewer.getName()),
                new Separator(),

                new ZoomInButton(viewer),
                new ZoomOutButton(viewer),
                new ZoomToSelectionButton(viewer),
                new ZoomToFullRangeButton(viewer),
                new Separator(),

                new HBox(
                    navButtons.getBackButton(),
                    navButtons.getForwardButton(),
                    navButtons.getMenuButton()
                ),
                getStateInfoButton(viewer),
                new Separator(),

                new ModelConfigButton(viewer),
                new ArrowSeriesMenuButton(viewer),
                new EventSeriesMenuButton(viewer),
                new SortingModeMenuButton(viewer),
                new FilterModeMenuButton(viewer),
                new Separator(),

                new DebugOptionsButton(viewer));
    }

    // FIXME Temporary, should be moved to tooltip
    private Button getStateInfoButton(TimeGraphWidget viewer) {
        Button button = new Button();
        Image helpIcon = JfxImageFactory.instance().getImageFromResource(HELP_ICON_PATH);
        button.setGraphic(new ImageView(helpIcon));
        button.setTooltip(new Tooltip("Get State Info")); //$NON-NLS-1$
        button.setOnAction(e -> {
            StateRectangle state = viewer.getSelectedState();
            if (state == null) {
                return;
            }
            Alert alert = new Alert(AlertType.INFORMATION);
            /* Use a read-only TextField so the text can be copy-pasted */
            TextField content = new TextField(state.toString());
            content.setEditable(false);
            content.setPrefWidth(1000.0);
            alert.getDialogPane().setContent(content);
            alert.setResizable(true);
            alert.show();

            JfxUtils.centerDialogOnScreen(alert, this);
        });
        return button;
    }

}
