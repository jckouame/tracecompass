/**********************************************************************
 * Copyright (c) 2013, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 **********************************************************************/
package org.eclipse.tracecompass.internal.analysis.os.linux.ui.views.latency;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackListener;
import org.swtchart.Chart;
import org.swtchart.IAxis;

/**
 * Tool tip provider for TMF chart viewer. It displays the x and y
 * value of the current mouse position.
 *
 * @author Bernd Hufmann
 */
public class TmfSimpleTooltipProvider implements MouseTrackListener {

    private LatencyDensityViewer fChartViewer;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------
    /**
     * Constructor for a tool tip provider.
     *
     * @param tmfChartViewer
     *                  The parent chart viewer
     */
    public TmfSimpleTooltipProvider(LatencyDensityViewer tmfChartViewer) {
        fChartViewer = tmfChartViewer;
        register();
    }

    // ------------------------------------------------------------------------
    // TmfBaseProvider
    // ------------------------------------------------------------------------
    public void register() {
        getChart().getPlotArea().addMouseTrackListener(this);
    }

    public void deregister() {
        if (!getChartViewer().getControl().isDisposed()) {
            getChart().getPlotArea().removeMouseTrackListener(this);
        }
    }

    /**
     * Returns the SWT chart reference
     *
     * @return SWT chart reference.
     */
    protected Chart getChart() {
        return fChartViewer.getControl();
    }

    /**
     * Returns the chart viewer reference.
     * @return the chart viewer reference
     */
    public LatencyDensityViewer getChartViewer() {
        return fChartViewer;
    }

    // ------------------------------------------------------------------------
    // MouseTrackListener
    // ------------------------------------------------------------------------
    @Override
    public void mouseEnter(@Nullable MouseEvent e) {
    }

    @Override
    public void mouseExit(@Nullable MouseEvent e) {
    }

    @Override
    public void mouseHover(@Nullable MouseEvent e) {
        if (e == null || getChartViewer().getControl().getAxisSet().getXAxes().length == 0) {
            return;
        }

        IAxis xAxis = getChart().getAxisSet().getXAxis(0);
        IAxis yAxis = getChart().getAxisSet().getYAxis(0);

        double xCoordinate = xAxis.getDataCoordinate(e.x);
        double yCoordinate = yAxis.getDataCoordinate(e.y);

//        LatencyDensityViewer viewer = getChartViewer();

        /* set tooltip of current data point */
        StringBuffer buffer = new StringBuffer();
        buffer.append("x="); //$NON-NLS-1$
        buffer.append(xCoordinate + " ns");
        buffer.append("\n"); //$NON-NLS-1$
        buffer.append("y="); //$NON-NLS-1$
        buffer.append((long) yCoordinate);
        getChart().getPlotArea().setToolTipText(buffer.toString());
    }
}