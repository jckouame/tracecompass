/******************************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.analysis.os.linux.ui.views.latency;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.tracecompass.analysis.os.linux.core.latency.LatencyAnalysis;
import org.eclipse.tracecompass.analysis.os.linux.core.latency.LatencyAnalysisListener;
import org.eclipse.tracecompass.common.core.NonNullUtils;
import org.eclipse.tracecompass.internal.analysis.os.linux.ui.Activator;
import org.eclipse.tracecompass.segmentstore.core.ISegment;
import org.eclipse.tracecompass.segmentstore.core.ISegmentStore;
import org.eclipse.tracecompass.segmentstore.core.SegmentComparators;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalHandler;
import org.eclipse.tracecompass.tmf.core.signal.TmfWindowRangeUpdatedSignal;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;
import org.eclipse.tracecompass.tmf.ui.viewers.TmfViewer;
import org.swtchart.Chart;
import org.swtchart.IBarSeries;
import org.swtchart.ISeries.SeriesType;
import org.swtchart.LineStyle;
import org.swtchart.Range;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.UnmodifiableIterator;

/**
 * Density viewer
 *
 * @author Matthew Khouzam
 *
 */
public class LatencyDensityViewer extends TmfViewer {

    private final String fXLabel;
    private final String fYLabel;
    private Chart fChart;
    private LatencyAnalysisListener fListener;
    private @Nullable LatencyAnalysis fAnalysisModule;
    private TmfTimeRange fCurrentRange = TmfTimeRange.NULL_RANGE;

    /**
     * Constructs a new density viewer.
     *
     * @param parent
     *            the parent of the viewer
     * @param title
     *            the title to use for the chart
     * @param xLabel
     *            the label to use for the X-axis
     * @param yLabel
     *            the label to use for the Y-axis
     */
    public LatencyDensityViewer(Composite parent, String title, String xLabel, String yLabel) {
        super(parent, title);
        fXLabel = xLabel;
        fYLabel = yLabel;
        fChart = new Chart(parent, SWT.NONE);
        fChart.getTitle().setText(title);
        fChart.getAxisSet().getXAxis(0).getTitle().setText(fXLabel);
        fChart.getAxisSet().getYAxis(0).getTitle().setText(fYLabel);
        fChart.getAxisSet().getXAxis(0).getGrid().setStyle(LineStyle.NONE);
        fChart.getAxisSet().getYAxis(0).getGrid().setStyle(LineStyle.NONE);
        fListener = new LatencyAnalysisListener() {
            @Override
            public void onComplete(LatencyAnalysis activeAnalysis, ISegmentStore<ISegment> data) {
                fAnalysisModule = activeAnalysis;
                updateWithRange(activeAnalysis);
            }
        };
        ITmfTrace trace = TmfTraceManager.getInstance().getActiveTrace();
        if (trace != null) {
            fAnalysisModule = getLatencyAnalysis(trace);
            if (fAnalysisModule != null) {
                fAnalysisModule.addListener(fListener);
            }
        }
        fChart.getPlotArea().addMouseListener(new MouseListener() {

            private double fMin = Double.MIN_VALUE;
            private double fMax = Double.MAX_VALUE;

            @Override
            public void mouseUp(@Nullable MouseEvent e) {
                if (e == null) {
                    return;
                }
                if (e.button == 3) {
                    fMax = getControl().getAxisSet().getXAxis(0).getDataCoordinate(e.x);
                    zoom(fMin, fMax);
                }
            }

            @Override
            public void mouseDown(@Nullable MouseEvent e) {
                if (e == null) {
                    return;
                }
                if (e.button == 3) {
                    fMin = getControl().getAxisSet().getXAxis(0).getDataCoordinate(e.x);
                    fMax = fMin;
                }
            }

            @Override
            public void mouseDoubleClick(@Nullable MouseEvent e) {
                // do nothing
            }
        });

    }

    private static final @Nullable LatencyAnalysis getLatencyAnalysis(ITmfTrace trace) {
        return TmfTraceUtils.getAnalysisModuleOfClass(trace, LatencyAnalysis.class, LatencyAnalysis.ID);
    }

