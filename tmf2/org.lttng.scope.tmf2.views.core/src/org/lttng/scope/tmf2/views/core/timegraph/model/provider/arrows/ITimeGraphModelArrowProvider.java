package org.lttng.scope.tmf2.views.core.timegraph.model.provider.arrows;

import org.lttng.scope.tmf2.views.core.TimeRange;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.arrows.TimeGraphArrowRender;
import org.lttng.scope.tmf2.views.core.timegraph.model.render.arrows.TimeGraphArrowSeries;

public interface ITimeGraphModelArrowProvider {

    TimeGraphArrowSeries getArrowSeries();

    TimeGraphArrowRender getArrowRender(TimeRange timeRange);

}
