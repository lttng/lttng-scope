/*******************************************************************************
 * Copyright (c) 2016 EfficiOS Inc., Alexandre Montplaisir
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.lttng.scope.tmf2.views.ui.timegraph.swtjfx;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.StringJoiner;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.lttng.scope.tmf2.views.core.timegraph.control.TimeGraphModelControl;
import org.lttng.scope.tmf2.views.core.timegraph.model.provider.ITimeGraphModelRenderProvider;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.states.TimeGraphStateRender;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.tree.TimeGraphTreeElement;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.tree.TimeGraphTreeRender;
import org.lttng.scope.tmf2.views.core.timegraph.view.TimeGraphModelView;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.concurrent.Task;
import javafx.embed.swt.FXCanvas;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeLineCap;

/**
 * Viewer for the {@link SwtJfxTimeGraphView}, encapsulating all the view's
 * controls.
 *
 * Its contents consist of:
 *
 * TODO update this to its final form
 * <pre>
 * SashForm fBaseControl (parent is passed from the view)
 *  + FXCanvas
 *  |   + ScrollPane
 *  |       + TreeView (?), contains the list of threads
 *  + FXCanvas
 *      + ScrollPane, will contain the time graph area
 *          + Pane, gets resized to very large horizontal size to represent the whole trace range
 *             + Canvas, canvas children are tiled on the Pane to show the content of one Render each
 *             + Canvas
 *             +  ...
 * </pre>
 *
 * Both ScrolledPanes's vertical scrollbars are bound together, so that they
 * scroll together.
 *
 * @author Alexandre Montplaisir
 */
public class SwtJfxTimeGraphViewer extends TimeGraphModelView {

    // ------------------------------------------------------------------------
    // Helper classes
    // ------------------------------------------------------------------------

    private static class HorizontalPosition {
        public long fStartTime = 0L;
        public long fEndTime = 0L;
    }

    private static class VerticalPosition {
        public double fTopPos = 0.0;
        public double fBottomPos = 0.0;
        public double fContentHeight = 0.0;
    }

    // ------------------------------------------------------------------------
    // Class fields
    // ------------------------------------------------------------------------

    private static final double MAX_CANVAS_WIDTH = 2000.0;
    private static final double MAX_CANVAS_HEIGHT = 2000.0;

    // ------------------------------------------------------------------------
    // Style definitions
    // (Could eventually be moved to separate .css file?)
    // ------------------------------------------------------------------------

    private static final Color BACKGROUD_LINES_COLOR = requireNonNull(Color.LIGHTBLUE);
    private static final String BACKGROUND_STYLE = "-fx-background-color: rgba(255, 255, 255, 255);"; //$NON-NLS-1$

    private static final double SELECTION_STROKE_WIDTH = 1;
    private static final Color SELECTION_STROKE_COLOR = requireNonNull(Color.BLUE);
    private static final Color SELECTION_FILL_COLOR = requireNonNull(Color.LIGHTBLUE.deriveColor(0, 1.2, 1, 0.4));

    private static final int LABEL_SIDE_MARGIN = 10;

    /**
     * Height of individual entries (text + states), including padding.
     *
     * TODO Make this configurable (vertical zoom feature)
     */
    private static final double ENTRY_HEIGHT = 20;

    /** Number of tree elements to print above *and* below the visible range */
    private static final int ENTRY_PREFETCHING = 5;

    /** Time between UI updates, in milliseconds */
    private static final int UI_UPDATE_DELAY = 250;

    // ------------------------------------------------------------------------
    // Instance fields
    // ------------------------------------------------------------------------

    private final SelectionContext fSelectionCtx = new SelectionContext();
    private final ScrollingContext fScrollingCtx = new ScrollingContext();

    private final LatestTaskExecutor fTaskExecutor = new LatestTaskExecutor();

    private final SashForm fBaseControl;

    private final FXCanvas fTreeFXCanvas;
    private final FXCanvas fTimeGraphFXCanvas;

