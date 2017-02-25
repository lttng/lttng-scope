package org.lttng.scope.tmf2.views.ui.timegraph.swtjfx;

import java.util.List;
import java.util.stream.Collectors;

import org.lttng.scope.tmf2.views.core.timegraph.model.provider.ITimeGraphModelRenderProvider;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.states.TimeGraphStateRender;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.tree.TimeGraphTreeElement;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.tree.TimeGraphTreeRender;

class TimeGraphAreaRender {

    private final long fStartTime;
    private final long fEndTime;

    private final int fFirstEntryIndex;

    private final List<TimeGraphTreeElement> fTreeElements;
    private final List<TimeGraphStateRender> fStateRenders;

    private TimeGraphAreaRender(long start, long end, int firstEntry,
            List<TimeGraphTreeElement> treeElements, List<TimeGraphStateRender> stateRenders) {
        fStartTime = start;
        fEndTime = end;
        fFirstEntryIndex = firstEntry;
        fTreeElements = treeElements;
        fStateRenders = stateRenders;
    }

    public static TimeGraphAreaRender getFromProvider(ITimeGraphModelRenderProvider provider, TimeGraphTreeRender treeRender,
            long start, long end, long resolution, int firstEntry, int lastEntry) {

        List<TimeGraphTreeElement> treeElements = treeRender.getAllTreeElements().subList(firstEntry, lastEntry);

        List<TimeGraphStateRender> stateRenders = treeElements.stream()
                .map(treeElem -> provider.getStateRender(treeElem, start, end, resolution))
                .collect(Collectors.toList());

        int nbEntries = lastEntry - firstEntry;
        if (treeElements.size() != nbEntries || stateRenders.size() != nbEntries) {
            throw new IllegalStateException();
        }

        return new TimeGraphAreaRender(start, end, firstEntry, treeElements, stateRenders);
    }

    public long getStartTime() {
        return fStartTime;
    }

    public long getEndTime() {
        return fEndTime;
    }

    public int getFirstEntryIndex() {
        return fFirstEntryIndex;
    }

    public List<TimeGraphTreeElement> getTreeElements() {
        return fTreeElements;
    }

    public List<TimeGraphStateRender> getStateRenders() {
        return fStateRenders;
    }

}
