package org.eclipse.tracecompass.analysis.os.linux.core.model;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.segmentstore.core.ISegment;
import org.eclipse.tracecompass.segmentstore.core.ISegmentStore;

/**
 * @author Jean-Christian Kouame
 * @since 2.0
 *
 */
public interface ILatencyAnalysis {
    /**
     * Get the results of the analysis
     * @return The result of the analysis
     */
    @Nullable ISegmentStore<ISegment> getResults();

    /**
     * Listener for the viewers
     *
     * @param listener
     *            listener for each type of viewer
     */
    void addListener(LatencyAnalysisListener listener);
}
