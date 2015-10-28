/******************************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Jean-Christian Kouame - Initial API and implementation
 *******************************************************************************/
package org.eclipse.tracecompass.analysis.os.linux.ui.views.xmlIrqAverageLatency;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.tracecompass.common.core.NonNullUtils;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceSelectedSignal;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.ui.views.TmfView;

/**
 * Displays the IRQ average latency analysis in a tree table
 *
 */
public class XMLIrqAverageLatency extends TmfView {

    private final static String ID = "org.eclipse.tracecompass.tmf.ui.xmlIrqAverageLatency"; //$NON-NLS-1$
    private XMLIrqAverageLatencyViewer fViewer;

    /**
     * Constructor
     */
    public XMLIrqAverageLatency() {
        super(ID);
    }

    @Override
    public void createPartControl(Composite parent) {
        fViewer = new XMLIrqAverageLatencyViewer(NonNullUtils.checkNotNull(parent));
        ITmfTrace trace = TmfTraceManager.getInstance().getActiveTrace();
        if (trace != null) {
            fViewer.traceSelected(new TmfTraceSelectedSignal(this, trace));
        }
    }

    @Override
    public void setFocus() {
        if (fViewer != null) {
            fViewer.getControl().setFocus();
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        if (fViewer != null) {
            fViewer.dispose();
        }
    }
}
