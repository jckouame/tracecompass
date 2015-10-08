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
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.PaintEvent;
import org.swtchart.Chart;
import org.swtchart.IAxis;
import org.swtchart.ICustomPaintListener;
import org.swtchart.IPlotArea;
import org.swtchart.ISeries;

import com.google.common.primitives.Doubles;

/**
 * Class for providing zooming based on mouse drag with right mouse button.
 * It also notifies the viewer about a change of range.
 *
 * @author Bernd Hufmann
 */
public class TmfMouseDragZoomProvider implements MouseListener, MouseMoveListener, ICustomPaintListener {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    /** Cached start time */
    private double fStartTime;
    /** Cached end time */
    private double fEndTime;
    /** Flag indicating that an update is ongoing */
    private boolean fIsUpdate;

    private final LatencyDensityViewer fChartViewer;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------
    /**
     * Default constructor
     *
     * @param tmfChartViewer
     *          the chart viewer reference.
     */
    public TmfMouseDragZoomProvider(LatencyDensityViewer tmfChartViewer) {
        fChartViewer = tmfChartViewer;
        register();
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
    // TmfBaseProvider
    // ------------------------------------------------------------------------
    public void register() {
        getChart().getPlotArea().addMouseListener(this);
        getChart().getPlotArea().addMouseMoveListener(this);
        ((IPlotArea) getChart().getPlotArea()).addCustomPaintListener(this);
    }

    public void deregister() {
        if (!getChartViewer().getControl().isDisposed()) {
            getChart().getPlotArea().removeMouseListener(this);
            getChart().getPlotArea().removeMouseMoveListener(this);
            ((IPlotArea) getChart().getPlotArea()).removeCustomPaintListener(this);
        }
    }

    // ------------------------------------------------------------------------
    // MouseListener
    // ------------------------------------------------------------------------
    @Override
    public void mouseDoubleClick(@Nullable MouseEvent e) {
    }

    @Override
    public void mouseDown(@Nullable MouseEvent e) {
        if (e != null && e.button == 3) {
            IAxis xAxis = getChart().getAxisSet().getXAxis(0);
            fStartTime = limitXDataCoordinate(xAxis.getDataCoordinate(e.x));
            fEndTime = fStartTime;
            fIsUpdate = true;
        }
    }

    @Override
    public void mouseUp(@Nullable MouseEvent e) {
        if ((fIsUpdate) && (fStartTime != fEndTime)) {
            if (fStartTime > fEndTime) {
                double tmp = fStartTime;
                fStartTime = fEndTime;
                fEndTime = tmp;
            }
            //double max = getChart().getAxisSet().getXAxis(0).getDataCoordinate(fStartTime);
            //fMin = getControl().getAxisSet().getXAxis(0).getDataCoordinate(e.x);
            getChartViewer().zoom(fStartTime, fEndTime);

//            LatencyDensityViewer viewer = getChartViewer();
//            viewer.updateWindow(fStartTime + viewer.getTimeOffset(), fEndTime + viewer.getTimeOffset());
        }

        if (fIsUpdate) {
            getChart().redraw();
        }
        fIsUpdate = false;
    }

    @Override
    public void mouseMove(@Nullable MouseEvent e) {
        if (e != null && fIsUpdate) {
            IAxis xAxis = getChart().getAxisSet().getXAxis(0);
            fEndTime = limitXDataCoordinate(xAxis.getDataCoordinate(e.x));
            getChart().redraw();
        }
    }

    /**
     * Limits x data coordinate to window start and window end range
     *
     * @param x
     *          x to limit
     * @return  x if x >= begin && x <= end
     *          begin if x < begin
     *          end if x > end
     */
    protected double limitXDataCoordinate(double x) {
        Chart chart = getChartViewer().getControl();
        IAxis[] xAxes = chart.getAxisSet().getXAxes();
        ISeries[] series = chart.getSeriesSet().getSeries();
        if ((xAxes.length > 0) && (series.length > 0) &&
                (series[0].getXSeries() != null) && (series[0].getXSeries().length > 0)) {
            // All series have the same X series
            double[] xSeries = series[0].getXSeries();
            double maxX = Doubles.max(xSeries);
            double minX = Doubles.min(xSeries);

            return Math.max(minX, Math.min(maxX, x));
        }

        return x;
    }

    // ------------------------------------------------------------------------
    // ICustomPaintListener
    // ------------------------------------------------------------------------
    @Override
    public void paintControl(@Nullable PaintEvent e) {
        if (e != null && fIsUpdate && (fStartTime != fEndTime)) {
            IAxis xAxis = getChart().getAxisSet().getXAxis(0);
            int startX = xAxis.getPixelCoordinate(fStartTime);
            int endX = xAxis.getPixelCoordinate(fEndTime);

            e.gc.setBackground(getChart().getDisplay().getSystemColor(SWT.COLOR_TITLE_INACTIVE_BACKGROUND));
            if (fStartTime < fEndTime) {
                e.gc.fillRectangle(startX, 0, endX - startX, e.height);
            } else {
                e.gc.fillRectangle(endX, 0, startX - endX, e.height);
            }
            e.gc.drawLine(startX, 0, startX, e.height);
            e.gc.drawLine(endX, 0, endX, e.height);
        }
    }

    @Override
    public boolean drawBehindSeries() {
        return true;
    }
}