/*******************************************************************************
 * Copyright (c) 2016 EfficiOS Inc., Alexandre Montplaisir
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.lttng.scope.tmf2.views.ui.timeline.widgets.timegraph;

import java.lang.ref.WeakReference;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.eclipse.jdt.annotation.Nullable;

import javafx.concurrent.Task;

public class LatestTaskExecutor {

    private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(2);
//    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();

    /**
     * The latest job that was schedule in this queue.
     */
    private WeakReference<@Nullable Task<?>> fLatestTask = new WeakReference<>(null);

    public LatestTaskExecutor() {
    }

    public synchronized void schedule(Task<?> newTask) {
        Task<?> latestJob = fLatestTask.get();
        if (latestJob != null) {
            /*
             * Cancel the existing task. Here's hoping it cooperates and ends
             * quickly!
             */
            latestJob.cancel(false);
        }

        /* Start the new job */
        fLatestTask = new WeakReference<>(newTask);
        EXECUTOR.submit(newTask);
    }

}
