/******************************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.analysis.os.linux.ui.views.latency;

import org.eclipse.tracecompass.analysis.os.linux.core.latency.EventChainLatencyAnalysis;
import org.eclipse.tracecompass.analysis.timing.core.latency.AbstractLatencyAnalysisModule;

/**
 * Latency Density view for event chain latencies
 */
public class EventChainLatencyDensityView extends AbstractLatencyDensityView {
    /** The view's ID */
    public static final String ID = "org.eclipse.tracecompass.analysis.os.linux.views.latency.chaindensity"; //$NON-NLS-1$
    /**
     * Constructs a new density view.
     */
    public EventChainLatencyDensityView() {
        super(ID);
    }

    @Override
    protected Class<? extends AbstractLatencyAnalysisModule> getAnalysisModuleClass() {
        return EventChainLatencyAnalysis.class;
    }

    @Override
    protected String getAnalysisModuleId() {
        return EventChainLatencyAnalysis.ID;
    }

}
