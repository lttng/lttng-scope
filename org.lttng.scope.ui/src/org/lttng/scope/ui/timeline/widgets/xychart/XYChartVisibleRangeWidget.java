/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.ui.timeline.widgets.xychart;

import java.util.Objects;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.lttng.scope.ui.timeline.TimelineWidget;

import com.efficios.jabberwocky.common.TimeRange;
import com.efficios.jabberwocky.context.ViewGroupContext;
import com.efficios.jabberwocky.views.xychart.control.XYChartControl;
import com.efficios.jabberwocky.views.xychart.model.provider.XYChartModelProvider;
import com.efficios.jabberwocky.views.xychart.model.render.XYChartRender;
import com.efficios.jabberwocky.views.xychart.view.XYChartView;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Parent;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.shape.Rectangle;

/**
 * Widget for the timeline view showing data in a XY-Chart. The contents of the
 * chart will follow the frmework's current visible range and update its display
 * accordingly.
 */
public final class XYChartVisibleRangeWidget implements XYChartView, TimelineWidget {

    private final XYChartControl control;
    private final XYChartModelProvider modelProvider;

    // TODO Temp, should be moved to ScrollPane?
    private final BorderPane basePane;
    private final SplitPane splitPane;
    private final BorderPane chartArea;
    private final XYChart<Number, Number> chart;

    private final RedrawTask redrawTask = new RedrawTask();

    public XYChartVisibleRangeWidget(XYChartControl control) {
        this.control = control;
        this.modelProvider = control.getRenderProvider();

        modelProvider.getSeries().get(0).getLineStyle();

        NumberAxis xAxis = new NumberAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setAutoRanging(false);
        xAxis.setTickMarkVisible(false);
        xAxis.setTickUnit(0);
        yAxis.setAutoRanging(true);
        yAxis.setTickLabelsVisible(true);

        chart = new AreaChart<>(xAxis, yAxis, null);
        chart.setTitle(null);
        chart.setLegendVisible(false);
        chart.setAnimated(false);

        Label infoAreaLabel = new Label(getName());
        BorderPane infoArea = new BorderPane(infoAreaLabel);
        chartArea = new BorderPane(chart);
        splitPane = new SplitPane(infoArea, chartArea);
        basePane = new BorderPane(splitPane);
    }

    // ------------------------------------------------------------------------
    // ITimelineWidget
    // ------------------------------------------------------------------------

    @Override
    public String getName() {
        return getControl().getRenderProvider().getProviderName();
    }

    @Override
    public Parent getRootNode() {
        return basePane;
    }

    @Override
    public void dispose() {
    }

    @Override
    public @Nullable TimelineWidgetUpdateTask getTimelineWidgetUpdateTask() {
        return redrawTask;
    }

    @Override
    public @NonNull SplitPane getSplitPane() {
        return splitPane;
    }

    @Override
    public @Nullable ScrollPane getTimeBasedScrollPane() {
        /*
         * Even though the chart updates its data according to the time range, it is not
         * done by using a scroll pane.
         */
        return null;
    }

    @Override
    public @Nullable Rectangle getSelectionRectangle() {
        // TODO
        return null;
    }

    @Override
    public @Nullable Rectangle getOngoingSelectionRectangle() {
        // TODO
        return null;
    }

    // ------------------------------------------------------------------------
    // XYChartView
    // ------------------------------------------------------------------------

    @Override
    public XYChartControl getControl() {
        return control;
    }

    @Override
    public ViewGroupContext getViewContext() {
        return getControl().getViewContext();
    }

    @Override
    public void clear() {
//        Platform.runLater(() -> parentPane.getChildren().clear());
    }

    @Override
    public void drawSelection(@NonNull TimeRange arg0) {
        // TODO
    }

    @Override
    public void seekVisibleRange(@NonNull TimeRange timeRange) {

    }

    private class RedrawTask implements TimelineWidget.TimelineWidgetUpdateTask {

        private TimeRange previousVisibleRange = ViewGroupContext.UNINITIALIZED_RANGE;

        @Override
        public void run() {
            TimeRange newVisibleRange = getViewContext().getCurrentVisibleTimeRange();
            if (Objects.equals(newVisibleRange, previousVisibleRange)) {
                return;
            }

            /* Paint a new chart */
            double viewWidth = chartArea.getWidth();
            long visibleRange = newVisibleRange.getDuration();
            long resolution = (long) (visibleRange / viewWidth) * 10L;
            XYChartRender render = modelProvider.generateRender(modelProvider.getSeries().get(0), newVisibleRange, resolution, null);

            ObservableList<XYChart.Data<Number, Number>> data = render.getData().stream()
                    .<XYChart.Data<Number, Number>> map(datapoint -> new XYChart.Data<>(datapoint.getX(), datapoint.getY()))
                    .collect(Collectors.toCollection(FXCollections::observableArrayList));

            Platform.runLater(() -> {
                chart.setData(FXCollections.observableArrayList());
                XYChart.Series<Number, Number> series = new XYChart.Series<>(data);
                chart.getData().add(series);

                TimeRange range = render.getRange();
                NumberAxis xAxis = (NumberAxis) chart.getXAxis();
                xAxis.setTickUnit(range.getDuration());
                xAxis.setLowerBound(range.getStartTime());
                xAxis.setUpperBound(range.getEndTime());
            });
        }

    }

}
