/**********************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 **********************************************************************/
package org.eclipse.tracecompass.internal.analysis.timing.ui.views.segmentstore.density;

import java.text.MessageFormat;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.tracecompass.analysis.timing.ui.views.segmentstore.density.AbstractSegmentStoreDensityViewer;
import org.swtchart.IAxis;
import org.swtchart.IBarSeries;
import org.swtchart.ISeries;

/**
 * Tool tip provider for density viewer. It displays the x and y
 * value of the current mouse position.
 *
 * @author Bernd Hufmann
 * @author Marc-Andre Laperle
 */
public class SimpleTooltipProvider extends BaseMouseProvider implements MouseTrackListener {

    /**
     * Constructor for a tool tip provider.
     *
     * @param densityViewer
     *            The parent density viewer
     */
    public SimpleTooltipProvider(AbstractSegmentStoreDensityViewer densityViewer) {
        super(densityViewer);
        register();
    }

    @Override
    public void register() {
        getChart().getPlotArea().addMouseTrackListener(this);
    }

    @Override
    public void deregister() {
        if (!getChart().isDisposed()) {
            getChart().getPlotArea().removeMouseTrackListener(this);
        }
    }

    @Override
    public void mouseEnter(@Nullable MouseEvent e) {
    }

    @Override
    public void mouseExit(@Nullable MouseEvent e) {
    }

    @Override
    public void mouseHover(@Nullable MouseEvent e) {
        if (e == null || getChart().getAxisSet().getXAxes().length == 0 || getChart().getAxisSet().getYAxes().length == 0) {
            return;
        }
        ISeries series = getDensityViewer().getControl().getSeriesSet().getSeries()[0];
        getChart().getPlotArea().setToolTipText(null);
        if (series instanceof IBarSeries) {
            IBarSeries barSeries = (IBarSeries) series;
            // Note: getBounds is broken in SWTChart 0.9.0
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

                    String toolTipText = MessageFormat.format(Messages.SimpleTooltipProvider_toolTipText, x1, x2, y);
                    getChart().getPlotArea().setToolTipText(toolTipText);
                    break;
                }
            }

        }
    }
}