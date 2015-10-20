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
import org.eclipse.swt.graphics.Rectangle;
import org.swtchart.Chart;
import org.swtchart.IAxis;
import org.swtchart.IBarSeries;
import org.swtchart.ISeries;

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
        if (e == null || getChartViewer().getControl().getAxisSet().getXAxes().length == 0 || getChartViewer().getControl().getAxisSet().getYAxes().length == 0) {
            return;
        }
        ISeries series = getChartViewer().getControl().getSeriesSet().getSeries()[0];
        getChart().getPlotArea().setToolTipText(null);
        if (series instanceof IBarSeries) {
            IBarSeries barSeries = (IBarSeries) series;
            Rectangle[] bounds = barSeries.getBounds();

            for (Rectangle rec : bounds) {
                if (rec == null) {
                    continue;
                }

                int start = rec.x;
                int end = start + rec.width;
                if (e.x >= start && e.x <= end) {

                    IAxis xAxis = getChart().getAxisSet().getXAxes()[0];
                    IAxis yAxis = getChart().getAxisSet().getYAxes()[0];
                    long x1 = Math.round(Math.max(0, xAxis.getDataCoordinate(start)));
                    long x2 = Math.round(Math.max(0, xAxis.getDataCoordinate(end)));
                    long y = Math.round(yAxis.getDataCoordinate(rec.y));

                    /* set tooltip of current data point */
                    StringBuffer buffer = new StringBuffer();
                    buffer.append("Duration: ["); //$NON-NLS-1$
                    buffer.append(x1);
                    buffer.append(", "); //$NON-NLS-1$
                    buffer.append(x2);
                    buffer.append("] ns"); //$NON-NLS-1$
                    buffer.append("\n"); //$NON-NLS-1$
                    buffer.append("Count: "); //$NON-NLS-1$
                    buffer.append(y);
                    getChart().getPlotArea().setToolTipText(buffer.toString());
                    break;
                }
            }

        }


    }
}