    private final Pane fTreePane;
    private final ScrollPane fTreeScrollPane;
    private final Pane fTimeGraphPane;
    private final ScrollPane fTimeGraphScrollPane;

    /*
     * Children of the time graph pane are split into groups, so we can easily
     * redraw or add only some of them.
     */
    private final Group fTimeGraphStatesLayer;
    private final Group fTimeGraphSelectionLayer;
    // TODO Layers for markers, arrows

    private final Rectangle fSelectionRect;
    private final Rectangle fOngoingSelectionRect;


    private final VerticalPosition fVerticalPosition = new VerticalPosition();

    private final AtomicLong fTaskSeq = new AtomicLong();
    private final Timer fUiUpdateTimer = new Timer();
    private final TimerTask fUiUpdateTimerTask = new TimerTask() {

        private final HorizontalPosition fPreviousHorizontalPos = new HorizontalPosition();
        private final VerticalPosition fPreviousVerticalPosition = new VerticalPosition();

        @Override
        public void run() {
            long start = getControl().getVisibleTimeRangeStart();
            long end = getControl().getVisibleTimeRangeEnd();
            double topPos = fVerticalPosition.fTopPos;
            double bottomPos = fVerticalPosition.fBottomPos;
            double contentHeight = fVerticalPosition.fContentHeight;

            if (start == fPreviousHorizontalPos.fStartTime
                    && end == fPreviousHorizontalPos.fEndTime
                    && topPos == fPreviousVerticalPosition.fTopPos
                    && bottomPos == fPreviousVerticalPosition.fBottomPos
                    && contentHeight == fPreviousVerticalPosition.fContentHeight) {
                /*
                 * Exact same position as the last one we've seen, no need to
                 * repaint.
                 */
                return;
            }
            fPreviousHorizontalPos.fStartTime = start;
            fPreviousHorizontalPos.fEndTime = end;
            fPreviousVerticalPosition.fTopPos = topPos;
            fPreviousVerticalPosition.fBottomPos = bottomPos;
            fPreviousVerticalPosition.fContentHeight = contentHeight;

            paintArea(start, end, fTaskSeq.getAndIncrement());
        }
    };

    private volatile TimeGraphTreeRender fLatestTreeRender = TimeGraphTreeRender.EMPTY_RENDER;

    /** Current zoom level */
    private double fNanosPerPixel = 1.0;

