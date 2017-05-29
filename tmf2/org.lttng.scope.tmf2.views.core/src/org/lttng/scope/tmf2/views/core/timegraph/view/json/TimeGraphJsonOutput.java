package org.lttng.scope.tmf2.views.core.timegraph.view.json;

import java.util.List;

import org.lttng.scope.tmf2.views.core.timegraph.control.TimeGraphModelControl;
import org.lttng.scope.tmf2.views.core.timegraph.model.provider.ITimeGraphModelProvider;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.states.TimeGraphStateRender;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.tree.TimeGraphTreeRender;
import org.lttng.scope.tmf2.views.core.timegraph.view.TimeGraphModelView;

import com.efficios.jabberwocky.common.TimeRange;

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
    public void seekVisibleRange(TimeRange newVisibleRange) {
        /* Generate JSON for the visible area */
        ITimeGraphModelProvider provider = getControl().getModelRenderProvider();

        TimeGraphTreeRender treeRender = provider.getTreeRender();
        List<TimeGraphStateRender> stateRenders = provider.getStateProvider().getStateRenders(treeRender,
                newVisibleRange, 1, null);

        RenderToJson.printRenderTo(stateRenders);
    }

    @Override
    public void drawSelection(TimeRange selectionRange) {
        // TODO NYI
    }

}
