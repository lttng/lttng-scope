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
import com.efficios.jabberwocky.project.TraceProject;
import com.efficios.jabberwocky.views.xychart.control.XYChartControl;
import com.efficios.jabberwocky.views.xychart.model.provider.XYChartModelProvider;
import com.efficios.jabberwocky.views.xychart.model.render.XYChartRender;
import com.efficios.jabberwocky.views.xychart.view.XYChartView;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.shape.Rectangle;

/**
 * Widget for the timeline view showing data in a XY-Chart. The chart will
 * display data representing the whole trace, and will display highlighted
 * rectangles representing the current visible and selection time ranges.
 */
public final class XYChartFullRangeWidget implements XYChartView, TimelineWidget {

    private static final double CHART_HEIGHT = 50.0;

    private final int weight;

    private final XYChartControl control;
    private final XYChartModelProvider modelProvider;

    private final BorderPane parentPane = new BorderPane();
    private final XYChart<Number, Number> chart;

    private final RedrawTask redrawTask = new RedrawTask();

    public XYChartFullRangeWidget(XYChartControl control, int weight) {
        this.weight = weight;
        this.control = control;
        this.modelProvider = control.getRenderProvider();

        modelProvider.getSeries().get(0).getLineStyle();

        NumberAxis xAxis = new NumberAxis();
        /* Hide the axes in the full range chart */
        xAxis.setTickMarkVisible(false);
        xAxis.setTickLabelsVisible(false);
        xAxis.setOpacity(0);

        NumberAxis yAxis = new NumberAxis();
        xAxis.setAutoRanging(false);
        xAxis.setTickMarkVisible(false);
        xAxis.setTickUnit(0);
        yAxis.setAutoRanging(true);
        yAxis.setTickLabelsVisible(false);
        yAxis.setOpacity(0);

        chart = new AreaChart<>(xAxis, yAxis, null);
//        chart.setTitle(getName());
        chart.setTitle(null);
        chart.setLegendVisible(false);
        chart.setAnimated(false);
        chart.setPadding(new Insets(-10, -10, -10, -10));
//        chart.setStyle("-fx-padding: 1px;"); //$NON-NLS-1$

        chart.setMinHeight(CHART_HEIGHT);
        chart.setPrefHeight(CHART_HEIGHT);
        chart.setMaxHeight(CHART_HEIGHT);

        parentPane.setCenter(chart);
    }

    // ------------------------------------------------------------------------
    // ITimelineWidget
    // ------------------------------------------------------------------------

    @Override
    public String getName() {
        return getControl().getRenderProvider().getProviderName();
    }

    @Override
    public int getWeight() {
        return weight;
    }

    @Override
    public Parent getRootNode() {
        return parentPane;
    }

    @Override
    public void dispose() {
    }

    @Override
    public @NonNull TimelineWidgetUpdateTask getTimelineWidgetUpdateTask() {
        return redrawTask;
    }

    @Override
    public @Nullable SplitPane getSplitPane() {
        /* Not applicable to this widget */
        return null;
    }

    @Override
    public @Nullable ScrollPane getTimeBasedScrollPane() {
        /* Not applicable to this widget */
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

        private @Nullable TraceProject<?, ?> lastTraceProject = null;

        @Override
        public void run() {
            TraceProject<?, ?> newTraceProject = getControl().getViewContext().getCurrentTraceProject();
            if (!Objects.equals(newTraceProject, lastTraceProject)) {
                if (newTraceProject != null) {
                    boolean painted = repaintChart(newTraceProject);
                    if (!painted) {
                        newTraceProject = null;
                    }
                }
                lastTraceProject = newTraceProject;
            }
        }

        /**
         * Repaint the whole chart. Should only be necessary if the active trace
         * changes.
         *
         * @return If we have produced a "real" render or not. If not, it might be
         *         because the trace is still being initialized, so we should not
         *         consider it painted.
         */
        private boolean repaintChart(TraceProject<?, ?> traceProject) {
            double viewWidth = parentPane.getWidth();
            TimeRange traceFullRange = TimeRange.of(traceProject.getStartTime(), traceProject.getEndTime());
            long resolution = (long) (traceFullRange.getDuration() / viewWidth) * 1L;
            XYChartRender render = modelProvider.generateRender(modelProvider.getSeries().get(0), traceFullRange, resolution, null);
            if (render == XYChartRender.Companion.getEMPTY_RENDER()) {
                return false;
            }

            ObservableList<XYChart.Data<Number, Number>> data = render.getData().stream()
                    .<XYChart.Data<Number, Number>> map(datapoint -> new XYChart.Data<>(datapoint.getX(), datapoint.getY()))
                    /* Hide the symbols */
                    .peek(xyData -> {
                        Rectangle symbol = new Rectangle(0,0);
                        symbol.setVisible(false);
                        xyData.setNode(symbol);
                    })
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

            return true;
        }

    }

}
