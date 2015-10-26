/******************************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.analysis.os.linux.ui.views.latency;

import java.util.List;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.tracecompass.analysis.timing.ui.views.segmentstore.AbstractSegmentStoreTableViewer;
import org.eclipse.tracecompass.common.core.NonNullUtils;
import org.eclipse.tracecompass.internal.analysis.os.linux.ui.Activator;
import org.eclipse.tracecompass.internal.analysis.os.linux.ui.AnalysisImageConstants;
import org.eclipse.tracecompass.internal.analysis.os.linux.ui.views.latency.AbstractDensityViewer.ContentChangedListener;
import org.eclipse.tracecompass.segmentstore.core.ISegment;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.ui.views.TmfView;
import org.swtchart.Range;

public abstract class AbstractLatencyDensityView extends TmfView {

    private @Nullable AbstractDensityViewer fChartViewer;
    private @Nullable AbstractSegmentStoreTableViewer fTableViewer;

    public AbstractLatencyDensityView(String viewName) {
        super(viewName);
    }

    @Override
    public void createPartControl(@Nullable Composite parent) {
        super.createPartControl(parent);

        final SashForm sashForm = new SashForm(parent, SWT.NONE);

        fTableViewer = createLatencyTableViewer(sashForm);

        final TableColumn tableColumn = fTableViewer.getTableViewer().getTable().getColumns()[2];
        tableColumn.setWidth(100);

        fChartViewer = createLatencyDensityViewer(sashForm);

        fChartViewer.addContentChangedListener(new ContentChangedListener(){

            @Override
            public void contentChanged(List<ISegment> data) {
                updateTableModel(data);
            }

            private void updateTableModel(List<ISegment> data) {
                final AbstractSegmentStoreTableViewer viewer = fTableViewer;
                if (viewer != null) {
                    viewer.updateModel(data.toArray(new ISegment[] {}));
                }
            }

            @Override
            public void selectionChanged(List<ISegment> data) {
                updateTableModel(data);
            }

        });

        sashForm.setWeights(new int[] {4, 6});

        Action zoomOut = new Action() {
            @Override
            public void run() {
                final AbstractDensityViewer chart = fChartViewer;
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
        };
        IToolBarManager toolBar = getViewSite().getActionBars().getToolBarManager();
        toolBar.add(zoomOut);
        ITmfTrace trace = TmfTraceManager.getInstance().getActiveTrace();
        if (trace != null && fChartViewer != null) {
            fChartViewer.loadTrace(trace);
        }
    }

    abstract protected AbstractSegmentStoreTableViewer createLatencyTableViewer(Composite parent);
    abstract protected AbstractDensityViewer createLatencyDensityViewer(Composite parent);

    @Override
    public void setFocus() {
        final AbstractDensityViewer viewer = fChartViewer;
        if (viewer != null) {
            viewer.getControl().setFocus();
        }
    }

}