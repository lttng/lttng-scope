/*******************************************************************************
 * Copyright (c) 2016 EfficiOS Inc., Alexandre Montplaisir
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.lttng.scope.views.timeline.widgets.timegraph;

import com.efficios.jabberwocky.common.TimeRange;
import com.efficios.jabberwocky.views.timegraph.control.TimeGraphModelControl;
import com.efficios.jabberwocky.views.timegraph.model.provider.ITimeGraphModelProvider;
import com.efficios.jabberwocky.views.timegraph.model.render.tree.TimeGraphTreeRender;
import com.efficios.jabberwocky.views.timegraph.view.TimeGraphModelView;
import com.google.common.annotations.VisibleForTesting;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.control.SplitPane;
import javafx.scene.control.ToolBar;
import javafx.scene.input.InputEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lttng.scope.common.NestingBoolean;
import org.lttng.scope.views.timeline.DebugOptions;
import org.lttng.scope.views.timeline.TimelineManager;
import org.lttng.scope.views.timeline.TimelineView;
import org.lttng.scope.views.timeline.TimelineWidget;
import org.lttng.scope.views.timeline.widgets.timegraph.layer.*;
import org.lttng.scope.views.timeline.widgets.timegraph.toolbar.ViewerToolBar;

import java.util.Collection;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import static java.util.Objects.requireNonNull;

/**
 * Viewer for the {@link TimelineView}, encapsulating all the view's
 * controls.
 *
 * Both ScrolledPanes's vertical scrollbars are bound together, so that they
 * scroll together.
 *
 * @author Alexandre Montplaisir
 */
public class TimeGraphWidget extends TimeGraphModelView implements TimelineWidget {

    private static final Logger LOGGER = Logger.getLogger(TimeGraphWidget.class.getName());

    // ------------------------------------------------------------------------
    // Style definitions
    // (Could eventually be moved to separate .css file?)
    // ------------------------------------------------------------------------

    public static final Color BACKGROUD_LINES_COLOR = requireNonNull(Color.LIGHTBLUE);

    private static final String BACKGROUND_STYLE = "-fx-background-color: rgba(255, 255, 255, 255);"; //$NON-NLS-1$

    private static final int LABEL_SIDE_MARGIN = 10;

    /**
     * Height of individual entries (text + states), including padding.
     *
     * TODO Make this configurable (vertical zoom feature)
     */
    public static final double ENTRY_HEIGHT = 20;

    /** Minimum allowed zoom level, in nanos per pixel */
    private static final double ZOOM_LIMIT = 1.0;

    // ------------------------------------------------------------------------
    // Instance fields
    // ------------------------------------------------------------------------

    private final int weight;

    private final ScrollingContext fScrollingCtx = new ScrollingContext();
    private final ZoomActions fZoomActions = new ZoomActions();

    /*
     * Children of the time graph pane are split into groups, so we can easily
     * redraw or add only some of them.
     */
    // TODO Layer for bookmarks
    private final TimeGraphBackgroundLayer fBackgroundLayer;
    private final TimeGraphStateLayer fStateLayer;
    private final TimeGraphArrowLayer fArrowLayer;
    private final TimeGraphDrawnEventLayer fDrawnEventLayer;
    private final TimeGraphSelectionLayer fSelectionLayer;
    private final Group fTimeGraphLoadingOverlayGroup;

    private final LatestTaskExecutor fTaskExecutor = new LatestTaskExecutor();

    private final NestingBoolean fHScrollListenerStatus;

    private final BorderPane fBasePane;
    private final ToolBar fToolBar;
    private final SplitPane fSplitPane;

    private final TimeGraphWidgetTreeArea fTreeArea;

    private final Pane fTimeGraphPane;
    private final ScrollPane fTimeGraphScrollPane;

    private final LoadingOverlay fTimeGraphLoadingOverlay;

    private final @NotNull PeriodicRedrawTask fRedrawTask = new PeriodicRedrawTask(this);

    private volatile TimeGraphTreeRender fLatestTreeRender = TimeGraphTreeRender.EMPTY_RENDER;

    /** Current zoom level */
    private final DoubleProperty fNanosPerPixel = new SimpleDoubleProperty(1.0);

