package org.eclipse.tracecompass.analysis.os.linux.ui.views.irqscatter;

import static org.eclipse.tracecompass.common.core.NonNullUtils.nullToEmptyString;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tracecompass.common.core.NonNullUtils;
import org.eclipse.tracecompass.internal.analysis.os.linux.ui.views.latency.Messages;
import org.eclipse.tracecompass.tmf.ui.viewers.xycharts.TmfXYChartViewer;
import org.eclipse.tracecompass.tmf.ui.views.TmfChartView;

public class IRQLatencyScatterGraphView extends TmfChartView {
    // Attributes
    // ------------------------------------------------------------------------

    /** The view's ID */
    public static final String ID = "org.eclipse.tracecompass.analysis.os.linux.ui.irqLatencyScatterChart"; //$NON-NLS-1$

    private @Nullable IRQLatencyScatterGraphViewer fScatterViewer;

    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------

    /**
     * Constructor
     */
    public IRQLatencyScatterGraphView() {
        super(ID);
    }

    // ------------------------------------------------------------------------
    // ViewPart
    // ------------------------------------------------------------------------

    @Override
    protected TmfXYChartViewer createChartViewer(@Nullable Composite parent) {
        fScatterViewer = new IRQLatencyScatterGraphViewer(NonNullUtils.checkNotNull(parent), nullToEmptyString(Messages.SystemCallLatencyScatterView_title), nullToEmptyString(Messages.SystemCallLatencyScatterView_xAxis),
                nullToEmptyString(Messages.SystemCallLatencyScatterView_yAxis));
        return fScatterViewer;
    }

}
