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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.analysis.os.linux.core.latency.LatencyAnalysis;
import org.eclipse.tracecompass.analysis.os.linux.core.latency.SystemCall;
import org.eclipse.tracecompass.segmentstore.core.ISegment;
import org.eclipse.tracecompass.segmentstore.core.ISegmentStore;
import org.eclipse.tracecompass.tmf.core.analysis.IAnalysisModule;
import org.eclipse.tracecompass.tmf.core.analysis.TmfAbstractAnalysisModule;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfAnalysisException;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;

import com.google.common.collect.ImmutableList;

/**
 * Analysis module to calculate statistics of a latency analysis
 *
 * @author Bernd Hufmann
 */
public class LatencyStatisticsAnalysisModule extends TmfAbstractAnalysisModule {

    /** The analysis module ID */
    public static String ID = "org.eclipse.tracecompass.analysis.os.linux.core.latency.statistics"; //$NON-NLS-1$

    private @Nullable LatencyAnalysis fLatencyModule;

    private @Nullable LatencyStatistics fTotalStats;

    private @Nullable Map<String, LatencyStatistics> fPerSyscallStats;

    @Override
    protected Iterable<IAnalysisModule> getDependentAnalyses() {
        ITmfTrace trace = getTrace();
        if (trace != null) {
            LatencyAnalysis module = TmfTraceUtils.getAnalysisModuleOfClass(trace, LatencyAnalysis.class, checkNotNull(LatencyAnalysis.ID));
            if (module != null) {
                fLatencyModule = module;
                return checkNotNull(ImmutableList.of((IAnalysisModule) module));
            }
        }
        return super.getDependentAnalyses();
    }

    @Override
    protected boolean executeAnalysis(IProgressMonitor monitor) throws TmfAnalysisException {
        LatencyAnalysis latency = fLatencyModule;
        ITmfTrace trace = getTrace();
        if ((latency == null) || (trace == null)) {
            return false;
        }
        latency.waitForCompletion();

        ISegmentStore<ISegment> store = latency.getResults();

        if (store != null) {

            boolean result = calculateTotalManual(store, monitor);

            if (!result) {
                return false;
            }

            result = calculateTotalPerSyscall(store, monitor);
            if (!result) {
                return false;
            }
        }
        return true;
    }

    private boolean calculateTotalManual(ISegmentStore<ISegment> store, IProgressMonitor monitor) {
        LatencyStatistics total = new LatencyStatistics();
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

    private boolean calculateTotalPerSyscall(ISegmentStore<ISegment> store, IProgressMonitor monitor) {
        Map<String, LatencyStatistics> perSyscallStats = new HashMap<>();

        Iterator<ISegment> iter = store.iterator();
        while (iter.hasNext()) {
            if (monitor.isCanceled()) {
                return false;
            }
            ISegment segment = iter.next();
            if (segment instanceof SystemCall) {
                SystemCall syscall = (SystemCall) segment;
                LatencyStatistics values = perSyscallStats.get(syscall.getName());
                if (values == null) {
                    values = new LatencyStatistics();
                }
                values.update(segment);
                perSyscallStats.put(syscall.getName(), values);
            }
        }
        fPerSyscallStats = perSyscallStats;
        return true;
    }

    @Override
    protected void canceling() {
    }

    /**
     * The total statistics
     *
     * @return the total statistics
     */
    public @Nullable LatencyStatistics getTotalStats() {
        return fTotalStats;
    }

    /**
     * The per syscall statistics
     *
     * @return the per syscall statistics
     */
    public @Nullable Map<String, LatencyStatistics> getPerSyscallStats() {
        return fPerSyscallStats;
    }

 }
