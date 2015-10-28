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
package org.eclipse.tracecompass.internal.analysis.os.linux.ui.views.latency.statistics;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import java.util.List;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tracecompass.internal.analysis.os.linux.core.latency.statistics.EventChainLatencyStatisticsAnalysisModule;
import org.eclipse.tracecompass.internal.analysis.os.linux.core.latency.statistics.LatencyStatistics;
import org.eclipse.tracecompass.tmf.core.analysis.TmfAbstractAnalysisModule;
import org.eclipse.tracecompass.tmf.ui.viewers.tree.ITmfTreeViewerEntry;
import org.eclipse.tracecompass.tmf.ui.viewers.tree.TmfTreeViewerEntry;

/**
 * A tree viewer implementation for displaying latency statistics
 *
 * @author Bernd Hufmann
 */
public class EventChainLatencyStatisticsViewer extends AbstractSegmentStoreStatisticsViewer {

    private static final String[] DURATION_NAMES = new String[] {
            org.eclipse.tracecompass.analysis.os.linux.core.latency.Messages.LatencyChain_TimerElapsedLabel,
            org.eclipse.tracecompass.analysis.os.linux.core.latency.Messages.LatencyChain_TimeToSchedWakeUpLabel,
            org.eclipse.tracecompass.analysis.os.linux.core.latency.Messages.LatencyChain_TimeToSchedSwitchLabel
    };


    /**
     * Constructor
     *
     * @param parent
     *            the parent composite
     */
    public EventChainLatencyStatisticsViewer(Composite parent) {
        super(parent);
    }

    /**
     * Gets the statistics analysis module
     *
     * @return the statistics analysis module
     */
    @Override
    @Nullable protected TmfAbstractAnalysisModule createStatisticsAnalysiModule() {
        EventChainLatencyStatisticsAnalysisModule module = new EventChainLatencyStatisticsAnalysisModule();
        return module;
    }

    @Override
    @Nullable protected ITmfTreeViewerEntry updateElements(long start, long end, boolean isSelection) {
        if (isSelection || (start == end)) {
            return null;
        }

        TmfAbstractAnalysisModule analysisModule = getStatisticsAnalysisModule();

        if (getTrace() == null || !(analysisModule instanceof EventChainLatencyStatisticsAnalysisModule)) {
            return null;
        }

        EventChainLatencyStatisticsAnalysisModule module = (EventChainLatencyStatisticsAnalysisModule) analysisModule;

        module.waitForCompletion();

        LatencyStatistics entry = module.getTotalStats();

        TmfTreeViewerEntry root = new TmfTreeViewerEntry(""); //$NON-NLS-1$
        List<ITmfTreeViewerEntry> entryList = root.getChildren();
        if ((entry != null) && (entry.getNbSegments() > 0)) {
            TmfTreeViewerEntry child = new LatencyTreeViewerEntry(checkNotNull(Messages.LatencyStatistics_TotalLabel), checkNotNull(entry));
            entryList.add(child);

            int i = 0;
            for (LatencyStatistics subEntry : entry.getChildren()) {
                child.addChild(new LatencyTreeViewerEntry(checkNotNull(DURATION_NAMES[i]), checkNotNull(subEntry)));
                i++;
            }
        }
        return root;
    }

}