    /**
     * Constructor
     *
     * @param parent
     *            Parent SWT composite
     */
    public SwtJfxTimeGraphViewer(Composite parent, TimeGraphModelControl control) {
        super(control);

        // TODO Convert this sash to JavaFX too?
        fBaseControl = new SashForm(parent, SWT.NONE);

        fTreeFXCanvas = new FXCanvas(fBaseControl, SWT.NONE);
        fTimeGraphFXCanvas = new FXCanvas(fBaseControl, SWT.NONE);

        // TODO Base on time-alignment
        fBaseControl.setWeights(new int[] { 15, 85 });

        // --------------------------------------------------------------------
        // Prepare the tree part's scene graph
        // --------------------------------------------------------------------

        fTreePane = new Pane();

        fTreeScrollPane = new ScrollPane(fTreePane);
        /* We only show the time graph's vertical scrollbar */
        fTreeScrollPane.setVbarPolicy(ScrollBarPolicy.NEVER);
        fTreeScrollPane.setHbarPolicy(ScrollBarPolicy.ALWAYS);

        // --------------------------------------------------------------------
        // Prepare the time graph's part scene graph
        // --------------------------------------------------------------------

        fSelectionRect = new Rectangle();
        fOngoingSelectionRect = new Rectangle();

        Stream.of(fSelectionRect, fOngoingSelectionRect).forEach(rect -> {
            rect.setStroke(SELECTION_STROKE_COLOR);
            rect.setStrokeWidth(SELECTION_STROKE_WIDTH);
            rect.setStrokeLineCap(StrokeLineCap.ROUND);
            rect.setFill(SELECTION_FILL_COLOR);
        });

        fTimeGraphStatesLayer = new Group();
        fTimeGraphSelectionLayer = new Group(fSelectionRect, fOngoingSelectionRect);

        fTimeGraphPane = new Pane(fTimeGraphStatesLayer, fTimeGraphSelectionLayer);
        fTimeGraphPane.setStyle(BACKGROUND_STYLE);
        fTimeGraphPane.addEventHandler(MouseEvent.MOUSE_PRESSED, fSelectionCtx.fMousePressedEventHandler);
        fTimeGraphPane.addEventHandler(MouseEvent.MOUSE_DRAGGED, fSelectionCtx.fMouseDraggedEventHandler);
        fTimeGraphPane.addEventHandler(MouseEvent.MOUSE_RELEASED, fSelectionCtx.fMouseReleasedEventHandler);

        /*
         * We control the width of the time graph pane programatically, so
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

        fTimeGraphScrollPane = new ScrollPane(fTimeGraphPane);
        fTimeGraphScrollPane.setVbarPolicy(ScrollBarPolicy.ALWAYS);
        fTimeGraphScrollPane.setHbarPolicy(ScrollBarPolicy.ALWAYS);

//        fTimeGraphScrollPane.viewportBoundsProperty().addListener(fScrollingCtx.fHScrollChangeListener);
        fTimeGraphScrollPane.setOnMouseEntered(fScrollingCtx.fMouseEnteredEventHandler);
        fTimeGraphScrollPane.setOnMouseExited(fScrollingCtx.fMouseExitedEventHandler);
        fTimeGraphScrollPane.hvalueProperty().addListener(fScrollingCtx.fHScrollChangeListener);
        fTimeGraphScrollPane.vvalueProperty().addListener(fScrollingCtx.fVScrollChangeListener);

        /* Synchronize the two scrollpanes' vertical scroll bars together */
        fTreeScrollPane.vvalueProperty().bindBidirectional(fTimeGraphScrollPane.vvalueProperty());

        // --------------------------------------------------------------------
        // Hook the parts into the SWT window
        // --------------------------------------------------------------------

        fTreeFXCanvas.setScene(new Scene(fTreeScrollPane));
        fTimeGraphFXCanvas.setScene(new Scene(fTimeGraphScrollPane));

        /*
         * Initially populate the viewer with the context of the current trace.
         */
        ITmfTrace trace = TmfTraceManager.getInstance().getActiveTrace();
        getControl().initializeForTrace(trace);

        fUiUpdateTimer.schedule(fUiUpdateTimerTask, UI_UPDATE_DELAY, UI_UPDATE_DELAY);
    }

    // ------------------------------------------------------------------------
    // Test accessors
    // ------------------------------------------------------------------------

    @VisibleForTesting
    protected Pane getTimeGraphPane() {
        return fTimeGraphPane;
    }

