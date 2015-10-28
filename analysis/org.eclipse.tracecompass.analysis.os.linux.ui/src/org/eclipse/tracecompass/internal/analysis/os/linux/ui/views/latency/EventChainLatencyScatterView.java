/*******************************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ********************************************************************************/

package org.eclipse.tracecompass.internal.analysis.os.linux.ui.views.latency;

import static org.eclipse.tracecompass.common.core.NonNullUtils.nullToEmptyString;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tracecompass.analysis.timing.ui.views.segmentstore.AbstractSegmentStoreScatterGraphViewer;
import org.eclipse.tracecompass.common.core.NonNullUtils;
import org.eclipse.tracecompass.tmf.ui.viewers.xycharts.TmfXYChartViewer;
import org.eclipse.tracecompass.tmf.ui.views.TmfChartView;

/**
 * Scatter graph view for event chain latencies
 *
 * @author Bernd Hufmann
 */
public class EventChainLatencyScatterView extends TmfChartView {
    // Attributes
    // ------------------------------------------------------------------------

    /** The view's ID */
    public static final String ID = "org.eclipse.tracecompass.analysis.os.linux.views.latency.chainscatter"; //$NON-NLS-1$

    private @Nullable AbstractSegmentStoreScatterGraphViewer fScatterViewer;

    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------

    /**
     * Constructor
     */
    public EventChainLatencyScatterView() {
        super(ID);
    }

    // ------------------------------------------------------------------------
    // ViewPart
    // ------------------------------------------------------------------------

    @Override
    protected TmfXYChartViewer createChartViewer(@Nullable Composite parent) {
        fScatterViewer = new EventChainLatencyScatterGraphViewer(NonNullUtils.checkNotNull(parent), nullToEmptyString(Messages.LatencyScatterView_title), nullToEmptyString(Messages.LatencyScatterView_xAxis),
                nullToEmptyString(Messages.LatencyScatterView_yAxis));
        return fScatterViewer;
    }

}
