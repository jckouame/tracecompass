package org.eclipse.tracecompass.tmf.ui.views.xmlSegment;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.tracecompass.analysis.timing.core.segmentstore.AbstractSegmentStoreAnalysisModule;
import org.eclipse.tracecompass.analysis.timing.ui.views.segmentstore.AbstractSegmentStoreTableViewer;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;

public class XMLSegmentTableViewer extends AbstractSegmentStoreTableViewer {

    public XMLSegmentTableViewer(@NonNull TableViewer tableViewer) {
        super(tableViewer);
    }

    @Override
    protected @Nullable AbstractSegmentStoreAnalysisModule getSegmentStoreAnalysisModule(@NonNull ITmfTrace trace) {
        return TmfTraceUtils.getAnalysisModuleOfClass(trace, XMLSegmentStoreAnalysis.class, XMLSegmentStoreAnalysis.ID);
    }

}
