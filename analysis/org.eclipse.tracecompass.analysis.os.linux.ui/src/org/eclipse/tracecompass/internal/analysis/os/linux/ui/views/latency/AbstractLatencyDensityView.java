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

import java.util.List;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.tracecompass.analysis.timing.core.latency.AbstractLatencyAnalysisModule;
import org.eclipse.tracecompass.common.core.NonNullUtils;
import org.eclipse.tracecompass.internal.analysis.os.linux.ui.Activator;
import org.eclipse.tracecompass.internal.analysis.os.linux.ui.AnalysisImageConstants;
import org.eclipse.tracecompass.internal.analysis.os.linux.ui.views.latency.LatencyDensityViewer.ContentChangedListener;
import org.eclipse.tracecompass.segmentstore.core.ISegment;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.ui.views.TmfView;
import org.eclipse.ui.IActionBars;

public abstract class AbstractLatencyDensityView extends TmfView {

    private @Nullable SashForm fSashForm;

    private @Nullable LatencyDensityViewer fChartViewer;
    private @Nullable LatencyTableViewer fTableViewer;

    public AbstractLatencyDensityView(String viewName) {
        super(viewName);
    }

    abstract protected Class<? extends AbstractLatencyAnalysisModule> getAnalysisModuleClass();

    abstract protected String getAnalysisModuleId();


    @Override
    public void createPartControl(@Nullable Composite parent) {
        super.createPartControl(parent);

        fSashForm = new SashForm(parent, SWT.NONE);

        TableViewer t = new TableViewer(fSashForm, SWT.FULL_SELECTION | SWT.VIRTUAL);
        fTableViewer = new LatencyTableViewer(t);
        final TableColumn tableColumn = t.getTable().getColumns()[2];
        tableColumn.setWidth(100);

        fChartViewer = new LatencyDensityViewer(NonNullUtils.checkNotNull(fSashForm),
                nullToEmptyString(Messages.LatencyDensityView_ChartTitle),
                nullToEmptyString(Messages.LatencyDensityView_TimeAxisLabel),
                nullToEmptyString(Messages.LatencyDensityView_CountAxisLabel), getAnalysisModuleClass(), getAnalysisModuleId());

        fChartViewer.addContentChangedListener(new ContentChangedListener(){

            @Override
            public void contentChanged(List<ISegment> data) {
                final LatencyTableViewer viewer = fTableViewer;
                if (viewer != null) {
                    viewer.updateModel(data.toArray(new ISegment[] {}));
                }
            }

        });

        final SashForm sashForm = fSashForm;
        if (sashForm != null) {
            sashForm.setWeights(new int[] {4, 6});
        }

        Action zoomOut = new Action() {
            @Override
            public void run() {
                final LatencyDensityViewer chart = fChartViewer;
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
        ITmfTrace trace = TmfTraceManager.getInstance().getActiveTrace();
        if (trace != null && fChartViewer != null) {
            fChartViewer.loadTrace(trace);
        }
    }

    @Override
    public void setFocus() {
        final LatencyDensityViewer viewer = fChartViewer;
        if (viewer != null) {
            viewer.getControl().setFocus();
        }
    }

}