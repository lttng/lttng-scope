/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.tmf2.views.ui.timeline.widgets.timegraph.toolbar;

import org.lttng.scope.tmf2.views.ui.timeline.widgets.timegraph.StateRectangle;
import org.lttng.scope.tmf2.views.ui.timeline.widgets.timegraph.TimeGraphWidget;
import org.lttng.scope.tmf2.views.ui.timeline.widgets.timegraph.toolbar.nav.NavigationButtons;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

/**
 * Toolbar for the time graph viewer.
 *
 * @author Alexandre Montplaisir
 */
public class ViewerToolBar extends ToolBar {

    private final Image fHelpIcon = new Image(getClass().getResourceAsStream("/icons/toolbar/help.gif")); //$NON-NLS-1$

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
                new Separator(),

                getStateInfoButton(viewer),
                new SortingModeMenuButton(viewer),
                new FilterModeMenuButton(viewer));
    }

    // FIXME Temporary, should be moved to tooltip
    private Button getStateInfoButton(TimeGraphWidget viewer) {
        Button button = new Button();
        button.setGraphic(new ImageView(fHelpIcon));
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
//            centerOnCurrentScreen(alert);
        });
        return button;
    }

//    private static void centerOnCurrentScreen(Alert alert) {
//        // TODO
//        Screen screen = Screen.getPrimary();
//        Rectangle2D screenBounds = screen.getBounds();
//        double screenCenterX = screenBounds.getMinX() + screenBounds.getWidth() / 2;
//        double screenCenterY = screenBounds.getMinY() + screenBounds.getHeight() / 2;
//        double alertX = screenCenterX - alert.getWidth() / 2;
//        double alertY = screenCenterY - alert.getHeight() / 2;
//        alert.setX(alertX);
//        alert.setY(alertY);
//    }

}