    @VisibleForTesting
    protected ScrollPane getTimeGraphScrollPane() {
        return fTimeGraphScrollPane;
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    @Override
    public void disposeImpl() {
        fUiUpdateTimer.cancel();
        fUiUpdateTimer.purge();
    }

    @Override
    public void clear() {
        // TODO
    }

    @Override
    public void seekVisibleRange(long visibleWindowStartTime, long visibleWindowEndTime) {
        final long fullTimeGraphStart = getControl().getFullTimeGraphStartTime();
        final long fullTimeGraphEnd = getControl().getFullTimeGraphEndTime();

        /* Update the zoom level */
        long windowTimeRange = visibleWindowEndTime - visibleWindowStartTime;
        double timeGraphWidth = fTimeGraphScrollPane.getWidth();
        fNanosPerPixel = windowTimeRange / timeGraphWidth;

        double timeGraphAreaWidth = timestampToPaneXPos(fullTimeGraphEnd) - timestampToPaneXPos(fullTimeGraphStart);
        if (timeGraphAreaWidth < 1.0) {
            // FIXME
            return;
        }

        double newValue;
        if (visibleWindowStartTime == fullTimeGraphStart) {
            newValue = fTimeGraphScrollPane.getHmin();
        } else if (visibleWindowEndTime == fullTimeGraphEnd) {
            newValue = fTimeGraphScrollPane.getHmax();
        } else {
            // FIXME Not aligned perfectly yet, see how the scrolling
            // listener does it?
            long targetTs = (visibleWindowStartTime + visibleWindowEndTime) / 2;
            double xPos = timestampToPaneXPos(targetTs);
            newValue = xPos / timeGraphAreaWidth;
        }

        fTimeGraphPane.setPrefWidth(timeGraphAreaWidth);
        fTimeGraphScrollPane.setHvalue(newValue);
    }

    private void paintArea(long windowStartTime, long windowEndTime, long taskSeqNb) {
        final long fullTimeGraphStart = getControl().getFullTimeGraphStartTime();
        final long fullTimeGraphEnd = getControl().getFullTimeGraphEndTime();

        /*
         * Get the current target width of the viewer, so we know at which
         * resolution we must do state system queries.
         *
         * Yes! We can query the size of visible components outside of the UI
         * thread! Praise the JavaFX!
         */
        long treePaneWidth = Math.round(fTreeScrollPane.getWidth());

        long windowTimeRange = windowEndTime - windowStartTime;

        /*
         * Request the needed renders and prepare the corresponding
         * canvases. We target at most one "window width" before and
         * after the current window, clamped by the trace's start and
         * end.
         */
        final long renderingStartTime = Math.max(fullTimeGraphStart, windowStartTime - windowTimeRange);
        final long renderingEndTime = Math.min(fullTimeGraphEnd, windowEndTime + windowTimeRange);
        final long renderTimeRange = (long) (MAX_CANVAS_WIDTH * fNanosPerPixel);
        final long resolution = Math.max(1, Math.round(fNanosPerPixel));

        if (renderTimeRange < 1) {
            return;
        }

        final VerticalPosition vertical = fVerticalPosition;

        Task<@Nullable Void> task = new Task<@Nullable Void>() {
            @Override
            protected @Nullable Void call() {

                long start = System.nanoTime();
                System.err.println("Starting paint task #" + taskSeqNb);

                ITimeGraphModelRenderProvider renderProvider = getControl().getModelRenderProvider();
                TimeGraphTreeRender treeRender = renderProvider.getTreeRender();
                final List<TimeGraphTreeElement> allTreeElements = treeRender.getAllTreeElements();

                if (isCancelled()) {
                    System.err.println("task #" + taskSeqNb + " was cancelled before generating the states");
                    return null;
                }

                long afterTreeRender = System.nanoTime();

                final int nbElements = allTreeElements.size();

                int topEntry = Math.max(0,
                        paneYPosToEntryListIndex(vertical.fTopPos, vertical.fContentHeight, nbElements) - ENTRY_PREFETCHING);
                int bottomEntry = Math.min(nbElements,
                        paneYPosToEntryListIndex(vertical.fBottomPos, vertical.fContentHeight, nbElements) + ENTRY_PREFETCHING);

                System.out.println("topEntry=" + topEntry +", bottomEntry=" + bottomEntry);

                List<TimeGraphStateRender> stateRenders = allTreeElements.subList(topEntry, bottomEntry).stream()
                        .map(treeElem -> renderProvider.getStateRender(treeElem, renderingStartTime, renderingEndTime, resolution))
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
                    treeContents = prepareTreeContents(treeRender, treePaneWidth);
                }

                /* Prepare the time graph part */
                Node timeGraphContents = prepareTimeGraphContents(stateRenders, topEntry);

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
                // Display.getDefault().syncExec( () -> {
                Platform.runLater(() -> {
                    long startUI = System.nanoTime();
                    if (treeContents != null) {
                        fTreePane.getChildren().clear();
                        fTreePane.getChildren().add(treeContents);
                    }

                    long afterTreeUI = System.nanoTime();

                    fTimeGraphStatesLayer.getChildren().clear();
                    fTimeGraphStatesLayer.getChildren().add(timeGraphContents);

                    long endUI = System.nanoTime();
                    StringJoiner sjui = new StringJoiner(", ", "UI Update (#" + taskSeqNb +"): ", "")
                            .add("Drawing tree=" + String.format("%,d", afterTreeUI - startUI) + " ns")
                            .add("Drawing states= " + String.format("%,d", endUI - afterTreeUI) + " ns");
                    System.err.println(sjui.toString());
                });

                return null;
            }
        };

        System.err.println("Queueing task #" + taskSeqNb);
        fTaskExecutor.schedule(task);
    }

