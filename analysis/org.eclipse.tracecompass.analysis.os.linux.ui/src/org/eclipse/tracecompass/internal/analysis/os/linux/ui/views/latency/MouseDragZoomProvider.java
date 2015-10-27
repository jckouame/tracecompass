/**********************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 **********************************************************************/

package org.eclipse.tracecompass.internal.analysis.os.linux.ui.views.latency;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.PaintEvent;
import org.swtchart.IAxis;
import org.swtchart.ICustomPaintListener;
import org.swtchart.IPlotArea;
import org.swtchart.Range;

/**
 * Class for providing zooming based on mouse drag with right mouse button.
 * It also notifies the viewer about a change of range.
 *
 * @author Bernd Hufmann
 * @author Marc-Andre Laperle
 */
public class MouseDragZoomProvider extends BaseMouseProvider implements MouseListener, MouseMoveListener, ICustomPaintListener {

    /** Cached start time */
    private double fStartTime;
    /** Cached end time */
    private double fEndTime;
    /** Flag indicating that an update is ongoing */
    private boolean fIsUpdate;

    /**
     * Default constructor
     *
     * @param densityViewer
     *            the density viewer reference.
     */
    public MouseDragZoomProvider(AbstractDensityViewer densityViewer) {
        super(densityViewer);
        register();
    }

    @Override
    public void register() {
        getChart().getPlotArea().addMouseListener(this);
        getChart().getPlotArea().addMouseMoveListener(this);
        ((IPlotArea) getChart().getPlotArea()).addCustomPaintListener(this);
    }

    @Override
    public void deregister() {
        if (!getChart().isDisposed()) {
            getChart().getPlotArea().removeMouseListener(this);
            getChart().getPlotArea().removeMouseMoveListener(this);
            ((IPlotArea) getChart().getPlotArea()).removeCustomPaintListener(this);
        }
    }

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
            getDensityViewer().zoom(new Range(fStartTime, fEndTime));
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