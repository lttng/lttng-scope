/*******************************************************************************
 * Copyright (c) 2016 EfficiOS Inc., Alexandre Montplaisir
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.lttng.scope.tmf2.views.ui.timegraph.swtjfx;

import java.lang.ref.WeakReference;

import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.annotation.Nullable;

public class LatestJobExecutor {

    /**
     * The latest job that was schedule in this queue.
     */
    private WeakReference<@Nullable Job> fLatestJob = new WeakReference<>(null);

    public LatestJobExecutor() {
    }

    public synchronized void schedule(Job newJob) {
        Job latestJob = fLatestJob.get();
        if (latestJob != null) {
            /*
             * Cancel the existing job. Here's hoping it cooperates and ends
             * quickly!
             */
            latestJob.cancel();
        }

        /* Start the new job */
        fLatestJob = new WeakReference<>(newJob);
        newJob.schedule();
    }

}
