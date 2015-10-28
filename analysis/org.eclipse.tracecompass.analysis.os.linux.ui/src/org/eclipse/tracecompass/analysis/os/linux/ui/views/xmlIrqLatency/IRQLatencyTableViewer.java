package org.eclipse.tracecompass.analysis.os.linux.ui.views.xmlIrqLatency;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.tracecompass.analysis.os.linux.core.latency.irq.IRQLatencyAnalysis1;
import org.eclipse.tracecompass.analysis.timing.core.segmentstore.AbstractSegmentStoreAnalysisModule;
import org.eclipse.tracecompass.analysis.timing.ui.views.segmentstore.AbstractSegmentStoreTableViewer;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;

public class IRQLatencyTableViewer extends AbstractSegmentStoreTableViewer {

    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------

    /**
     * Constructor
     *
     * @param tableViewer
     *            The table viewer
     */
    public IRQLatencyTableViewer(TableViewer tableViewer) {
        super(tableViewer);
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    @Override
    protected @Nullable AbstractSegmentStoreAnalysisModule getSegmentStoreAnalysisModule(ITmfTrace trace) {
          return TmfTraceUtils.getAnalysisModuleOfClass(trace, IRQLatencyAnalysis1.class, IRQLatencyAnalysis1.ID);
    }
}
