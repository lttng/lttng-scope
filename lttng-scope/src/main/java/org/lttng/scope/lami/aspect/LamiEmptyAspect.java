/*******************************************************************************
 * Copyright (c) 2015, 2016 EfficiOS Inc., Alexandre Montplaisir
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.lttng.scope.lami.aspect;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lttng.scope.lami.module.LamiTableEntry;

import java.util.Comparator;

/**
 * Null aspect for LAMI tables, normally printing nothing in the corresponding
 * cells. Should be access using {@link #INSTANCE}.
 *
 * @author Alexandre Montplaisir
 */
public class LamiEmptyAspect extends LamiTableEntryAspect {

    /** Singleton instance */
    public static final LamiEmptyAspect INSTANCE = new LamiEmptyAspect();

    private LamiEmptyAspect() {
        super("", null); //$NON-NLS-1$
    }

    @Override
    public boolean isContinuous() {
        return false;
    }

    @Override
    public @Nullable String resolveString(LamiTableEntry entry) {
        return null;
    }

    @Override
    public boolean isTimeStamp() {
        return false;
    }

    @Override
    public @Nullable Number resolveNumber(@NotNull LamiTableEntry entry) {
        return null;
    }

    @Override
    public Comparator<LamiTableEntry> getComparator() {
        return (o1, o2) -> 0;
    }

}
