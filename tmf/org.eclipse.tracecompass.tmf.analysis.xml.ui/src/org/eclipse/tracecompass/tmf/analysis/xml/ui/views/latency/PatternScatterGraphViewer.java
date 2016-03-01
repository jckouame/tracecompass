package org.eclipse.tracecompass.tmf.analysis.xml.ui.views.latency;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tracecompass.analysis.timing.core.segmentstore.ISegmentStoreProvider;
import org.eclipse.tracecompass.analysis.timing.ui.views.segmentstore.scatter.AbstractSegmentStoreScatterGraphViewer;
import org.eclipse.tracecompass.tmf.analysis.xml.core.stateprovider.XmlPatternAnalysis;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;

/**
 * @author Jean-Christian Kouame
 *
 */
public class PatternScatterGraphViewer extends AbstractSegmentStoreScatterGraphViewer {

    private String fAnalysisId;

    /**
     * @param parent
     * @param title
     * @param xLabel
     * @param yLabel
     */
    public PatternScatterGraphViewer(@NonNull Composite parent, @NonNull String title, @NonNull String xLabel, @NonNull String yLabel) {
        super(parent, title, xLabel, yLabel);
    }

    @Override
    protected @Nullable ISegmentStoreProvider getSegmentStoreProvider(@NonNull ITmfTrace trace) {
        if (fAnalysisId == null) {
            return null;
        }
        return TmfTraceUtils.getAnalysisModuleOfClass(trace, XmlPatternAnalysis.class, fAnalysisId);
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
        setData(TmfTraceUtils.getAnalysisModuleOfClass(TmfTraceManager.getInstance().getActiveTrace(), XmlPatternAnalysis.class, fAnalysisId));
    }

}
