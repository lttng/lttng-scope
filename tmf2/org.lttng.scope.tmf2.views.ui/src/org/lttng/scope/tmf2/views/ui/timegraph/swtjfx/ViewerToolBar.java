/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.tmf2.views.ui.timegraph.swtjfx;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.lttng.scope.tmf2.views.core.timegraph.model.provider.ITimeGraphModelRenderProvider;
import org.lttng.scope.tmf2.views.core.timegraph.model.provider.ITimeGraphModelRenderProvider.FilterMode;
import org.lttng.scope.tmf2.views.core.timegraph.model.provider.ITimeGraphModelRenderProvider.SortingMode;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.MenuButton;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.ToolBar;

class ViewerToolBar extends ToolBar {

    public ViewerToolBar(SwtJfxTimeGraphViewer viewer) {
        super();
        getItems().addAll(getSortingModeButton(viewer),
                getFilterModeButton(viewer),
                getStateInfoButton(viewer));
    }

    private static MenuButton getSortingModeButton(SwtJfxTimeGraphViewer viewer) {
        ITimeGraphModelRenderProvider provider = viewer.getControl().getModelRenderProvider();

        ToggleGroup tg = new ToggleGroup();
        List<RadioMenuItem> sortingModeItems = IntStream.range(0, provider.getSortingModes().size())
                .mapToObj(index -> {
                    SortingMode sm = provider.getSortingModes().get(index);
                    RadioMenuItem rmi = new RadioMenuItem(sm.getName());
                    rmi.setToggleGroup(tg);
                    rmi.setOnAction(e -> {
                        provider.setCurrentSortingMode(index);
                        viewer.getControl().repaintCurrentArea();
                    });
                    return rmi;
                })
                .collect(Collectors.toList());
        if (!sortingModeItems.isEmpty()) {
            /*
             * Initialize the first mode to be selected, which is what the model
             * does. This should not trigger the event handler.
             */
            sortingModeItems.get(0).setSelected(true);
        }
        MenuButton button = new MenuButton();
        button.setText("Sorting Modes");
        button.getItems().addAll(sortingModeItems);
        return button;
    }

    private static MenuButton getFilterModeButton(SwtJfxTimeGraphViewer viewer) {
        ITimeGraphModelRenderProvider provider = viewer.getControl().getModelRenderProvider();

        List<CheckMenuItem> filterModeItems = IntStream.range(0, provider.getFilterModes().size())
                .mapToObj(index -> {
                    FilterMode fm = provider.getFilterModes().get(index);
                    CheckMenuItem cmi = new CheckMenuItem(fm.getName());
                    cmi.setOnAction(e -> {
                        if (cmi.isSelected()) {
                            /* Mode was enabled */
                            provider.enableFilterMode(index);
                        } else {
                            /* Mode was disabled */
                            provider.disableFilterMode(index);
                        }
                        viewer.getControl().repaintCurrentArea();
                    });
                    return cmi;
                })
                .collect(Collectors.toList());
        MenuButton button = new MenuButton();
        button.setText("Filter Modes");
        button.getItems().addAll(filterModeItems);
        return button;
    }

    private static Button getStateInfoButton(SwtJfxTimeGraphViewer viewer) {
        Button button = new Button("Get Info");
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
