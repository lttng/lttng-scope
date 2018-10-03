/*
 * Copyright (C) 2018 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.efficios.jabberwocky.views.xychart.control

import com.efficios.jabberwocky.common.TimeRange
import com.efficios.jabberwocky.context.ViewGroupContext
import com.efficios.jabberwocky.project.TraceProject
import com.efficios.jabberwocky.trace.Trace
import com.efficios.jabberwocky.trace.TraceStubs
import com.efficios.jabberwocky.trace.event.TraceEvent
import com.efficios.jabberwocky.views.xychart.model.provider.XYChartModelProvider
import com.efficios.jabberwocky.views.xychart.view.XYChartView
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.nio.file.Path

class XYChartControlTest {

    private val viewContext = ViewGroupContext()
    private val control = XYChartControl(viewContext, TestModelProvider())

    private lateinit var project1Path: Path
    private lateinit var project1: TraceProject<TraceEvent, Trace<TraceEvent>>
    private lateinit var project2Path: Path
    private lateinit var project2: TraceProject<TraceEvent, Trace<TraceEvent>>

    @BeforeEach
    fun setup() {
        control.view = TestView(control)

        val project1Name = "xy-chart-control-test1"
        project1Path = Files.createTempDirectory(project1Name)
        project1 = TraceProject.ofSingleTrace(project1Name, project1Path, TraceStubs.TraceStub1())

        val project2Name = "xy-chart-control-test2"
        project2Path = Files.createTempDirectory(project2Name)
        project2 = TraceProject.ofSingleTrace(project2Name, project2Path, TraceStubs.TraceStub2())

        viewContext.switchProject(null)
    }

    @AfterEach
    fun cleanup() {
        project1Path.toFile().deleteRecursively()
        project2Path.toFile().deleteRecursively()
    }

    @Test
    fun testInitialState() {
        with(control.view as TestView) {
            assertFalse(disposeCalled)
            assertEquals(ViewGroupContext.UNINITIALIZED_RANGE, lastVisibleRange)
            assertEquals(ViewGroupContext.UNINITIALIZED_RANGE, lastSelectionRange)
        }
    }

    @Test
    fun testSetProject() {
        viewContext.switchProject(project1)

        with(control.view as TestView) {
            assertFalse(disposeCalled)
            assertEquals(TimeRange.of(2, 10), lastVisibleRange)
            assertEquals(TimeRange.of(2, 2), lastSelectionRange)
        }
    }

    @Test
    fun testChangeVisibleTimeRange() {
        viewContext.switchProject(project1)
        viewContext.visibleTimeRange = TimeRange.of(3, 8)

        with(control.view as TestView) {
            assertFalse(disposeCalled)
            assertEquals(TimeRange.of(3, 8), lastVisibleRange)
            assertEquals(TimeRange.of(2, 2), lastSelectionRange)
        }
    }

    @Test
    fun testChangeSelectionTimeRange() {
        viewContext.switchProject(project1)
        viewContext.selectionTimeRange = TimeRange.of(5, 6)

        with(control.view as TestView) {
            assertFalse(disposeCalled)
            assertEquals(TimeRange.of(2, 10), lastVisibleRange)
            assertEquals(TimeRange.of(5, 6), lastSelectionRange)
        }
    }

    @Test
    fun testUnsetProject() {
        viewContext.switchProject(project1)
        viewContext.switchProject(null)

        with(control.view as TestView) {
            assertFalse(disposeCalled)
            assertEquals(ViewGroupContext.UNINITIALIZED_RANGE, lastVisibleRange)
            assertEquals(ViewGroupContext.UNINITIALIZED_RANGE, lastSelectionRange)
        }
    }

    @Test
    fun testSwitchProject() {
        viewContext.switchProject(project1)
        viewContext.switchProject(project2)

        with(control.view as TestView) {
            assertFalse(disposeCalled)
            assertEquals(TimeRange.of(4, 8), lastVisibleRange)
            assertEquals(TimeRange.of(4, 4), lastSelectionRange)
        }
    }

    @Test
    fun testDispose() {
        viewContext.switchProject(project1)
        control.dispose()

        with(control.view as TestView) {
            assertTrue(disposeCalled)
        }
    }

}

/** Dummy model provider */
private class TestModelProvider : XYChartModelProvider("test-provider")

/**
 * "View" implementation which will simply track which of its method(s) were called.
 */
private class TestView(override val control: XYChartControl) : XYChartView {

    var disposeCalled = false
        private set
    var lastVisibleRange: TimeRange = ViewGroupContext.UNINITIALIZED_RANGE
        private set
    var lastSelectionRange: TimeRange = ViewGroupContext.UNINITIALIZED_RANGE
        private set

    override fun dispose() {
        disposeCalled = true
    }

    override fun clear() {
    }

    override fun seekVisibleRange(newVisibleRange: TimeRange) {
        lastVisibleRange = newVisibleRange
    }

    override fun drawSelection(selectionRange: TimeRange) {
        lastSelectionRange = selectionRange
    }

}
