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
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.tracecompass.common.core.NonNullUtils;
import org.eclipse.tracecompass.internal.analysis.os.linux.ui.Activator;
import org.eclipse.tracecompass.internal.analysis.os.linux.ui.AnalysisImageConstants;
import org.eclipse.tracecompass.tmf.ui.viewers.table.TmfSimpleTableViewer;
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
    private @Nullable SashForm fSashForm;

    private @Nullable LatencyDensityViewer fChart;
    private @Nullable TmfSimpleTableViewer fTableViewer;
    /**
     * Constructs a new density view.
     */
    public LatencyDensityView() {
        super(ID);
    }

    @Override
    public void createPartControl(@Nullable Composite parent) {
        super.createPartControl(parent);

        fSashForm = new SashForm(parent, SWT.NONE);

        TableViewer t = new TableViewer(fSashForm);
        fTableViewer = new TmfSimpleTableViewer(t);
        Table table = fTableViewer.getTableViewer().getTable();

        TableItem tableItem = new TableItem(table, SWT.NONE);
        tableItem.setText("Test");

        fChart = new LatencyDensityViewer(NonNullUtils.checkNotNull(fSashForm),
                nullToEmptyString(Messages.LatencyDensityView_ChartTitle),
                nullToEmptyString(Messages.LatencyDensityView_TimeAxisLabel),
                nullToEmptyString(Messages.LatencyDensityView_CountAxisLabel));
        final SashForm sashForm = fSashForm;
        if (sashForm != null) {
            sashForm.setWeights(new int[] {3, 7});
        }

        Action zoomOut = new Action() {
            @Override
            public void run() {
                final LatencyDensityViewer chart = fChart;
                if (chart != null) {
                    chart.zoom(0, Long.MAX_VALUE);
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
