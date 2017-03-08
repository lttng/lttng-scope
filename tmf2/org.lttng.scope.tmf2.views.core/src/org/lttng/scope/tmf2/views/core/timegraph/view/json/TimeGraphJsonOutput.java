package org.lttng.scope.tmf2.views.core.timegraph.view.json;

import java.util.List;

import org.lttng.scope.tmf2.views.core.timegraph.control.TimeGraphModelControl;
import org.lttng.scope.tmf2.views.core.timegraph.model.provider.ITimeGraphModelRenderProvider;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.states.TimeGraphStateRender;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.tree.TimeGraphTreeRender;
import org.lttng.scope.tmf2.views.core.timegraph.view.TimeGraphModelView;

public class TimeGraphJsonOutput extends TimeGraphModelView {

    public TimeGraphJsonOutput(TimeGraphModelControl control) {
        super(control);
    }

    @Override
    public void disposeImpl() {
    }

    @Override
    public void clear() {
        // TODO
    }

    @Override
    public void seekVisibleRange(long visibleWindowStartTime, long visibleWindowEndTime) {
        /* Generate JSON for the visible area */
        ITimeGraphModelRenderProvider provider = getControl().getModelRenderProvider();

        TimeGraphTreeRender treeRender = provider.getTreeRender();
        List<TimeGraphStateRender> stateRenders = provider.getStateRenders(treeRender, visibleWindowStartTime, visibleWindowEndTime, 1);

        RenderToJson.printRenderTo(stateRenders);
    }

    @Override
    public void drawSelection(long selectionStartTime, long selectionEndTime) {
        // TODO NYI
    }

}
