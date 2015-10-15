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

import java.util.Iterator;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.analysis.os.linux.core.latency.EventChainLatencyAnalysis;
import org.eclipse.tracecompass.segmentstore.core.ISegment;
import org.eclipse.tracecompass.segmentstore.core.ISegmentStore;
import org.eclipse.tracecompass.tmf.core.analysis.IAnalysisModule;
import org.eclipse.tracecompass.tmf.core.analysis.TmfAbstractAnalysisModule;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfAnalysisException;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;

import com.google.common.collect.ImmutableList;

/**
 * Statistics for event chain latencies.
 */
public class EventChainLatencyStatisticsAnalysisModule extends TmfAbstractAnalysisModule {

    /** The analysis module ID */
    public static String ID = "org.eclipse.tracecompass.analysis.os.linux.core.latency.chainstatistics"; //$NON-NLS-1$

    private @Nullable EventChainLatencyAnalysis fLatencyModule;
    private @Nullable SegmentStoreStatistics fTotalStats;

    @Override
    protected Iterable<IAnalysisModule> getDependentAnalyses() {
        ITmfTrace trace = getTrace();
        if (trace != null) {
            EventChainLatencyAnalysis module = TmfTraceUtils.getAnalysisModuleOfClass(trace, EventChainLatencyAnalysis.class, EventChainLatencyAnalysis.ID);
            if (module != null) {
                fLatencyModule = module;
                return checkNotNull(ImmutableList.of((IAnalysisModule) module));
            }
        }
        return super.getDependentAnalyses();
    }

    @Override
    protected boolean executeAnalysis(IProgressMonitor monitor) throws TmfAnalysisException {
        EventChainLatencyAnalysis latencyAnalysis = fLatencyModule;
        ITmfTrace trace = getTrace();
        if ((latencyAnalysis == null) || (trace == null)) {
            return false;
        }
        /*
         *  Wait till dependent analysis is finished. Otherwise it the statistics
         *  cannot be calculated.
         */
        latencyAnalysis.waitForCompletion();

        ISegmentStore<ISegment> store = latencyAnalysis.getResults();

        if (store != null) {
            boolean result = calculateStatistics(store, monitor);
            if (!result) {
                return false;
            }
        }
        return true;
    }

    private boolean calculateStatistics(ISegmentStore<ISegment> store, IProgressMonitor monitor) {
        SegmentStoreStatistics total = new SegmentStoreStatistics();
        Iterator<ISegment> iter = store.iterator();
        while (iter.hasNext()) {
            if (monitor.isCanceled()) {
                return false;
            }
            ISegment segment = iter.next();
            total.update(checkNotNull(segment));
        }
        fTotalStats = total;
        return true;
    }

    @Override
    protected void canceling() {
    }

    /**
     * Returns the statistics over full segment store.
     *
     * @return the statistics over full segment store
     */
    public @Nullable SegmentStoreStatistics getTotalStats() {
        return fTotalStats;
    }
 }