    @Override
    public void drawSelection(long selectionStartTime, long selectionEndTime) {
        double xStart = timestampToPaneXPos(selectionStartTime);
        double xEnd = timestampToPaneXPos(selectionEndTime);
        double xWidth = xEnd - xStart;

        fSelectionRect.setX(xStart);
        fSelectionRect.setY(0);
        fSelectionRect.setWidth(xWidth);
        fSelectionRect.setHeight(fTimeGraphPane.getHeight());

        fSelectionRect.setVisible(true);
    }

    // ------------------------------------------------------------------------
    // Methods related to the Tree area
    // ------------------------------------------------------------------------

    private static Node prepareTreeContents(TimeGraphTreeRender treeRender, double paneWidth) {
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

        /* Prepare the Canvases with the horizontal alignment lines */
        List<Canvas> canvases = new ArrayList<>();
        int maxEntriesPerCanvas = (int) (MAX_CANVAS_HEIGHT / ENTRY_HEIGHT);
        Lists.partition(treeElements, maxEntriesPerCanvas).forEach(subList -> {
            int nbElements = subList.size();
            double height = nbElements * ENTRY_HEIGHT;

            Canvas canvas = new Canvas(paneWidth, height);
            drawBackgroundLines(canvas, ENTRY_HEIGHT);
            canvas.setCache(true);
            canvases.add(canvas);
        });
        VBox canvasBox = new VBox();
        canvasBox.getChildren().addAll(canvases);

        /* Put the background Canvas and the Tree View into their containers */
        StackPane stackPane = new StackPane(canvasBox, treeElemsBox);
        stackPane.setStyle(BACKGROUND_STYLE);
        return stackPane;
    }

    // ------------------------------------------------------------------------
    // Methods related to the Time Graph area
    // ------------------------------------------------------------------------

    private Node prepareTimeGraphContents(List<TimeGraphStateRender> stateRenders, int topEntry) {
        Collection<Node> rectangles = IntStream.range(0, stateRenders.size()).parallel()
                .mapToObj(idx -> getRectanglesForStateRender(stateRenders.get(idx), idx + topEntry))
                .flatMap(Function.identity())
                .collect(Collectors.toSet());

        return new Group(rectangles);
    }

    /**
     * Get the vertically-tiled Canvas's for a single render. They will
     * be already relocated correctly, so the collection's order does not
     * matter.
     *
     * @param render
     *            The render
     * @return The vertical set of canvases
     */
    private Stream<Rectangle> getRectanglesForStateRender(TimeGraphStateRender stateRender, int entryIndex) {
        return stateRender.getStateIntervals().stream()
                .map(interval -> {
                    double xStart = timestampToPaneXPos(interval.getStartEvent().getTimestamp());
                    double xEnd = timestampToPaneXPos(interval.getEndEvent().getTimestamp());
                    double width = Math.max(1.0, xEnd - xStart) + 1.0;

                    double height;
                    switch (interval.getLineThickness()) {
                    case NORMAL:
                    default:
                        height = ENTRY_HEIGHT - 4;
                        break;
                    case SMALL:
                        height = ENTRY_HEIGHT - 8;
                        break;
                    }

                    // TODO Calculate value for small thickness too
                    double y = entryIndex * ENTRY_HEIGHT + 2;

                    Rectangle rect = new Rectangle(xStart, y, width, height);
                    rect.setFill(JfxColorFactory.getColorFromDef(interval.getColorDefinition()));
                    return rect;
                });
    }

    // ------------------------------------------------------------------------
    // Mouse event listeners
    // ------------------------------------------------------------------------

    /**
     * Class encapsulating the time range selection, related drawing and
     * listeners.
     */
    private class SelectionContext {