    /**
     * Constructor
     *
     * @param control
     *            The control for this widget. See
     *            {@link TimeGraphModelControl}.
     * @param hScrollListenerStatus
     *            If the hscroll property of this widget's scrollpane is bound
     *            with others (possibly through the
     *            {@link TimelineWidget#getTimeBasedScrollPane()} method), then
     *            a common {@link NestingBoolean} should be used to track
     *            requests to disable the hscroll listener.
     *            <p>
     *            If the widget is to be used stand-alone, then you can pass a "
     *            <code>new NestingBoolean()</code> " that only this view will
     *            use.
     */
    public TimeGraphWidget(TimeGraphModelControl control, NestingBoolean hScrollListenerStatus, int weight) {
        super(control);
        this.weight = weight;
        fHScrollListenerStatus = hScrollListenerStatus;

        // --------------------------------------------------------------------
        // Prepare the tree part's scene graph
        // --------------------------------------------------------------------

        fTreeArea = new TimeGraphWidgetTreeArea(ENTRY_HEIGHT, getControl().getModelRenderProvider().traceProjectProperty());

        // --------------------------------------------------------------------
        // Prepare the time graph's part scene graph
        // --------------------------------------------------------------------

        fTimeGraphLoadingOverlay = new LoadingOverlay(getDebugOptions());
        fTimeGraphLoadingOverlayGroup = new Group(fTimeGraphLoadingOverlay);

        fTimeGraphPane = new Pane();
        fBackgroundLayer = new TimeGraphBackgroundLayer(this, new Group());
        fStateLayer = new TimeGraphStateLayer(this, new Group());
        fArrowLayer = new TimeGraphArrowLayer(this, new Group());
        fDrawnEventLayer = new TimeGraphDrawnEventLayer(this, new Group());
        fSelectionLayer = new TimeGraphSelectionLayer(this, new Group());

        /*
         * The order of the layers is important here, it will go from back to
         * front.
         */
        fTimeGraphPane.getChildren().addAll(fBackgroundLayer.getParentGroup(),
                fStateLayer.getParentGroup(),
                fStateLayer.getLabelGroup(),
                fArrowLayer.getParentGroup(),
                fDrawnEventLayer.getParentGroup(),
                fSelectionLayer.getParentGroup(),
                fTimeGraphLoadingOverlayGroup);

        fTimeGraphPane.setStyle(BACKGROUND_STYLE);

        /*
         * We control the width of the time graph pane programmatically, so
         * ensure that calls to setPrefWidth set the actual width right away.
         */
        fTimeGraphPane.minWidthProperty().bind(fTimeGraphPane.prefWidthProperty());
        fTimeGraphPane.maxWidthProperty().bind(fTimeGraphPane.prefWidthProperty());

        /*
         * Ensure the time graph pane is always exactly the same vertical size
         * as the tree pane, so they remain aligned.
         */

        fTimeGraphPane.minHeightProperty().bind(fTreeArea.currentHeightProperty());
        fTimeGraphPane.prefHeightProperty().bind(fTreeArea.currentHeightProperty());
        fTimeGraphPane.maxHeightProperty().bind(fTreeArea.currentHeightProperty());

        /*
         * Setup clipping on the timegraph pane, meaning its children outside of its
         * actual boundary should not be rendered. For example, when the tree gets
         * collapsed, data for hidden entries should be hidden too.
         */
        Rectangle clipRect = new Rectangle();
        clipRect.setX(0);
        clipRect.setY(0);
        clipRect.widthProperty().bind(fTimeGraphPane.widthProperty());
        clipRect.heightProperty().bind(fTimeGraphPane.heightProperty());
        fTimeGraphPane.setClip(clipRect);

        /*
         * Set the loading overlay's size to always follow the size of the pane.
         */
        fTimeGraphLoadingOverlay.widthProperty().bind(fTimeGraphPane.widthProperty());
        fTimeGraphLoadingOverlay.heightProperty().bind(fTimeGraphPane.heightProperty());

        fTimeGraphScrollPane = new ScrollPane(fTimeGraphPane);
        fTimeGraphScrollPane.setVbarPolicy(ScrollBarPolicy.ALWAYS);
        fTimeGraphScrollPane.setHbarPolicy(ScrollBarPolicy.ALWAYS);
        fTimeGraphScrollPane.setFitToHeight(true);
        fTimeGraphScrollPane.setFitToWidth(true);
        fTimeGraphScrollPane.setPannable(true);

        /*
         * Attach the scrollbar listener
         *
         * TODO Move this to the timeline ?
         */
        fTimeGraphScrollPane.hvalueProperty().addListener(fScrollingCtx.fHScrollChangeListener);

        /*
         * Mouse scroll handlers (for zooming) are attached to the time graph
         * itself: events let through will be used by the scrollpane as normal
         * scroll actions.
         */
        fTimeGraphPane.setOnScroll(fMouseScrollListener);

        /*
         * Upon reception of any mouse/keyboard event, if there's still a drawn
         * tooltip it should be hidden.
         */
        fTimeGraphPane.addEventFilter(InputEvent.ANY, e -> {
            StateRectangle selectedState = fSelectedState;
            if (selectedState != null) {
                selectedState.hideTooltip();
            }
            /* We must not consume the event here */
        });

        /* Synchronize the two scrollpanes' vertical scroll bars together */
        fTreeArea.getVerticalScrollBar().valueProperty().bindBidirectional(fTimeGraphScrollPane.vvalueProperty());

        // --------------------------------------------------------------------
        // Prepare the top-level area
        // --------------------------------------------------------------------

        fToolBar = new ViewerToolBar(this);

        fSplitPane = new SplitPane(fTreeArea, fTimeGraphScrollPane);
        fSplitPane.setOrientation(Orientation.HORIZONTAL);

        fBasePane = new BorderPane();
        fBasePane.setCenter(fSplitPane);
        fBasePane.setTop(fToolBar);
    }

