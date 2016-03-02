/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.tracecompass.tmf.analysis.xml.ui.module;

import org.eclipse.tracecompass.tmf.ui.analysis.TmfAnalysisViewOutput;

/**
 * Class overriding the default analysis view output for XML pattern analysis
 * Latency views
 *
 * @author Jean-Christian Kouame
 * @since 2.0
 *
 */
public class TmfXmlLatencyViewOutput extends TmfAnalysisViewOutput {

    private String fLabel;

    /**
     * @param viewid
     *            The ID of the view
     * @param label
     *            The label of view
     */
    public TmfXmlLatencyViewOutput(String viewid, String label) {
        super(viewid);
        fLabel = label;
    }

    @Override
    public String getName() {
        return fLabel;
    }
}
