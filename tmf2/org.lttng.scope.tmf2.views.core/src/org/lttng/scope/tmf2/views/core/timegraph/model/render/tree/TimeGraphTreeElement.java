/*******************************************************************************
 * Copyright (c) 2016 EfficiOS Inc., Alexandre Montplaisir
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.lttng.scope.tmf2.views.core.timegraph.model.render.tree;

import java.util.List;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;

public class TimeGraphTreeElement {

    private final String fName;
    private final List<TimeGraphTreeElement> fChildElements;

    public TimeGraphTreeElement(String name, List<TimeGraphTreeElement> children) {
        fName = name;
        fChildElements = ImmutableList.copyOf(children);
    }

    public String getName() {
        return fName;
    }

    public List<TimeGraphTreeElement> getChildElements() {
        return fChildElements;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
            .add("fName", fName) //$NON-NLS-1$
            .add("fChildElements", fChildElements.toString()) //$NON-NLS-1$
            .toString();
    }

}
