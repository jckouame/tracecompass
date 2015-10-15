/*******************************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Bernd Hufmann - Initial API and implementation
 *******************************************************************************/
package org.eclipse.tracecompass.internal.analysis.os.linux.core.latency.statistics;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.analysis.os.linux.core.latency.EventChainSegments;
import org.eclipse.tracecompass.segmentstore.core.ISegment;

import com.google.common.collect.ImmutableList;

/**
 * Class to calculate simple latency statistics (min, max, average)
 *
 * @author Bernd Hufmann
 */
public class LatencyStatistics {
    private long fMin;
    private long fMax;
    private long fSum;
    private long fNbSegments;
    @Nullable private List<LatencyStatistics> fChildren;

    /**
     * Constructor
     */
    public LatencyStatistics() {
        this.fMin = Long.MAX_VALUE;
        this.fMax = Long.MIN_VALUE;
        this.fSum = 0;
        this.fNbSegments = 0;
    }

    /**
     * Get minimum value
     *
     * @return minimum value
     */
    public long getMin() {
        return fMin;
    }

    /**
     * Get maximum value
     *
     * @return maximum value
     */
    public long getMax() {
        return fMax;
    }

    /**
     * Get number of segments analyzed
     *
     * @return number of segments analyzed
     */
    public long getNbSegments() {
        return fNbSegments;
    }

    /**
     * Gets the arithmetic average
     *
     * @return arithmetic average
     */
    public double getAverage() {
        return ((double) fSum) / fNbSegments;
    }

    /**
     * Gets children LatencyStatistics
     *
     * @return children LatencyStatistics
     */
    public List<LatencyStatistics> getChildren() {
        List<LatencyStatistics> children = fChildren;
        if (children != null) {
            return checkNotNull(ImmutableList.copyOf(children));
        }
        return checkNotNull(Collections.EMPTY_LIST);
    }

    /**
     * Update the statistics based on a given segment
     *
     * @param segment
     *            the segment used for the update
     */
    public void update (ISegment segment) {
        long value = segment.getLength();
        fMin = Math.min(fMin, value);
        fMax = Math.max(fMax, value);
        fSum += value;
        fNbSegments++;

        if (segment instanceof EventChainSegments) {
            List<LatencyStatistics> children = fChildren;
            List<ISegment> subsegments = ((EventChainSegments) segment).getSubSegments();
            if (children == null) {
                children = new ArrayList<>();
                for (int i = 0; i < subsegments.size(); i++) {
                    children.add(new LatencyStatistics());
                }
            }
            for (int i = 0; i < subsegments.size(); i++) {
                children.get(i).update(checkNotNull(subsegments.get(i)));
            }
            fChildren = children;
        }
    }

}