        private boolean fOngoingSelection;
        private double fMouseOriginX;

        public final EventHandler<MouseEvent> fMousePressedEventHandler = e -> {
            if (e.isShiftDown() ||
                    e.isControlDown() ||
                    e.isSecondaryButtonDown() ||
                    e.isMiddleButtonDown()) {
                /* Do other things! */
                // TODO!
                return;
            }

            if (fOngoingSelection) {
                return;
            }

            /* Remove the current selection, if there is one */
            fSelectionRect.setVisible(false);

            fMouseOriginX = e.getX();

            fOngoingSelectionRect.setX(fMouseOriginX);
            fOngoingSelectionRect.setY(0);
            fOngoingSelectionRect.setWidth(0);
            fOngoingSelectionRect.setHeight(fTimeGraphPane.getHeight());

            fOngoingSelectionRect.setVisible(true);

            e.consume();

            fOngoingSelection = true;
        };

        public final EventHandler<MouseEvent> fMouseDraggedEventHandler = e -> {
            double newX = e.getX();
            double offsetX = newX - fMouseOriginX;

            if (offsetX > 0) {
                fOngoingSelectionRect.setX(fMouseOriginX);
                fOngoingSelectionRect.setWidth(offsetX);
            } else {
                fOngoingSelectionRect.setX(newX);
                fOngoingSelectionRect.setWidth(-offsetX);
            }

            e.consume();
        };

        public final EventHandler<MouseEvent> fMouseReleasedEventHandler = e -> {
            fOngoingSelectionRect.setVisible(false);

            e.consume();

            /* Send a time range selection signal for the currently selected time range */
            double startX = Math.max(0, fOngoingSelectionRect.getX());
            // FIXME Possible glitch when selecting backwards outside of the window
            double endX = Math.min(fTimeGraphPane.getWidth(), startX + fOngoingSelectionRect.getWidth());
            long tsStart = paneXPosToTimestamp(startX);
            long tsEnd = paneXPosToTimestamp(endX);

            getControl().updateTimeRangeSelection(tsStart, tsEnd);

            fOngoingSelection = false;
        };
    }

    /**
     * Class encapsulating the scrolling operations of the time graph pane.
     *
     * The mouse entered/exited handlers ensure only the scrollpane being
     * interacted by the user is the one sending the synchronization signals.
     */
    private class ScrollingContext {

        private boolean fUserActionOngoing = false;

        private final EventHandler<MouseEvent> fMouseEnteredEventHandler = e -> {
            fUserActionOngoing = true;
        };

        private final EventHandler<MouseEvent> fMouseExitedEventHandler = e -> {
            fUserActionOngoing = false;
        };

        /**
         * Listener for the horizontal scrollbar changes
         */
        private final ChangeListener<Number> fHScrollChangeListener = (observable, oldValue, newValue) -> {
            if (!fUserActionOngoing) {
                System.out.println("HScroll listener triggered but inactive");
                return;
            }

            System.out.println("HScroll change listener triggered, oldval=" + oldValue.toString() + ", newval=" + newValue.toString());

            /*
             * Determine the X position represented by the left edge of the pane
             */
            double hmin = fTimeGraphScrollPane.getHmin();
            double hmax = fTimeGraphScrollPane.getHmax();
            /* scrollPane.getHvalue() would return the *old* value here! */
            double hvalue = newValue.doubleValue();
            double contentWidth = fTimeGraphPane.getLayoutBounds().getWidth();
            double viewportWidth = fTimeGraphScrollPane.getViewportBounds().getWidth();
            double hoffset = Math.max(0, contentWidth - viewportWidth) * (hvalue - hmin) / (hmax - hmin);

            /*
             * Convert the positions of the left and right edges to timestamps,
             * and send a window range update signal
             */
            long tsStart = paneXPosToTimestamp(hoffset);
            long tsEnd = paneXPosToTimestamp(hoffset + viewportWidth);

            System.out.printf("Offset: %.1f, width: %.1f %n", hoffset, viewportWidth);
            System.out.printf("Sending visible range update: %,d to %,d%n", tsStart, tsEnd);

            getControl().updateVisibleTimeRange(tsStart, tsEnd);

            /*
             * The control will not send thisn signal back to us (to avoid
             * jitter while scrolling), but the next UI update should refresh
             * the view accordingly.
             */
        };

