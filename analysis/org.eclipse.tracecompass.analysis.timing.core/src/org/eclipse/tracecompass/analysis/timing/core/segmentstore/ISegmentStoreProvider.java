package org.eclipse.tracecompass.analysis.timing.core.segmentstore;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.segmentstore.core.ISegment;
import org.eclipse.tracecompass.segmentstore.core.ISegmentStore;
import org.eclipse.tracecompass.tmf.core.segment.ISegmentAspect;

/**
 * Segment store provider. Useful to populate views.
 *
 * @author Matthew Khouzam
 * @since 2.0
 */
public interface ISegmentStoreProvider {

    /**
     * Add a listener for the viewers
     *
     * @param listener
     *            listener for each type of viewer
     */
    public void addListener(IAnalysisProgressListener listener);

    /**
     * Remove listener for the viewers
     *
     * @param listener
     *            listener for each type of viewer
     */
    public void removeListener(IAnalysisProgressListener listener);

    /**
     * Return the pre-defined set of segment aspects exposed by this analysis.
     *
     * It should not be null, but could be empty.
     *
     * @return The segment aspects for this analysis
     */
    Iterable<ISegmentAspect> getSegmentAspects();

    /**
     * Returns the result in a from the analysis in a ISegmentStore
     *
     * @return Results from the analysis in a ISegmentStore
     */
    @Nullable ISegmentStore<ISegment> getSegmentStore();

}