    public TimeGraphTreeRender getLatestTreeRender() {
        return fLatestTreeRender;
    }

    // ------------------------------------------------------------------------
    // ITimelineWidget
    // ------------------------------------------------------------------------

    @Override
    public String getName() {
        return getControl().getModelRenderProvider().getName();
    }

    @Override
    public int getWeight() {
        return weight;
    }

    @Override
    public @NotNull PeriodicRedrawTask getTimelineWidgetUpdateTask() {
        return fRedrawTask;
    }

    @Override
    public Parent getRootNode() {
        return fBasePane;
    }

    @Override
    public @NotNull SplitPane getSplitPane() {
        return fSplitPane;
    }

    @Override
    public @NotNull ScrollPane getTimeBasedScrollPane() {
        return fTimeGraphScrollPane;
    }

    @Override
    public @Nullable Rectangle getSelectionRectangle() {
        return fSelectionLayer.getSelectionRectangle();
    }

    @Override
    public @Nullable Rectangle getOngoingSelectionRectangle() {
        return fSelectionLayer.getOngoingSelectionRectangle();
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    @Override
    public void disposeImpl() {
    }

    @Override
    public void clear() {
        Platform.runLater(() -> {
            /*
             * Clear the generated children of the various groups so they go
             * back to their initial (post-constructor) state.
             */
            fTreeArea.clear();

            fBackgroundLayer.clear();
            fStateLayer.clear();
            fArrowLayer.clear();
            fDrawnEventLayer.clear();

            /* Also clear whatever cached objects the viewer currently has. */
            fLatestTreeRender = TimeGraphTreeRender.EMPTY_RENDER;
            getTimelineWidgetUpdateTask().forceRedraw();
        });
    }

    @Override
    public void seekVisibleRange(@Nullable TimeRange newVisibleRange) {
        requireNonNull(newVisibleRange);
        final TimeRange fullTimeGraphRange = getViewContext().getCurrentProjectFullRange();

        /* Update the zoom level */
        long windowTimeRange = newVisibleRange.getDuration();
        double timeGraphVisibleWidth = fTimeGraphScrollPane.getViewportBounds().getWidth();
        if (timeGraphVisibleWidth < 100) {
            /*
             * The view's width is reported as 0 if the widget is not yet part of the
             * scenegraph. Instead target a larger width so that we obtain a value of
             * nanos-per-pixel that makes sense.
             */
            timeGraphVisibleWidth = 2000;
        }
        fNanosPerPixel.set(windowTimeRange / timeGraphVisibleWidth);

        double oldTotalWidth = fTimeGraphPane.getLayoutBounds().getWidth();
        double newTotalWidth = timestampToPaneXPos(fullTimeGraphRange.getEndTime()) - timestampToPaneXPos(fullTimeGraphRange.getStartTime());
        if (newTotalWidth < 1.0) {
            // FIXME
            return;
        }

        double newValue;
        if (newVisibleRange.getStartTime() == fullTimeGraphRange.getStartTime()) {
            newValue = fTimeGraphScrollPane.getHmin();
        } else if (newVisibleRange.getEndTime() == fullTimeGraphRange.getEndTime()) {
            newValue = fTimeGraphScrollPane.getHmax();
        } else {
            /*
             * The "hvalue" is in reference to the beginning of the pane, not
             * the middle point as one could think.
             *
             * Also note that the "scrollable distance" is not simply
             * "timeGraphTotalWidth", it's
             * "timeGraphTotalWidth - timeGraphVisibleWidth". The view does not
             * allow scrolling the start and end edges up to the middle point
             * for example.
             *
             * See http://stackoverflow.com/a/23518314/4227853 for a great
             * explanation.
             */
            double startPos = timestampToPaneXPos(newVisibleRange.getStartTime());
            newValue = startPos / (newTotalWidth - timeGraphVisibleWidth);
        }

        fHScrollListenerStatus.disable();
        try {

            /*
             * If the zoom level changed, resize the pane and relocate its
             * current contents. That way the "intermediate" display before the
             * next repaint will continue showing correct data.
             */
            if (Math.abs(newTotalWidth - oldTotalWidth) > 0.5) {

                /* Resize/reposition the state rectangles */
                double factor = (newTotalWidth / oldTotalWidth);
                fStateLayer.getRenderedStateRectangles().forEach(rect -> {
                    rect.setLayoutX(rect.getLayoutX() * factor);
                    rect.setWidth(rect.getWidth() * factor);
                });

                /* Reposition the text labels (don't stretch them!) */
                fStateLayer.getRenderedStateLabels().forEach(text -> {
                    text.setX(text.getX() * factor);
                });

                /* Reposition the arrows */
                fArrowLayer.getRenderedArrows().forEach(arrow -> {
                    arrow.setStartX(arrow.getStartX() * factor);
                    arrow.setEndX(arrow.getEndX() * factor);
                });

                /* Reposition the drawn events */
                fDrawnEventLayer.getRenderedEvents().forEach(event -> {
                    /*
                     * Drawn events use the "translate" properties to define
                     * their position.
                     */
                    event.setTranslateX(event.getTranslateX() * factor);
                });


                /*
                 * Resize the pane itself. Remember min/max are bound to the
                 * "pref" width, so this will change the actual size right away.
                 */
                fTimeGraphPane.setPrefWidth(newTotalWidth);
                /*
                 * Since we changed the size of a child of the scrollpane, it's
                 * important to call layout() on it before setHvalue(). If we
                 * don't, the setHvalue() will apply to the old layout, and the
                 * upcoming pulse will simply revert our changes.
                 */
                fTimeGraphScrollPane.layout();
            }

            fTimeGraphScrollPane.setHvalue(newValue);

        } finally {
            fHScrollListenerStatus.enable();
        }

        /*
         * Redraw the current selection, as it may have moved if we changed the
         * size of the pane.
         */
        redrawSelection();
    }

    /**
     *
     * Paint the specified view area.
     *
     * @param windowRange
     *            The horizontal position where the visible window currently is
     * @param verticalPos
     *            The vertical position where the visible window currently is
     * @param movedHorizontally
     *            If we have moved horizontally since the last redraw. May be
     *            used to skip some operations. If you are not sure say "true".
     * @param movedVertically
     *            If we have moved vertically since the last redraw. May be used
     *            to skip some operations. If you are not sure say "true".
     * @param taskSeqNb
     *            The sequence number of this task, used for logging only
     */
    void paintArea(TimeRange windowRange, VerticalPosition verticalPos,
            boolean movedHorizontally, boolean movedVertically,
            long taskSeqNb) {
        final TimeRange fullTimeGraphRange = getViewContext().getCurrentProjectFullRange();

        /*
         * Request the needed renders and prepare the corresponding UI objects.
         * We may ask for some padding on each side, clamped by the trace's
         * start and end.
         */
        final long timeRangePadding = Math.round(windowRange.getDuration() * getDebugOptions().renderRangePadding.get());
        final long renderingStartTime = Math.max(fullTimeGraphRange.getStartTime(), windowRange.getStartTime() - timeRangePadding);
        final long renderingEndTime = Math.min(fullTimeGraphRange.getEndTime(), windowRange.getEndTime() + timeRangePadding);
        final TimeRange renderingRange = TimeRange.of(renderingStartTime, renderingEndTime);

        /*
         * Start a new repaint, display the "loading" overlay. The next
         * paint task to finish will put it back to non-visible.
         */
        if (getDebugOptions().isLoadingOverlayEnabled.get()) {
            fTimeGraphLoadingOverlay.fadeIn();
        }

        Task<Void> task = new Task<Void>() {
            @Override
            protected @Nullable Void call() {
                LOGGER.finer(() -> "Starting paint task #" + taskSeqNb); //$NON-NLS-1$

                ITimeGraphModelProvider modelProvider = getControl().getModelRenderProvider();
                TimeGraphTreeRender treeRender = modelProvider.getTreeRender();

                if (isCancelled()) {
                    return null;
                }

                /* Prepare the tree part, if needed */
                if (!treeRender.equals(fLatestTreeRender)) {
                    fLatestTreeRender = treeRender;
                    fTreeArea.updateTreeContents(treeRender);
                }

                if (isCancelled()) {
                    return null;
                }

                /* Paint the background. It's very quick so we can do it every time. */
                fBackgroundLayer.drawContents(treeRender, renderingRange, verticalPos, this);

                /*
                 * The state rectangles should be redrawn as soon as we move,
                 * either horizontally or vertically.
                 */
                fStateLayer.setWindowRange(windowRange);
                fStateLayer.drawContents(treeRender, renderingRange, verticalPos, this);

                if (isCancelled()) {
                    return null;
                }

                /*
                 * Arrows and drawn events are drawn for the full vertical
                 * range. Only refetch/repaint them if we moved horizontally.
                 */
                if (movedHorizontally) {
                    fArrowLayer.drawContents(treeRender, renderingRange, verticalPos, this);
                    fDrawnEventLayer.drawContents(treeRender, renderingRange, verticalPos, this);
                }

                if (isCancelled()) {
                    return null;
                }

                /* Painting is finished, turn off the loading overlay */
                Platform.runLater(() -> {
                    LOGGER.finest(() -> "fading out overlay"); //$NON-NLS-1$
                    fTimeGraphLoadingOverlay.fadeOut();
                    if (fRepaintLatch != null) {
                        fRepaintLatch.countDown();
                    }
                });

                return null;
            }
        };

        LOGGER.finer(() -> "Queueing task #" + taskSeqNb); //$NON-NLS-1$

        /*
         * Attach a listener to the task to receive exceptions thrown within the
         * task.
         */
        task.exceptionProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                newVal.printStackTrace();
            }
        });

        fTaskExecutor.schedule(task);
    }

    @Override
    public void drawSelection(TimeRange selectionRange) {
        fSelectionLayer.drawSelection(selectionRange);
    }

    private void redrawSelection() {
        TimeRange selectionRange = getViewContext().getCurrentSelectionTimeRange();
        drawSelection(selectionRange);
    }

    private @Nullable StateRectangle fSelectedState = null;

    /**
     * Set the selected state rectangle
     *
     * @param state
     *            The new selected state. It should ideally be one that's
     *            present in the scenegraph.
     * @param deselectPrevious
     *            If the previously selected interval should be unmarked as
     *            selected.
     */
    public void setSelectedState(StateRectangle state, boolean deselectPrevious) {
        @Nullable StateRectangle previousSelectedState = fSelectedState;
        if (previousSelectedState != null) {
            previousSelectedState.hideTooltip();
            if (deselectPrevious) {
                previousSelectedState.setSelected(false);
            }
        }

        state.setSelected(true);
        fSelectedState = state;
    }

    /**
     * Get the currently selected state interval
     *
     * @return The current selected state
     */
    public @Nullable StateRectangle getSelectedState() {
        return fSelectedState;
    }

    /**
     * Return all state rectangles currently present in the timegraph.
     *
     * @return The rendered state rectangles
     */
    public Collection<StateRectangle> getRenderedStateRectangles() {
        return fStateLayer.getRenderedStateRectangles();
    }

    // ------------------------------------------------------------------------
    // Mouse event listeners
    // ------------------------------------------------------------------------

    /**
     * Class encapsulating the scrolling operations of the time graph pane.
     *
     * The mouse entered/exited handlers ensure only the scrollpane being
     * interacted by the user is the one sending the synchronization signals.
     */
    private class ScrollingContext {

        /**
         * Listener for the horizontal scrollbar changes
         */
        private final ChangeListener<Number> fHScrollChangeListener = (observable, oldValue, newValue) -> {
            if (!getDebugOptions().isScrollingListenersEnabled.get()) {
                LOGGER.finest(() -> "HScroll event ignored due to debug option"); //$NON-NLS-1$
                return;
            }
            if (!fHScrollListenerStatus.enabledProperty().get()) {
                LOGGER.finest(() -> "HScroll listener triggered but inactive"); //$NON-NLS-1$
                return;
            }

            LOGGER.finest(() -> "HScroll change listener triggered, oldval=" + oldValue.toString() + ", newval=" + newValue.toString()); //$NON-NLS-1$ //$NON-NLS-2$

            /* We need to specify the new value here, or else the old one will be used */
            TimeRange range = getTimeGraphEdgeTimestamps(newValue.doubleValue());

            LOGGER.finest(() -> "Sending visible range update: " + range.toString()); //$NON-NLS-1$

            getControl().updateVisibleTimeRange(range, false);

            /*
             * We ask the control to not send this signal back to us (to avoid
             * jitter while scrolling), but the next UI update should refresh
             * the view accordingly.
             *
             * It is not our responsibility to update to this
             * HorizontalPosition. The control will update accordingly upon
             * managing the signal we just sent.
             */
        };
    }

    /**
     * Event handler attached to the *time graph pane*, to execute zooming
     * operations when the control key is down (otherwise, it just lets the even
     * bubble to the ScrollPane, which will do a standard scroll).
     */
    private final EventHandler<ScrollEvent> fMouseScrollListener = e -> {
        boolean forceUseMousePosition = false;

        if (!e.isControlDown()) {
            return;
        }

        if (e.isShiftDown()) {
            forceUseMousePosition = true;
        }
        e.consume();

        double delta = e.getDeltaY();
        boolean zoomIn = (delta > 0.0); // false means a zoom-out

        /*
         * getX() corresponds to the X position of the mouse on the time graph.
         * This is seriously awesome.
         */
        fZoomActions.zoom(zoomIn, forceUseMousePosition, e.getX());

    };

    // ------------------------------------------------------------------------
    // View-specific actions
    // These do not come from the control, but from the view itself
    // ------------------------------------------------------------------------

    /**
     * Utils class encapsulating zoom operations
     */
    public class ZoomActions {

        public void zoom(boolean zoomIn, boolean forceUseMousePosition, @Nullable Double mouseX) {
            final double zoomStep = getDebugOptions().zoomStep.get();

            double newScaleFactor = (zoomIn ? 1.0 * (1 + zoomStep) : 1.0 * (1 / (1 + zoomStep)));

            /* Send a corresponding window-range signal to the control */
            TimeGraphModelControl control = getControl();
            TimeRange visibleRange = getViewContext().getCurrentVisibleTimeRange();

            TimeRange currentSelection = getViewContext().getCurrentSelectionTimeRange();
            long currentSelectionCenter = ((currentSelection.getDuration() / 2) + currentSelection.getStartTime());
            boolean currentSelectionCenterIsVisible = visibleRange.contains(currentSelectionCenter);

            long zoomPivot;
            if (getDebugOptions().zoomPivotOnMousePosition.get() && mouseX != null && forceUseMousePosition) {
                /* Pivot on mouse position */
                zoomPivot = paneXPosToTimestamp(mouseX);
            } else if (getDebugOptions().zoomPivotOnSelection.get() && currentSelectionCenterIsVisible) {
                /* Pivot on current selection center */
                zoomPivot = currentSelectionCenter;
            } else if (getDebugOptions().zoomPivotOnMousePosition.get() && mouseX != null) {
                /* Pivot on mouse position */
                zoomPivot = paneXPosToTimestamp(mouseX);
            } else {
                /* Pivot on center of visible range */
                zoomPivot = visibleRange.getStartTime() + (visibleRange.getDuration() / 2);
            }

            /* Prevent going closer than the zoom limit */
            double timeGraphVisibleWidth = Math.max(1, fTimeGraphScrollPane.getViewportBounds().getWidth());
            double minDuration = ZOOM_LIMIT * timeGraphVisibleWidth;

            double newDuration = visibleRange.getDuration() * (1.0 / newScaleFactor);
            newDuration = Math.max(minDuration, newDuration);
            double durationDelta = newDuration - visibleRange.getDuration();
            double zoomPivotRatio = (double) (zoomPivot - visibleRange.getStartTime()) / (double) (visibleRange.getDuration());

            long newStart = visibleRange.getStartTime() - Math.round(durationDelta * zoomPivotRatio);
            long newEnd = visibleRange.getEndTime() + Math.round(durationDelta - (durationDelta * zoomPivotRatio));

            /* Clamp newStart and newEnd to the full trace's range */
            TimeRange fullRange = control.getViewContext().getCurrentProjectFullRange();
            long traceStart = fullRange.getStartTime();
            long traceEnd = fullRange.getEndTime();
            newStart = Math.max(newStart, traceStart);
            newEnd = Math.min(newEnd, traceEnd);

            control.updateVisibleTimeRange(TimeRange.of(newStart, newEnd), true);
        }

    }

    /**
     * Get the viewer's zoom actions
     *
     * @return The zoom actions
     */
    public ZoomActions getZoomActions() {
        return fZoomActions;
    }

    // ------------------------------------------------------------------------
    // Common utils
    // ------------------------------------------------------------------------

    /**
     * Determine the timestamps currently represented by the left and right
     * edges of the time graph pane. In other words, the current "visible range"
     * the view is showing.
     *
     * Note that this method gets its information from UI objects only, so there
     * might be discrepancies between this and the results of
     * {@link ViewGroupContext#getVisibleTimeRange()}.
     *
     * @param newHValue
     *            The "hvalue" property of the horizontal scrollbar to use. If
     *            null, the current value will be retrieved from the scenegraph
     *            object. For example, a scrolling listener might want to pass
     *            its newValue here, since the scenegraph object will not have
     *            been updated yet.
     * @return The corresponding time range
     */
    TimeRange getTimeGraphEdgeTimestamps(@Nullable Double newHValue) {
        double hvalue = (newHValue == null ? fTimeGraphScrollPane.getHvalue() : newHValue.doubleValue());

        /*
         * Determine the X positions represented by the edges.
         */
        double hmin = fTimeGraphScrollPane.getHmin();
        double hmax = fTimeGraphScrollPane.getHmax();
        double contentWidth = fTimeGraphPane.getLayoutBounds().getWidth();
        double viewportWidth = fTimeGraphScrollPane.getViewportBounds().getWidth();
        double hoffset = Math.max(0, contentWidth - viewportWidth) * (hvalue - hmin) / (hmax - hmin);

        /*
         * Convert the positions of the left and right edges to timestamps.
         */
        long tsStart = paneXPosToTimestamp(hoffset);
        long tsEnd = paneXPosToTimestamp(hoffset + viewportWidth);

        return TimeRange.of(tsStart, tsEnd);
    }

    public double timestampToPaneXPos(long timestamp) {
        TimeRange fullTimeGraphRange = getViewContext().getCurrentProjectFullRange();
        return timestampToPaneXPos(timestamp, fullTimeGraphRange, fNanosPerPixel.get());
    }

    @VisibleForTesting
    static double timestampToPaneXPos(long timestamp, TimeRange fullTimeGraphRange, double nanosPerPixel) {
        long start = fullTimeGraphRange.getStartTime();
        long end = fullTimeGraphRange.getEndTime();

        if (timestamp < start) {
            throw new IllegalArgumentException(timestamp + " is smaller than trace start time " + start); //$NON-NLS-1$
        }
        if (timestamp > end) {
            throw new IllegalArgumentException(timestamp + " is greater than trace end time " + end); //$NON-NLS-1$
        }

        double traceDuration = fullTimeGraphRange.getDuration();
        double timeStampRatio = (timestamp - start) / traceDuration;

        long fullTraceWidthInPixels = (long) (traceDuration / nanosPerPixel);
        double xPos = fullTraceWidthInPixels * timeStampRatio;
        return Math.round(xPos);
    }

    public long paneXPosToTimestamp(double x) {
        long fullTimeGraphStartTime = getViewContext().getCurrentProjectFullRange().getStartTime();
        return paneXPosToTimestamp(x, fTimeGraphPane.getWidth(), fullTimeGraphStartTime, fNanosPerPixel.get());
    }

    @VisibleForTesting
    static long paneXPosToTimestamp(double x, double totalWidth, long startTimestamp, double nanosPerPixel) {
        if (x < 0.0 || totalWidth < 1.0 || x > totalWidth) {
            throw new IllegalArgumentException("Invalid position arguments: pos=" + x + ", width=" + totalWidth);
        }

        long ts = Math.round(x * nanosPerPixel);
        return ts + startTimestamp;
    }

    /**
     * Get the current vertical position of the timegraph.
     *
     * @return The corresponding VerticalPosition
     */
    VerticalPosition getCurrentVerticalPosition() {
        double vvalue = fTimeGraphScrollPane.getVvalue();

        /* Get the Y position of the top/bottom edges of the pane */
        double vmin = fTimeGraphScrollPane.getVmin();
        double vmax = fTimeGraphScrollPane.getVmax();
        double contentHeight = fTimeGraphPane.getLayoutBounds().getHeight();
        double viewportHeight = fTimeGraphScrollPane.getViewportBounds().getHeight();

        double vtop = Math.max(0, contentHeight - viewportHeight) * (vvalue - vmin) / (vmax - vmin);
        double vbottom = vtop + viewportHeight;

        return new VerticalPosition(vtop, vbottom);
    }

    public static int paneYPosToEntryListIndex(double yPos, double entryHeight) {
        if (yPos < 0.0 || entryHeight < 0.0) {
            throw new IllegalArgumentException();
        }

        return (int) (yPos / entryHeight);
    }

    // ------------------------------------------------------------------------
    // Test accessors
    // ------------------------------------------------------------------------

    private volatile @Nullable CountDownLatch fRepaintLatch = null;

    @VisibleForTesting
    void prepareWaitForRepaint() {
        if (fRepaintLatch != null) {
            throw new IllegalStateException("Do not call this method concurrently!"); //$NON-NLS-1$
        }
        fRepaintLatch = new CountDownLatch(1);
    }

    @VisibleForTesting
    boolean waitForRepaint() {
        CountDownLatch latch = fRepaintLatch;
        boolean done = false;
        if (latch == null) {
            throw new IllegalStateException("Do not call this method concurrently!"); //$NON-NLS-1$
        }
        try {
            done = latch.await(100, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
        }
        if (done) {
            fRepaintLatch = null;
        }
        return done;
    }

    /**
     * Bypass the redraw thread and do a manual redraw of the current location.
     */
    @VisibleForTesting
    void paintCurrentLocation() {
        TimeRange currentHorizontalPos = getViewContext().getCurrentVisibleTimeRange();
        VerticalPosition currentVerticalPos = getCurrentVerticalPosition();
        paintArea(currentHorizontalPos, currentVerticalPos, true, true, 0);
    }

    // could eventually be exposed to the user, as "advanced preferences"
    public DebugOptions getDebugOptions() {
        return TimelineManager.DEBUG_OPTIONS;
    }

    public double getCurrentNanosPerPixel() {
        return fNanosPerPixel.get();
    }

    public Pane getTimeGraphPane() {
        return fTimeGraphPane;
    }

    @VisibleForTesting
    ScrollPane getTimeGraphScrollPane() {
        return fTimeGraphScrollPane;
    }

    @VisibleForTesting
    TimeGraphArrowLayer getArrowLayer() {
        return fArrowLayer;
    }

    @VisibleForTesting
    TimeGraphDrawnEventLayer getDrawnEventLayer() {
        return fDrawnEventLayer;
    }
}