    private void updateData(List<ISegment> data) {
        if (data.isEmpty()) {
            return;
        }
        Collections.sort(data, SegmentComparators.INTERVAL_LENGTH_COMPARATOR);
        IBarSeries series = (IBarSeries) fChart.getSeriesSet().createSeries(SeriesType.BAR, Messages.LatencyDensityViewer_SeriesLabel);
        series.setVisible(true);
        series.setBarPadding(0);
        final int width = fChart.getPlotArea().getBounds().width / 4;
        double[] xOrigSeries = new double[width];
        double[] yOrigSeries = new double[width];
        Arrays.fill(yOrigSeries, 1.0);
        long maxLength = data.get(data.size() - 1).getLength();
        double maxFactor = 1.0 / (maxLength + 1.0);
        long minX = Long.MAX_VALUE;
        for (ISegment segment : data) {
            double xBox = segment.getLength() * maxFactor * width;
            yOrigSeries[(int) xBox]++;
            minX = Math.min(minX, segment.getLength());
        }
        for (int i = 0; i < width; i++) {
            xOrigSeries[i] = i * maxLength / width;
        }
        double maxY = Double.MIN_VALUE;
        for (int i = 0; i < width; i++) {
            maxY = Math.max(maxY, yOrigSeries[i]);
        }
        if (minX == maxLength) {
            maxLength++;
            minX--;
        }
        series.setYSeries(yOrigSeries);
        series.setXSeries(xOrigSeries);
        fChart.getAxisSet().getXAxis(0).setRange(new Range(minX, maxLength));
        fChart.getAxisSet().getYAxis(0).setRange(new Range(1.0, maxY));
        fChart.getAxisSet().getYAxis(0).enableLogScale(true);
        fChart.redraw();
    }

    @Override
    public Chart getControl() {
        return fChart;
    }

    public void zoom(final double min, final double max) {
        LatencyAnalysis analysisModule = fAnalysisModule;
        if (analysisModule == null) {
            return;
        }
        ISegmentStore<ISegment> results = analysisModule.getResults();
        if (results == null) {
            return;
        }
        Iterable<ISegment> intersectingElements = results.getIntersectingElements(fCurrentRange.getStartTime().getValue(), fCurrentRange.getEndTime().getValue());
        Predicate<? super ISegment> predicate = new Predicate<ISegment>() {
            @Override
            public boolean apply(@Nullable ISegment input) {

                return input != null && input.getLength() >= min && input.getLength() <= max;
            }

        };
        final UnmodifiableIterator<ISegment> intersection = Iterators.<ISegment> filter(intersectingElements.iterator(), predicate);

        Display.getDefault().asyncExec(new Runnable() {
            @Override
            public void run() {
                updateData(NonNullUtils.checkNotNull(Lists.newArrayList(intersection)));
            }
        });
    }

    /**
     * Signal handler for handling of the window range signal.
     *
     * @param signal
     *            The {@link TmfWindowRangeUpdatedSignal}
     */
    @TmfSignalHandler
    public void windowRangeUpdated(@Nullable TmfWindowRangeUpdatedSignal signal) {
        if (signal == null) {
            return;
        }
        ITmfTrace trace = getTrace();
        if (trace == null) {
            Activator.getDefault().logInfo("No Trace to update"); //$NON-NLS-1$
            return;
        }
        fAnalysisModule = getLatencyAnalysis(trace);
        if (fAnalysisModule != null) {
            fAnalysisModule.addListener(fListener);
        }
        fCurrentRange = NonNullUtils.checkNotNull(signal.getCurrentRange());
        LatencyAnalysis analysisModule = fAnalysisModule;
        if (analysisModule == null) {
            return;
        }
        updateWithRange(analysisModule);
    }

    private void updateWithRange(LatencyAnalysis analysisModule) {
        ISegmentStore<ISegment> results = analysisModule.getResults();
        if (results != null) {
            final Iterable<ISegment> intersection = results.getIntersectingElements(fCurrentRange.getStartTime().getValue(), fCurrentRange.getEndTime().getValue());
            Display.getDefault().asyncExec(new Runnable() {

                @Override
                public void run() {
                    updateData(Lists.newArrayList(intersection));
                }
            });
        }
    }

    private static @Nullable ITmfTrace getTrace() {
        return TmfTraceManager.getInstance().getActiveTrace();
    }

    @Override
    public void refresh() {
        fChart.redraw();
    }

    @Override
    public void dispose() {
        if (fAnalysisModule != null) {
            fAnalysisModule.addListener(fListener);
        }
        super.dispose();
    }
}
