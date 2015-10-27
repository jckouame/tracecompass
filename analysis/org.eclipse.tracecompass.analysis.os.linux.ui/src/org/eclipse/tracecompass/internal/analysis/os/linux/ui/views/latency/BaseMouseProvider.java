/**********************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 **********************************************************************/

package org.eclipse.tracecompass.internal.analysis.os.linux.ui.views.latency;

import org.swtchart.Chart;
import org.swtchart.IAxis;
import org.swtchart.ISeries;

import com.google.common.primitives.Doubles;

/**
 * Base class for any mouse provider such as tool tip, zoom and selection providers.
 *
 * @author Bernd Hufmann
 * @author Marc-Andre Laperle
 */
public abstract class BaseMouseProvider {

    private AbstractDensityViewer fDensityViewer;

    /**
     * Standard constructor.
     *
     * @param densityViewer
     *            The parent density viewer
     */
    public BaseMouseProvider(AbstractDensityViewer densityViewer) {
        fDensityViewer = densityViewer;
    }

    /**
     * Method to register the provider to the viewer.
     */
    protected abstract void register();

    /**
     * Method to deregister the provider from the viewer.
     */
    protected abstract void deregister();

    /**
     * @return the density viewer
     */
    public AbstractDensityViewer getDensityViewer() {
        return fDensityViewer;
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
        IAxis[] xAxes = getChart().getAxisSet().getXAxes();
        ISeries[] series = getChart().getSeriesSet().getSeries();
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

    /**
     * Returns the SWT chart reference
     *
     * @return SWT chart reference.
     */
    protected Chart getChart() {
        return fDensityViewer.getControl();
    }

}