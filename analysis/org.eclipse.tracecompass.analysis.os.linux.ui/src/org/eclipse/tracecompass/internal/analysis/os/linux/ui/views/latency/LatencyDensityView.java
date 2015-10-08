/******************************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.analysis.os.linux.ui.views.latency;

import static org.eclipse.tracecompass.common.core.NonNullUtils.nullToEmptyString;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tracecompass.common.core.NonNullUtils;
import org.eclipse.tracecompass.internal.analysis.os.linux.ui.Activator;
import org.eclipse.tracecompass.internal.analysis.os.linux.ui.AnalysisImageConstants;
import org.eclipse.tracecompass.tmf.ui.views.TmfView;
import org.eclipse.ui.IActionBars;

/**
 * Latency Density view
 *
 * @author Matthew Khouzam
 *
 */
public class LatencyDensityView extends TmfView {
    /** The view's ID */
    public static final String ID = "org.eclipse.tracecompass.analysis.os.linux.views.latency.density"; //$NON-NLS-1$

    private @Nullable LatencyDensityViewer fChart;
    /**
     * Constructs a new density view.
     */
    public LatencyDensityView() {
        super(ID);
    }

    @Override
    public void createPartControl(@Nullable Composite parent) {
        super.createPartControl(parent);
        fChart = new LatencyDensityViewer(NonNullUtils.checkNotNull(parent),
                nullToEmptyString(Messages.LatencyDensityView_ChartTitle),
                nullToEmptyString(Messages.LatencyDensityView_TimeAxisLabel),
                nullToEmptyString(Messages.LatencyDensityView_CountAxisLabel));
        Action zoomOut = new Action() {
            @Override
            public void run() {
                fChart.zoom(0, Long.MAX_VALUE);
            }

            @Override
            public ImageDescriptor getImageDescriptor() {
                return NonNullUtils.checkNotNull(Activator.getDefault().getImageDescripterFromPath(AnalysisImageConstants.IMG_UI_ZOOM_OUT_MENU));
            }

            @Override
            public String getToolTipText() {
                return NonNullUtils.checkNotNull(Messages.LatencyDensityView_ZoomOutActionToolTipText);
            }
        };
        IActionBars actionBars = getViewSite().getActionBars();
        IToolBarManager toolBar = actionBars.getToolBarManager();
        toolBar.add(zoomOut);
    }

    @Override
    public void setFocus() {
        final LatencyDensityViewer chart = fChart;
        if (chart != null) {
            chart.getControl().setFocus();
        }
    }

}
