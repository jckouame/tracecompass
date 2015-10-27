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
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.tracecompass.analysis.timing.ui.views.segmentstore.AbstractSegmentStoreTableViewer;
import org.eclipse.tracecompass.internal.analysis.os.linux.ui.views.latency.AbstractDensityViewer.ContentChangedListener;
import org.eclipse.tracecompass.segmentstore.core.ISegment;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.ui.views.TmfView;

/**
 * Displays the segment store analysis data in a scatter graph
 *
 * @author Matthew Khouzam
 * @author Marc-Andre Laperle
 */
public abstract class AbstractLatencyDensityView extends TmfView {

    @Nullable AbstractDensityViewer fChartViewer;
    private @Nullable AbstractSegmentStoreTableViewer fTableViewer;

    public AbstractLatencyDensityView(String viewName) {
        super(viewName);
    }

    private final class ContentChangedListenerImplementation implements ContentChangedListener {
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
    }

    @Override
    public void createPartControl(@Nullable Composite parent) {
        super.createPartControl(parent);

        final SashForm sashForm = new SashForm(parent, SWT.NONE);

        fTableViewer = createLatencyTableViewer(sashForm);

        final TableColumn tableColumn = fTableViewer.getTableViewer().getTable().getColumns()[2];
        tableColumn.setWidth(100);

        fChartViewer = createLatencyDensityViewer(sashForm);

        fChartViewer.addContentChangedListener(new ContentChangedListenerImplementation());

        sashForm.setWeights(new int[] {4, 6});

        Action zoomOut = new ZoomOutAction(this);
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