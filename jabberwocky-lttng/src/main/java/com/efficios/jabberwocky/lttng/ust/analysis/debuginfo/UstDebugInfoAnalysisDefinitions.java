/*
 * Copyright (C) 2017 EfficiOS Inc., Alexandre Montplaisir <alexmonthy@efficios.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.efficios.jabberwocky.lttng.ust.analysis.debuginfo;

import java.util.HashMap;
import java.util.Map;

import com.efficios.jabberwocky.lttng.ust.trace.layout.LttngUst28EventLayout;
import com.google.common.collect.ImmutableMap;
import org.jetbrains.annotations.Nullable;

class UstDebugInfoAnalysisDefinitions {

    static final int DL_DLOPEN_INDEX = 1;
    static final int DL_BUILD_ID_INDEX = 2;
    static final int DL_DEBUG_LINK_INDEX = 3;
    static final int DL_DLCLOSE_INDEX = 4;
    static final int STATEDUMP_BIN_INFO_INDEX = 5;
    static final int STATEDUMP_BUILD_ID_INDEX = 6;
    static final int STATEDUMP_DEBUG_LINK_INDEX = 7;
    static final int STATEDUMP_START_INDEX = 8;

    private static final Map<LttngUst28EventLayout, UstDebugInfoAnalysisDefinitions> DEFINITIONS_MAP= new HashMap<>();

    public static synchronized UstDebugInfoAnalysisDefinitions getDefsFromLayout(LttngUst28EventLayout layout) {
        UstDebugInfoAnalysisDefinitions defs = DEFINITIONS_MAP.get(layout);
        if (defs == null) {
            defs = new UstDebugInfoAnalysisDefinitions(layout);
            DEFINITIONS_MAP.put(layout, defs);
        }
        return defs;
    }

    private final LttngUst28EventLayout layout;
    private final Map<String, Integer> eventNames;

    private UstDebugInfoAnalysisDefinitions(LttngUst28EventLayout layout) {
        this.layout = layout;
        this.eventNames = buildEventNames(layout);
    }

    private static Map<String, Integer> buildEventNames(LttngUst28EventLayout layout) {
        ImmutableMap.Builder<String, Integer> builder = ImmutableMap.builder();
        builder.put(layout.eventDlOpen(), DL_DLOPEN_INDEX);
        builder.put(layout.eventDlBuildId(), DL_BUILD_ID_INDEX);
        builder.put(layout.eventDlDebugLink(), DL_DEBUG_LINK_INDEX);
        builder.put(layout.eventDlClose(), DL_DLCLOSE_INDEX);
        builder.put(layout.eventStatedumpBinInfo(), STATEDUMP_BIN_INFO_INDEX);
        builder.put(layout.eventStateDumpBuildId(), STATEDUMP_BUILD_ID_INDEX);
        builder.put(layout.eventStateDumpDebugLink(), STATEDUMP_DEBUG_LINK_INDEX);
        builder.put(layout.eventStatedumpStart(), STATEDUMP_START_INDEX);
        return builder.build();
    }

    public LttngUst28EventLayout getLayout() {
        return layout;
    }

    public @Nullable Integer getIndexOfName(String eventName) {
        return eventNames.get(eventName);
    }
}
