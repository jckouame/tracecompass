/*******************************************************************************
 * Copyright (c) 2015, 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   France Lapointe Nguyen - Initial API and implementation
 *   Bernd Hufmann - Move abstract class to TMF
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.timing.ui.views.segmentstore.table;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tracecompass.analysis.timing.core.segmentstore.ISegmentStoreProvider;
import org.eclipse.tracecompass.common.core.NonNullUtils;
import org.eclipse.tracecompass.internal.analysis.timing.ui.views.filter.dialog.LatencyViewFilterDialog;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.ui.views.TmfView;
import org.eclipse.ui.IActionBars;

/**
 * View for displaying a segment store analysis in a table.
 *
 * @author France Lapointe Nguyen
 * @since 2.0
 */
public abstract class AbstractSegmentStoreTableView extends TmfView {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    private @Nullable AbstractSegmentStoreTableViewer fSegmentStoreViewer;
    private @Nullable Action fFilter;
    private @Nullable Composite fParent;

    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------

    /**
     * Constructor
     */
    public AbstractSegmentStoreTableView() {
        super(""); //$NON-NLS-1$
    }

    // ------------------------------------------------------------------------
    // ViewPart
    // ------------------------------------------------------------------------

    @Override
    public void createPartControl(@Nullable Composite parent) {
        fParent = parent;
        SashForm sf = new SashForm(parent, SWT.NONE);
        TableViewer tableViewer = new TableViewer(sf, SWT.FULL_SELECTION | SWT.VIRTUAL);

        fSegmentStoreViewer = createSegmentStoreViewer(tableViewer);
        setInitialData();

        IActionBars bars = getViewSite().getActionBars();
        fillLocalToolBar(NonNullUtils.checkNotNull(bars.getToolBarManager()));
    }

    private void fillLocalToolBar(IToolBarManager manager) {
        manager.add(createFilterActions());
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    private @Nullable Action createFilterActions() {
        if (fFilter == null) {
            fFilter = new Action() {
                @Override
                public void run() {
                    showFilterDialog();
                }
            };
        }
        return fFilter;
    }

    private void showFilterDialog() {
        final Composite parent = fParent;
        ITmfTrace trace = TmfTraceManager.getInstance().getActiveTrace();
        final @Nullable AbstractSegmentStoreTableViewer viewer = fSegmentStoreViewer;
        if (trace != null && parent != null && viewer != null && !parent.isDisposed()) {
            @Nullable ISegmentStoreProvider segmentProvider = viewer.getSegmentProvider();
            if (segmentProvider != null) {
                LatencyViewFilterDialog dialog = new LatencyViewFilterDialog(NonNullUtils.checkNotNull(parent.getShell()), trace, viewer.getColumnsAspects(), segmentProvider.getProviderId());
                dialog.open();
            }
        }
    }

    @Override
    public void setFocus() {
        if (fSegmentStoreViewer != null) {
            fSegmentStoreViewer.getTableViewer().getControl().setFocus();
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        if (fSegmentStoreViewer != null) {
            fSegmentStoreViewer.dispose();
        }
    }

    /**
     * Returns the latency analysis table viewer instance
     *
     * @param tableViewer
     *            the table viewer to use
     * @return the latency analysis table viewer instance
     */
    protected abstract AbstractSegmentStoreTableViewer createSegmentStoreViewer(TableViewer tableViewer);

    /**
     * Get the table viewer
     *
     * @return the table viewer, useful for testing
     */
    @Nullable
    public AbstractSegmentStoreTableViewer getSegmentStoreViewer() {
        return fSegmentStoreViewer;
    }

    /**
     * Set initial data into the viewer
     */
    private void setInitialData() {
        if (fSegmentStoreViewer != null) {
            @NonNull AbstractSegmentStoreTableViewer segmentStoreViewer = fSegmentStoreViewer;
            @Nullable ISegmentStoreProvider segmentProvider = segmentStoreViewer.getSegmentProvider();
            segmentStoreViewer.setData(segmentProvider);
        }
    }
}
