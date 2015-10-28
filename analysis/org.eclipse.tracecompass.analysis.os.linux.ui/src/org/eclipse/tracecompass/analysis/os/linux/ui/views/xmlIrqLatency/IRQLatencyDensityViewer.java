package org.eclipse.tracecompass.analysis.os.linux.ui.views.xmlIrqLatency;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tracecompass.analysis.os.linux.core.latency.irq.IRQLatencyAnalysis1;
import org.eclipse.tracecompass.analysis.timing.core.segmentstore.AbstractSegmentStoreAnalysisModule;
import org.eclipse.tracecompass.internal.analysis.os.linux.ui.views.latency.AbstractDensityViewer;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;

public class IRQLatencyDensityViewer extends AbstractDensityViewer{

    /**
     * Constructs a new density viewer.
     *
     * @param parent
     *            the parent of the viewer
     */
    public IRQLatencyDensityViewer(Composite parent) {
        super(parent);
    }

    @Override
    protected @Nullable AbstractSegmentStoreAnalysisModule getSegmentStoreAnalysisModule(ITmfTrace trace) {
        return TmfTraceUtils.getAnalysisModuleOfClass(trace, IRQLatencyAnalysis1.class, IRQLatencyAnalysis1.ID);
    }
}
