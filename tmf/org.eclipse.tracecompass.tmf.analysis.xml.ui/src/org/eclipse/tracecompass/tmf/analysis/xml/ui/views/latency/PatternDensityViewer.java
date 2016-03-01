package org.eclipse.tracecompass.tmf.analysis.xml.ui.views.latency;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tracecompass.analysis.timing.core.segmentstore.ISegmentStoreProvider;
import org.eclipse.tracecompass.analysis.timing.ui.views.segmentstore.density.AbstractSegmentStoreDensityViewer;
import org.eclipse.tracecompass.tmf.analysis.xml.core.stateprovider.XmlPatternAnalysis;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;

public class PatternDensityViewer extends AbstractSegmentStoreDensityViewer {

    private String fAnalysisId;
    public PatternDensityViewer(@NonNull Composite parent) {
        super(parent);
    }

    /**
     * Set the analysis ID
     *
     * @param analysisId
     *            The analysis ID
     */
    public void updateViewer(String analysisId) {
        if (analysisId == null) {
            return;
        }
        fAnalysisId = analysisId;
        loadTrace(TmfTraceManager.getInstance().getActiveTrace());
//        setData(TmfTraceUtils.getAnalysisModuleOfClass(TmfTraceManager.getInstance().getActiveTrace(), XmlPatternAnalysis.class, fAnalysisId));
    }

    @Override
    protected @Nullable ISegmentStoreProvider getSegmentStoreProvider(@NonNull ITmfTrace trace) {
        if (fAnalysisId == null) {
            return null;
        }
        return TmfTraceUtils.getAnalysisModuleOfClass(trace, XmlPatternAnalysis.class, fAnalysisId);}
}
