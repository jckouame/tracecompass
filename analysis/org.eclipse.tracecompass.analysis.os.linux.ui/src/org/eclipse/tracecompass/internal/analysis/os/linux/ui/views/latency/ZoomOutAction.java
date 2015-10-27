/******************************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.analysis.os.linux.ui.views.latency;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.tracecompass.common.core.NonNullUtils;
import org.eclipse.tracecompass.internal.analysis.os.linux.ui.Activator;
import org.eclipse.tracecompass.internal.analysis.os.linux.ui.AnalysisImageConstants;
import org.swtchart.Range;

/**
 * Zoom action for the densify view
 */
public class ZoomOutAction extends Action {

    private final AbstractLatencyDensityView fView;

    /**
     * @param abstractLatencyDensityView
     */
    public ZoomOutAction(AbstractLatencyDensityView abstractLatencyDensityView) {
        fView = abstractLatencyDensityView;
    }

    @Override
    public void run() {
        final AbstractDensityViewer chart = fView.fChartViewer;
        if (chart != null) {
            chart.zoom(new Range(0, Long.MAX_VALUE));
        }
    }

    @Override
    public ImageDescriptor getImageDescriptor() {
        return NonNullUtils.checkNotNull(Activator.getDefault().getImageDescripterFromPath(AnalysisImageConstants.IMG_UI_ZOOM_OUT_MENU));
    }

    @Override
    public String getToolTipText() {
        return NonNullUtils.checkNotNull(Messages.LatencyDensityView_ZoomOutActionToolTipText);
    }
}