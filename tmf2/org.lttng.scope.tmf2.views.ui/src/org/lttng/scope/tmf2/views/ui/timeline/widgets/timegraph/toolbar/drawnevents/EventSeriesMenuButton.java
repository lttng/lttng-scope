/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.tmf2.views.ui.timeline.widgets.timegraph.toolbar.drawnevents;

import java.util.Optional;

import org.eclipse.jdt.annotation.Nullable;
import org.lttng.scope.tmf2.views.core.timegraph.model.provider.ITimeGraphModelProvider;
import org.lttng.scope.tmf2.views.core.timegraph.model.provider.drawnevents.ITimeGraphDrawnEventProvider;
import org.lttng.scope.tmf2.views.core.timegraph.model.provider.drawnevents.TimeGraphDrawnEventProviderManager;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.drawnevents.TimeGraphDrawnEventSeries;
import org.lttng.scope.tmf2.views.ui.jfx.JfxColorFactory;
import org.lttng.scope.tmf2.views.ui.jfx.JfxUtils;
import org.lttng.scope.tmf2.views.ui.timeline.widgets.timegraph.TimeGraphWidget;
import org.lttng.scope.tmf2.views.ui.timeline.widgets.timegraph.layer.TimeGraphDrawnEventLayer;

import com.google.common.collect.Iterables;

import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.collections.SetChangeListener;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.paint.Color;
import javafx.scene.shape.Shape;

/**
 * Menu button showing listing the existing drawn-event providers, with menu
 * items to create/clear such providers.
 *
 * TODO This button and its related actions are independent from a time graph
 * widget. They could be moved elsewhere in the UI at some point.
 *
 * @author Alexandre Montplaisir
 */
public class EventSeriesMenuButton extends MenuButton {

    private static final TimeGraphDrawnEventProviderManager PROVIDER_MANAGER =
            TimeGraphDrawnEventProviderManager.instance();

    /**
     * Constructor
     *
     * @param widget
     *            Time graph widget to which this button refers
     */
    public EventSeriesMenuButton(TimeGraphWidget widget) {
        setText(Messages.eventSeriesMenuButtonName);

        MenuItem separator = new SeparatorMenuItem();

        /*
         * There are minimum 3 items in the menu. Whenever there are more (from
         * registered providers), make the separator visible.
         */
        getItems().addListener((ListChangeListener<MenuItem>) change -> {
            separator.visibleProperty().set(change.getList().size() > 3);
        });

        /* "Create New Series" menu item */
        MenuItem addNewSeriesMenuItem = new MenuItem(Messages.newEventSeriesMenuItem);
        addNewSeriesMenuItem.setOnAction(e -> {
            ITimeGraphModelProvider modelProvider = widget.getControl().getModelRenderProvider();
            CreateEventSeriesDialog dialog = new CreateEventSeriesDialog(modelProvider);
            dialog.setOnShowing(h -> Platform.runLater(() -> JfxUtils.centerDialogOnScreen(dialog, EventSeriesMenuButton.this)));
            Optional<@Nullable PredicateDrawnEventProvider> results = dialog.showAndWait();
            ITimeGraphDrawnEventProvider provider = results.orElse(null);
            if (provider != null) {
                PROVIDER_MANAGER.getRegisteredProviders().add(provider);
                provider.enabledProperty().set(true);
            }
        });

        /* "Clear series" menu item */
        MenuItem clearSeriesMenuItem = new MenuItem(Messages.clearEventSeriesMenuItem);
        clearSeriesMenuItem.setOnAction(e -> {
            // TODO Eventually we could track which providers were created from
            // this button/dialog, and only clear those here.
            PROVIDER_MANAGER.getRegisteredProviders().clear();
        });

        getItems().addAll(separator, addNewSeriesMenuItem, clearSeriesMenuItem);


        /* Load the already-registered providers */
        PROVIDER_MANAGER.getRegisteredProviders().forEach(this::addProviderToMenu);

        /* Watch for future addition/removal of providers */
        PROVIDER_MANAGER.getRegisteredProviders().addListener((SetChangeListener<ITimeGraphDrawnEventProvider>) change -> {
            ITimeGraphDrawnEventProvider addedProvider = change.getElementAdded();
            if (addedProvider != null) {
                addProviderToMenu(addedProvider);
            }
            ITimeGraphDrawnEventProvider removedProvider = change.getElementRemoved();
            if (removedProvider != null) {
                removeProviderFromMenu(removedProvider);
            }
        });
    }

    private void addProviderToMenu(ITimeGraphDrawnEventProvider provider) {
        CheckMenuItem item = new EventProviderMenuItem(provider);
        int index = getItems().size() - 3;
        getItems().add(index, item);
    }

    private void removeProviderFromMenu(ITimeGraphDrawnEventProvider provider) {
        MenuItem itemToRemove = Iterables
                .tryFind(getItems(), item -> {
                    return (item instanceof EventProviderMenuItem
                            && ((EventProviderMenuItem) item).getProvider().equals(provider));
                })
                .orNull();

        if (itemToRemove != null) {
            getItems().remove(itemToRemove);
        }
    }

    /**
     * Menu item that represents a particular
     * {@link ITimeGraphDrawnEventProvider}.
     */
    private static class EventProviderMenuItem extends CheckMenuItem {

        private final ITimeGraphDrawnEventProvider fProvider;

        public EventProviderMenuItem(ITimeGraphDrawnEventProvider provider) {
            fProvider = provider;

            TimeGraphDrawnEventSeries series = provider.getEventSeries();

            setMnemonicParsing(false);
            setText(series.getSeriesName());
            selectedProperty().bindBidirectional(provider.enabledProperty());

            Shape graphic = TimeGraphDrawnEventLayer.getShapeFromSymbol(series.getSymbolStyle().get());
            Color color = JfxColorFactory.getColorFromDef(series.getColor().get());
            graphic.setFill(color);
            setGraphic(graphic);
        }

        public ITimeGraphDrawnEventProvider getProvider() {
            return fProvider;
        }
    }

}
