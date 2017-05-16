/*******************************************************************************
 * Copyright (c) 2016 EfficiOS Inc., Alexandre Montplaisir
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.lttng.scope.tmf2.views.ui.timeline.widgets.timegraph;

import static java.util.Objects.requireNonNull;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.Timer;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.lttng.scope.tmf2.views.core.NestingBoolean;
import org.lttng.scope.tmf2.views.core.TimeRange;
import org.lttng.scope.tmf2.views.core.timegraph.control.TimeGraphModelControl;
import org.lttng.scope.tmf2.views.core.timegraph.model.provider.ITimeGraphModelProvider;
import org.lttng.scope.tmf2.views.core.timegraph.model.provider.states.ITimeGraphModelStateProvider;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.states.TimeGraphStateRender;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.tree.TimeGraphTreeElement;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.tree.TimeGraphTreeRender;
import org.lttng.scope.tmf2.views.core.timegraph.view.TimeGraphModelView;
import org.lttng.scope.tmf2.views.ui.jfx.JfxUtils;
import org.lttng.scope.tmf2.views.ui.timeline.DebugOptions;
import org.lttng.scope.tmf2.views.ui.timeline.ITimelineWidget;
import org.lttng.scope.tmf2.views.ui.timeline.TimelineView;
import org.lttng.scope.tmf2.views.ui.timeline.widgets.timegraph.layer.TimeGraphArrowLayer;
import org.lttng.scope.tmf2.views.ui.timeline.widgets.timegraph.layer.TimeGraphBackgroundLayer;
import org.lttng.scope.tmf2.views.ui.timeline.widgets.timegraph.layer.TimeGraphDrawnEventLayer;
import org.lttng.scope.tmf2.views.ui.timeline.widgets.timegraph.layer.TimeGraphSelectionLayer;
import org.lttng.scope.tmf2.views.ui.timeline.widgets.timegraph.toolbar.ViewerToolBar;

import com.google.common.annotations.VisibleForTesting;

import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.control.SplitPane;
import javafx.scene.control.ToolBar;
import javafx.scene.input.InputEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

/**
 * Viewer for the {@link TimelineView}, encapsulating all the view's
 * controls.
 *
 * Both ScrolledPanes's vertical scrollbars are bound together, so that they
 * scroll together.
 *
 * @author Alexandre Montplaisir
 */
public class TimeGraphWidget extends TimeGraphModelView implements ITimelineWidget {

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

    // ------------------------------------------------------------------------
    // Instance fields
    // ------------------------------------------------------------------------

    private final DebugOptions fDebugOptions = new DebugOptions();

    private final ScrollingContext fScrollingCtx = new ScrollingContext();
    private final ZoomActions fZoomActions = new ZoomActions();
    private final TimeGraphBackgroundLayer fBackgroundLayer;
    private final TimeGraphArrowLayer fArrowLayer;
    private final TimeGraphDrawnEventLayer fDrawnEventLayer;
    private final TimeGraphSelectionLayer fSelectionLayer;

    private final LatestTaskExecutor fTaskExecutor = new LatestTaskExecutor();

    private final NestingBoolean fHScrollListenerStatus;

    private final BorderPane fBasePane;
    private final ToolBar fToolBar;
    private final SplitPane fSplitPane;

    private final Pane fTreePane;
    private final ScrollPane fTreeScrollPane;
    private final Pane fTimeGraphPane;
    private final ScrollPane fTimeGraphScrollPane;

    /*
     * Children of the time graph pane are split into groups, so we can easily
     * redraw or add only some of them.
     */
    private final Group fTimeGraphStatesLayer;
    private final Group fTimeGraphTextLabelsLayer;
    // TODO Layer for bookmarks
    private final Group fTimeGraphLoadingOverlayLayer;

    private final LoadingOverlay fTimeGraphLoadingOverlay;