        private final ChangeListener<Number> fVScrollChangeListener = (observable, oldValue, newValue) -> {
            if (!fUserActionOngoing) {
                System.out.println("HScroll listener triggered but inactive");
                return;
            }

            /* Get the Y position of the top/bottom edges of the pane */
            double vmin = fTreeScrollPane.getVmin();
            double vmax = fTreeScrollPane.getVmax();
            double vvalue = newValue.doubleValue();
            double contentHeight = fTreePane.getLayoutBounds().getHeight();
            double viewportHeight = fTreeScrollPane.getViewportBounds().getHeight();

            double vtop = Math.max(0, contentHeight - viewportHeight) * (vvalue - vmin) / (vmax - vmin);
            double vbottom = vtop + viewportHeight;

            fVerticalPosition.fTopPos = vtop;
            fVerticalPosition.fBottomPos = vbottom;
            fVerticalPosition.fContentHeight = contentHeight;

            /* Next UI update will take these coordinates in consideration */
        };
    }

    // ------------------------------------------------------------------------
    // Common utils
    // ------------------------------------------------------------------------

    private static void drawBackgroundLines(Canvas canvas, double entryHeight) {
        double width = canvas.getWidth();
        int nbLines = (int) (canvas.getHeight() / entryHeight);

        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.save();

        gc.setStroke(BACKGROUD_LINES_COLOR);
        gc.setLineWidth(1);
        /* average+2 gives the best-looking output */
        DoubleStream.iterate((ENTRY_HEIGHT / 2) + 2, i -> i + entryHeight)
                .limit(nbLines)
                .forEach(yPos -> {
                    gc.strokeLine(0, yPos, width, yPos);
                });

        gc.restore();
    }

    private double timestampToPaneXPos(long timestamp) {
        long fullTimeGraphStartTime = getControl().getFullTimeGraphStartTime();
        long fullTimeGraphEndTime = getControl().getFullTimeGraphEndTime();
        return timestampToPaneXPos(timestamp, fullTimeGraphStartTime, fullTimeGraphEndTime, fNanosPerPixel);
    }

    @VisibleForTesting
    public static double timestampToPaneXPos(long timestamp, long start, long end, double nanosPerPixel) {
        if (timestamp < start) {
            throw new IllegalArgumentException(timestamp + " is smaller than trace start time " + start); //$NON-NLS-1$
        }
        if (timestamp > end) {
            throw new IllegalArgumentException(timestamp + " is greater than trace end time " + end); //$NON-NLS-1$
        }

        double traceTimeRange = end - start;
        double timeStampRatio = (timestamp - start) / traceTimeRange;

        long fullTraceWidthInPixels = (long) (traceTimeRange / nanosPerPixel);
        double xPos = fullTraceWidthInPixels * timeStampRatio;
        return Math.round(xPos);
    }

    private long paneXPosToTimestamp(double x) {
        long fullTimeGraphStartTime = getControl().getFullTimeGraphStartTime();
        return paneXPosToTimestamp(x, fTimeGraphPane.getWidth(), fullTimeGraphStartTime, fNanosPerPixel);
    }

    @VisibleForTesting
    public static long paneXPosToTimestamp(double x, double totalWidth, long startTimestamp, double nanosPerPixel) {
        if (x < 0.0 || totalWidth < 1.0 || x > totalWidth) {
            throw new IllegalArgumentException("Invalid position arguments: pos=" + x + ", width=" + totalWidth);
        }

        long ts = Math.round(x * nanosPerPixel);
        return ts + startTimestamp;
    }

    private static int paneYPosToEntryListIndex(double yPos, double yMax, int nbEntries) {
        double ratio = yPos / yMax;
        return (int) (ratio * nbEntries);
    }
}
