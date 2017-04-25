/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.lttng.scope.tmf2.views.core.timegraph.model.render.states;

import java.util.Map;

import org.eclipse.jdt.annotation.Nullable;
import org.lttng.scope.tmf2.views.core.config.ConfigOption;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.ColorDefinition;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.LineThickness;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.TimeGraphEvent;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.tree.TimeGraphTreeElement;

public interface TimeGraphStateInterval {

    TimeGraphEvent getStartEvent();

    TimeGraphEvent getEndEvent();

    String getStateName();

    @Nullable String getLabel();

    ConfigOption<ColorDefinition> getColorDefinition();

    ConfigOption<LineThickness> getLineThickness();

    boolean isMultiState();

    Map<String, String> getProperties();

    default long getStartTime() {
        return getStartEvent().getTimestamp();
    }

    default long getEndTime() {
        return getEndEvent().getTimestamp();
    }

    default long getDuration() {
        return (getEndTime() - getStartTime());
    }

    default TimeGraphTreeElement getTreeElement() {
        return getStartEvent().getTreeElement();
    }

}