    private final Timer fUiUpdateTimer = new Timer();
    private final PeriodicRedrawTask fUiUpdateTimerTask = new PeriodicRedrawTask(this);

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
     *            {@link ITimelineWidget#getTimeBasedScrollPane()} method), then
     *            a common {@link NestingBoolean} should be used to track
     *            requests to disable the hscroll listener.
     *            <p>
     *            If the widget is to be used stand-alone, then you can pass a "
     *            <code>new NestingBoolean()</code> " that only this view will
     *            use.
     */
    public TimeGraphWidget(TimeGraphModelControl control, NestingBoolean hScrollListenerStatus) {
        super(control);
        fHScrollListenerStatus = hScrollListenerStatus;

        // --------------------------------------------------------------------
        // Prepare the tree part's scene graph
        // --------------------------------------------------------------------

        fTreePane = new Pane();

        fTreeScrollPane = new ScrollPane(fTreePane);
        /* We only show the time graph's vertical scrollbar */
        fTreeScrollPane.setVbarPolicy(ScrollBarPolicy.NEVER);
        fTreeScrollPane.setHbarPolicy(ScrollBarPolicy.ALWAYS);

        fTreePane.prefWidthProperty().bind(fTreeScrollPane.widthProperty());

        // --------------------------------------------------------------------
        // Prepare the time graph's part scene graph
        // --------------------------------------------------------------------

        fTimeGraphLoadingOverlay = new LoadingOverlay(fDebugOptions);

        Group timeGraphBackgroundLayer = new Group();
        fTimeGraphStatesLayer = new Group();
        fTimeGraphTextLabelsLayer = new Group();
        Group timeGraphArrowsLayer = new Group();
        Group timeGraphDrawnEventsLayer = new Group();
        Group timeGraphSelectionLayer = new Group();
        fTimeGraphLoadingOverlayLayer = new Group(fTimeGraphLoadingOverlay);

        /*
         * The order of the layers is important here, it will go from back to
         * front.
         */
        fTimeGraphPane = new Pane(timeGraphBackgroundLayer,
                fTimeGraphStatesLayer,
                fTimeGraphTextLabelsLayer,
                timeGraphArrowsLayer,
                timeGraphDrawnEventsLayer,
                timeGraphSelectionLayer,
                fTimeGraphLoadingOverlayLayer);

        fBackgroundLayer = new TimeGraphBackgroundLayer(this, timeGraphBackgroundLayer);
        fArrowLayer = new TimeGraphArrowLayer(this, timeGraphArrowsLayer);
        fDrawnEventLayer = new TimeGraphDrawnEventLayer(this, timeGraphDrawnEventsLayer);
        fSelectionLayer = new TimeGraphSelectionLayer(this, timeGraphSelectionLayer);

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
        fTimeGraphPane.minHeightProperty().bind(fTreePane.heightProperty());
        fTimeGraphPane.prefHeightProperty().bind(fTreePane.heightProperty());
        fTimeGraphPane.maxHeightProperty().bind(fTreePane.heightProperty());

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
        fTreeScrollPane.vvalueProperty().bindBidirectional(fTimeGraphScrollPane.vvalueProperty());

        // --------------------------------------------------------------------
        // Prepare the top-level area
        // --------------------------------------------------------------------

        fToolBar = new ViewerToolBar(this);

        fSplitPane = new SplitPane(fTreeScrollPane, fTimeGraphScrollPane);
        fSplitPane.setOrientation(Orientation.HORIZONTAL);

        fBasePane = new BorderPane();
        fBasePane.setCenter(fSplitPane);
        fBasePane.setTop(fToolBar);

        /* Start the periodic redraw thread */
        long delay = fDebugOptions.uiUpdateDelay.get();
        fUiUpdateTimer.schedule(fUiUpdateTimerTask, delay, delay);
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
    public Parent getRootNode() {
        return fBasePane;
    }

    @Override
    public @NonNull SplitPane getSplitPane() {
        return fSplitPane;
    }

    @Override
    public @NonNull ScrollPane getTimeBasedScrollPane() {
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
        /* Stop/cleanup the redraw thread */
        fUiUpdateTimer.cancel();
        fUiUpdateTimer.purge();
    }

    @Override
    public void clear() {
        Platform.runLater(() -> {
            /*
             * Clear the generated children of the various groups so they go
             * back to their initial (post-constructor) state.
             */
            fTreePane.getChildren().clear();

            fTimeGraphStatesLayer.getChildren().clear();
            fTimeGraphTextLabelsLayer.getChildren().clear();

            fBackgroundLayer.clear();
            fArrowLayer.clear();
            fDrawnEventLayer.clear();

            /* Also clear whatever cached objects the viewer currently has. */
            fLatestTreeRender = TimeGraphTreeRender.EMPTY_RENDER;
            fUiUpdateTimerTask.forceRedraw();
        });
    }

    @Override
    public void seekVisibleRange(TimeRange newVisibleRange) {
        final TimeRange fullTimeGraphRange = getViewContext().getCurrentTraceFullRange();

        /* Update the zoom level */
        long windowTimeRange = newVisibleRange.getDuration();
        double timeGraphVisibleWidth = fTimeGraphScrollPane.getViewportBounds().getWidth();
        /* Clamp the width to 1 px (0 is reported if the view is not visible) */
        timeGraphVisibleWidth = Math.max(1, timeGraphVisibleWidth);
        fNanosPerPixel.set(windowTimeRange / timeGraphVisibleWidth);

        double oldTotalWidth = fTimeGraphPane.getLayoutBounds().getWidth();
        double newTotalWidth = timestampToPaneXPos(fullTimeGraphRange.getEnd()) - timestampToPaneXPos(fullTimeGraphRange.getStart());
        if (newTotalWidth < 1.0) {
            // FIXME
            return;
        }

        double newValue;
        if (newVisibleRange.getStart() == fullTimeGraphRange.getStart()) {
            newValue = fTimeGraphScrollPane.getHmin();
        } else if (newVisibleRange.getEnd() == fullTimeGraphRange.getEnd()) {
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
            double startPos = timestampToPaneXPos(newVisibleRange.getStart());
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
                getRenderedStateRectangles().forEach(rect -> {
                    rect.setX(rect.getX() * factor);
                    rect.setWidth(rect.getWidth() * factor);
                });

                /* Reposition the text labels (don't stretch them!) */
                getRenderedStateLabels().forEach(text -> {
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
        final TimeRange fullTimeGraphRange = getViewContext().getCurrentTraceFullRange();

        /*
         * Request the needed renders and prepare the corresponding UI objects.
         * We may ask for some padding on each side, clamped by the trace's
         * start and end.
         */
        final long timeRangePadding = Math.round(windowRange.getDuration() * fDebugOptions.renderRangePadding.get());
        final long renderingStartTime = Math.max(fullTimeGraphRange.getStart(), windowRange.getStart() - timeRangePadding);
        final long renderingEndTime = Math.min(fullTimeGraphRange.getEnd(), windowRange.getEnd() + timeRangePadding);
        final TimeRange renderingRange = TimeRange.of(renderingStartTime, renderingEndTime);
        final long resolution = Math.max(1, Math.round(fNanosPerPixel.get()));

        /*
         * Start a new repaint, display the "loading" overlay. The next
         * paint task to finish will put it back to non-visible.
         */
        if (getDebugOptions().isLoadingOverlayEnabled.get()) {
            fTimeGraphLoadingOverlay.fadeIn();
        }

        Task<@Nullable Void> task = new Task<@Nullable Void>() {
            @Override
            protected @Nullable Void call() {
                long start = System.nanoTime();
                System.err.println("Starting paint task #" + taskSeqNb);

                /*
                 * The state rectangles are redrawn as soon as we move, either
                 * horizontally or vertically.
                 */

                ITimeGraphModelProvider modelProvider = getControl().getModelRenderProvider();
                ITimeGraphModelStateProvider stateProvider = modelProvider.getStateProvider();
                TimeGraphTreeRender treeRender = modelProvider.getTreeRender();
                final List<TimeGraphTreeElement> allTreeElements = treeRender.getAllTreeElements();

                if (isCancelled()) {
                    System.err.println("task #" + taskSeqNb + " was cancelled before generating the states");
                    return null;
                }

                long afterTreeRender = System.nanoTime();

                final int nbElements = allTreeElements.size();

                int entriesToPrefetch = fDebugOptions.entryPadding.get();
                int topEntry = Math.max(0,
                        paneYPosToEntryListIndex(verticalPos.fTopPos, ENTRY_HEIGHT) - entriesToPrefetch);
                int bottomEntry = Math.min(nbElements,
                        paneYPosToEntryListIndex(verticalPos.fBottomPos, ENTRY_HEIGHT) + entriesToPrefetch);

                System.out.println("topEntry=" + topEntry +", bottomEntry=" + bottomEntry);

                List<TimeGraphStateRender> stateRenders = allTreeElements.subList(topEntry, bottomEntry).stream()
                        .map(treeElem -> stateProvider.getStateRender(treeElem, renderingRange, resolution, this))
                        .collect(Collectors.toList());

                if (isCancelled()) {
                    System.err.println("task #" + taskSeqNb + " was cancelled before generating the contents");
                    return null;
                }

                long afterStateRenders = System.nanoTime();

                /* Prepare the tree part, if needed */
                @Nullable Node treeContents;
                if (treeRender.equals(fLatestTreeRender)) {
                    treeContents = null;
                } else {
                    fLatestTreeRender = treeRender;
                    treeContents = prepareTreeContents(treeRender, fTreePane.widthProperty());
                }

                /* We can paint the background at this stage. */
                fBackgroundLayer.paintBackground(renderingRange, verticalPos);

                /* Prepare the time graph part */
                Collection<StateRectangle> stateRectangles = prepareStateRectangles(stateRenders, topEntry);
                Node statesLayerContents = prepareTimeGraphStatesContents(stateRectangles);
                Node labelsLayerContents = prepareTimeGrahLabelsContents(stateRectangles, windowRange);

                /*
                 * Go over all state rectangles, and bring the "multi-state"
                 * ones to the front, to be sure they show on top of the others.
                 * Note we cannot do the forEach() as part of the stream, that
                 * would throw a ConcurrentModificationException.
                 */
                ((Group) statesLayerContents).getChildren().stream()
                        .map(node -> (StateRectangle) node)
                        .filter(rect -> (rect.getStateInterval().isMultiState()))
                        .collect(Collectors.toList())
                        .forEach(Node::toFront);

                if (isCancelled()) {
                    System.err.println("task #" + taskSeqNb + " was cancelled before updating the view");
                    return null;
                }

                long afterJavaFXObjects = System.nanoTime();

                StringJoiner sj = new StringJoiner(", ", "Repaint breakdown (#" + taskSeqNb + "): ", "")
                        .add("Generating tree render=" + String.format("%,d", afterTreeRender - start) + " ns")
                        .add("Generating state renders= " + String.format("%,d", afterStateRenders - afterTreeRender) + " ns")
                        .add("Generating JavaFX objects=" + String.format("%,d", afterJavaFXObjects - afterStateRenders) + " ns");
                System.err.println(sj.toString());

                /* Update the view! */
                Platform.runLater(() -> {
                    long startUI = System.nanoTime();
                    if (treeContents != null) {
                        fTreePane.getChildren().clear();
                        fTreePane.getChildren().add(treeContents);
                    }

                    long afterTreeUI = System.nanoTime();

                    fTimeGraphStatesLayer.getChildren().clear();
                    fTimeGraphTextLabelsLayer.getChildren().clear();
                    fTimeGraphStatesLayer.getChildren().add(statesLayerContents);
                    fTimeGraphTextLabelsLayer.getChildren().add(labelsLayerContents);

                    long endUI = System.nanoTime();
                    StringJoiner sjui = new StringJoiner(", ", "UI Update (#" + taskSeqNb +"): ", "")
                            .add("Drawing tree=" + String.format("%,d", afterTreeUI - startUI) + " ns")
                            .add("Drawing states= " + String.format("%,d", endUI - afterTreeUI) + " ns");
                    System.err.println(sjui.toString());
                });

                if (isCancelled()) {
                    return null;
                }

                /*
                 * Arrows and drawn events are drawn for the full vertical
                 * range. Only refetch/repaint them if we moved horizontally.
                 */
                if (movedHorizontally) {
                    fArrowLayer.paintArrows(treeRender, renderingRange, this);
                    fDrawnEventLayer.paintEvents(treeRender, renderingRange, this);
                }

                if (isCancelled()) {
                    return null;
                }

                /* Painting is finished, turn off the loading overlay */
                Platform.runLater(() -> {
                    System.err.println("fading out overlay");
                    fTimeGraphLoadingOverlay.fadeOut();
                    if (fRepaintLatch != null) {
                        fRepaintLatch.countDown();
                    }
                });

                return null;
            }
        };

        System.err.println("Queueing task #" + taskSeqNb);

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

    // ------------------------------------------------------------------------
    // Methods related to the Tree area
    // ------------------------------------------------------------------------

    private static Node prepareTreeContents(TimeGraphTreeRender treeRender, ReadOnlyDoubleProperty widthProperty) {
        /* Prepare the tree element objects */
        List<Label> treeElements = treeRender.getAllTreeElements().stream()
                // TODO Put as a real tree. TreeView ?
                .map(elem -> new Label(elem.getName()))
                .peek(label -> {
                    label.setPrefHeight(ENTRY_HEIGHT);
                    label.setPadding(new Insets(0, LABEL_SIDE_MARGIN, 0, LABEL_SIDE_MARGIN));
                    /*
                     * Re-set the solid background for the labels, so we do not
                     * see the background lines through.
                     */
                    label.setStyle(BACKGROUND_STYLE);
                })
                .collect(Collectors.toList());

        VBox treeElemsBox = new VBox(); // Change to TreeView eventually ?
        treeElemsBox.getChildren().addAll(treeElements);

        /* Prepare the background layer with the horizontal alignment lines */
        List<Line> lines = DoubleStream.iterate((ENTRY_HEIGHT / 2), y -> y + ENTRY_HEIGHT)
                .limit(treeElements.size())
                .mapToObj(y -> {
                    Line line = new Line();
                    line.startXProperty().bind(JfxUtils.ZERO_PROPERTY);
                    line.endXProperty().bind(widthProperty);
                    line.setStartY(y);
                    line.setEndY(y);

                    line.setStroke(BACKGROUD_LINES_COLOR);
                    line.setStrokeWidth(1.0);
                    return line;
                })
                .collect(Collectors.toList());
        Pane background = new Pane();
        background.getChildren().addAll(lines);

        /* Put the background layer and the Tree View into their containers */
        StackPane stackPane = new StackPane(background, treeElemsBox);
        stackPane.setStyle(BACKGROUND_STYLE);
        return stackPane;
    }

    // ------------------------------------------------------------------------
    // Methods related to the Time Graph area
    // ------------------------------------------------------------------------

    private Collection<StateRectangle> prepareStateRectangles(
            List<TimeGraphStateRender> stateRenders, int topEntry) {
        /* Prepare the colored state rectangles */
        Collection<StateRectangle> rectangles = IntStream.range(0, stateRenders.size()).parallel()
                .mapToObj(idx -> getRectanglesForStateRender(stateRenders.get(idx), idx + topEntry))
                .flatMap(Function.identity())
                .collect(Collectors.toSet());
        return rectangles;
    }

    private Stream<StateRectangle> getRectanglesForStateRender(TimeGraphStateRender stateRender, int entryIndex) {
        return stateRender.getStateIntervals().stream()
                .map(interval -> new StateRectangle(this, interval, entryIndex));
    }

    private static Node prepareTimeGraphStatesContents(Collection<StateRectangle> stateRectangles) {
        Group group = new Group();
        group.getChildren().addAll(stateRectangles);
        return group;
    }

    private Node prepareTimeGrahLabelsContents(Collection<StateRectangle> stateRectangles,
            TimeRange windowRange) {
        double minX = timestampToPaneXPos(windowRange.getStart());

        final String ellipsisStr = DebugOptions.ELLIPSIS_STRING;
        final Font textFont = fDebugOptions.stateLabelFont.get();
        final OverrunStyle overrunStyle = OverrunStyle.ELLIPSIS;
        final Color textColor = Color.WHITE;

        /* Requires a ~2 pixels adjustment to be centered on the states */
        final double yOffset = ENTRY_HEIGHT / 2.0 + 2.0;
        Collection<Node> texts = stateRectangles.stream()
                /* Only try to annotate rectangles that are large enough */
                .filter(stateRect -> stateRect.getWidth() > fDebugOptions.getEllipsisWidth())
                .filter(stateRect -> stateRect.getStateInterval().getLabel() != null)
                .map(stateRect -> {
                    String labelText = requireNonNull(stateRect.getStateInterval().getLabel());
                    /* A small offset looks better here */
                    double textX = Math.max(minX, stateRect.getX()) + 4.0;
                    double textY = stateRect.getY() + yOffset;

                    double rectEndX = stateRect.getX() + stateRect.getWidth();
                    double minWidth = rectEndX - textX;

                    String ellipsedText = JfxUtils.computeClippedText(textFont,
                            labelText,
                            minWidth,
                            overrunStyle,
                            ellipsisStr);

                    if (ellipsedText.equals(ellipsisStr)) {
                        return null;
                    }

                    Text text = new Text(textX, textY, ellipsedText);
                    text.setFont(textFont);
                    text.setFill(textColor);
                    return text;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return new Group(texts);
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
     * Retrieve the state rectangles currently present in the scenegraph. This
     * should include all currently visible ones, but also possibly more (due to
     * padding, prefetching, etc.)
     *
     * @return The state rectangles
     */
    public Collection<StateRectangle> getRenderedStateRectangles() {
        if (fTimeGraphStatesLayer.getChildren().isEmpty()) {
            return Collections.EMPTY_LIST;
        }
        Collection<?> stateRectangles = ((Group) fTimeGraphStatesLayer.getChildren().get(0)).getChildren();
        @SuppressWarnings("unchecked")
        Collection<StateRectangle> ret = (@NonNull Collection<StateRectangle>) stateRectangles;
        return ret;
    }

    private Collection<Text> getRenderedStateLabels() {
        if (fTimeGraphTextLabelsLayer.getChildren().isEmpty()) {
            return Collections.EMPTY_LIST;
        }
        /* The labels are wrapped in a group */
        Collection<?> texts = ((Group) fTimeGraphTextLabelsLayer.getChildren().get(0)).getChildren();
        @SuppressWarnings("unchecked")
        Collection<Text> ret = (@NonNull Collection<Text>) texts;
        return ret;
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
            if (!fDebugOptions.isScrollingListenersEnabled.get()) {
                System.out.println("HScroll event ignored due to debug option");
                return;
            }
            if (!fHScrollListenerStatus.enabledProperty().get()) {
                System.out.println("HScroll listener triggered but inactive");
                return;
            }

            System.out.println("HScroll change listener triggered, oldval=" + oldValue.toString() + ", newval=" + newValue.toString());

            /* We need to specify the new value here, or else the old one will be used */
            TimeRange range = getTimeGraphEdgeTimestamps(newValue.doubleValue());

            System.out.println("Sending visible range update: " + range.toString());

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
            final double zoomStep = fDebugOptions.zoomStep.get();

            double newScaleFactor = (zoomIn ? 1.0 * (1 + zoomStep) : 1.0 * (1 / (1 + zoomStep)));

            /* Send a corresponding window-range signal to the control */
            TimeGraphModelControl control = getControl();
            TimeRange visibleRange = getViewContext().getCurrentVisibleTimeRange();

            TimeRange currentSelection = getViewContext().getCurrentSelectionTimeRange();
            long currentSelectionCenter = ((currentSelection.getDuration() / 2) + currentSelection.getStart());
            boolean currentSelectionCenterIsVisible = visibleRange.contains(currentSelectionCenter);

            long zoomPivot;
            if (fDebugOptions.zoomPivotOnMousePosition.get() && mouseX != null && forceUseMousePosition) {
                /* Pivot on mouse position */
                zoomPivot = paneXPosToTimestamp(mouseX);
            } else if (fDebugOptions.zoomPivotOnSelection.get() && currentSelectionCenterIsVisible) {
                /* Pivot on current selection center */
                zoomPivot = currentSelectionCenter;
            } else if (fDebugOptions.zoomPivotOnMousePosition.get() && mouseX != null) {
                /* Pivot on mouse position */
                zoomPivot = paneXPosToTimestamp(mouseX);
            } else {
                /* Pivot on center of visible range */
                zoomPivot = visibleRange.getStart() + (visibleRange.getDuration() / 2);
            }

            double newDuration = visibleRange.getDuration() * (1.0 / newScaleFactor);
            double durationDelta = newDuration - visibleRange.getDuration();
            double zoomPivotRatio = (double) (zoomPivot - visibleRange.getStart()) / (double) (visibleRange.getDuration());

            long newStart = visibleRange.getStart() - Math.round(durationDelta * zoomPivotRatio);
            long newEnd = visibleRange.getEnd() + Math.round(durationDelta - (durationDelta * zoomPivotRatio));

            /* Clamp newStart and newEnd to the full trace's range */
            TimeRange fullRange = control.getViewContext().getCurrentTraceFullRange();
            long traceStart = fullRange.getStart();
            long traceEnd = fullRange.getEnd();
            newStart = Math.max(newStart, traceStart);
            newEnd = Math.min(newEnd, traceEnd);

            /* Keep at least 1 ns width */
            if (newStart == newEnd) {
                if (newEnd == traceEnd) {
                    newStart--;
                } else {
                    newEnd++;
                }
            }

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
     * {@link TimeGraphModelControl#getVisibleTimeRange()}.
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
        TimeRange fullTimeGraphRange = getViewContext().getCurrentTraceFullRange();
        return timestampToPaneXPos(timestamp, fullTimeGraphRange, fNanosPerPixel.get());
    }

    @VisibleForTesting
    static double timestampToPaneXPos(long timestamp, TimeRange fullTimeGraphRange, double nanosPerPixel) {
        long start = fullTimeGraphRange.getStart();
        long end = fullTimeGraphRange.getEnd();

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
        long fullTimeGraphStartTime = getViewContext().getCurrentTraceFullRange().getStart();
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
        double vmin = fTreeScrollPane.getVmin();
        double vmax = fTreeScrollPane.getVmax();
        double contentHeight = fTreePane.getLayoutBounds().getHeight();
        double viewportHeight = fTreeScrollPane.getViewportBounds().getHeight();

        double vtop = Math.max(0, contentHeight - viewportHeight) * (vvalue - vmin) / (vmax - vmin);
        double vbottom = vtop + viewportHeight;

        return new VerticalPosition(vtop, vbottom);
    }

    private static int paneYPosToEntryListIndex(double yPos, double entryHeight) {
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
        return fDebugOptions;
    }

    @VisibleForTesting
    double getCurrentNanosPerPixel() {
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
