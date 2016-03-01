/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.tracecompass.tmf.analysis.xml.ui.views.latency;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.tracecompass.analysis.timing.core.segmentstore.ISegmentStoreProvider;
import org.eclipse.tracecompass.analysis.timing.ui.views.segmentstore.table.AbstractSegmentStoreTableViewer;
import org.eclipse.tracecompass.tmf.analysis.xml.core.stateprovider.XmlPatternAnalysis;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;

/**
 * Displays the latency analysis data in a column table
 *
 * @author Jean-Christian Kouame
 *
 */
public class PatternLatencyTableViewer extends AbstractSegmentStoreTableViewer {

    private String fAnalysisId;

    /**
     * Constructor
     *
     * @param tableViewer
     *            The table viewer
     */
    public PatternLatencyTableViewer(@NonNull TableViewer tableViewer) {
        super(tableViewer);
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
