/******************************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.analysis.os.linux.ui.views.latency;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
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
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceClosedSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceOpenedSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceSelectedSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfWindowRangeUpdatedSignal;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceContext;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;
import org.eclipse.tracecompass.tmf.ui.viewers.TmfViewer;
import org.swtchart.Chart;
import org.swtchart.IAxis;
import org.swtchart.IBarSeries;
import org.swtchart.ISeries;
import org.swtchart.ISeries.SeriesType;
import org.swtchart.ISeriesSet;
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

    interface ContentChangedListener {
        void contentChanged(List<ISegment> data);
    }

    private final String fXLabel;
    private final String fYLabel;
    private Chart fChart;
    private @Nullable LatencyAnalysisListener fListener;
    private @Nullable LatencyAnalysis fAnalysisModule;
    private TmfTimeRange fCurrentRange = TmfTimeRange.NULL_RANGE;
    private TmfMouseDragZoomProvider fDragZoomProvider;
    private TmfSimpleTooltipProvider fTooltipProvider;
    private @Nullable ITmfTrace fTrace;
    private List<ContentChangedListener> fListeners;

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
        fListeners = new ArrayList<>();
        fXLabel = xLabel;
        fYLabel = yLabel;
        fChart = new Chart(parent, SWT.NONE);
        fChart.getTitle().setText(title);
        fChart.getAxisSet().getXAxis(0).getTitle().setText(fXLabel);
        fChart.getAxisSet().getYAxis(0).getTitle().setText(fYLabel);
        fChart.getAxisSet().getXAxis(0).getGrid().setStyle(LineStyle.NONE);
        fChart.getAxisSet().getYAxis(0).getGrid().setStyle(LineStyle.NONE);

        fDragZoomProvider = new TmfMouseDragZoomProvider(this);
        fDragZoomProvider.register();
        fTooltipProvider = new TmfSimpleTooltipProvider(this);
        fTooltipProvider.register();
        fChart.getPlotArea().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseUp(@Nullable MouseEvent e) {
                super.mouseUp(e);
                if (e != null) {
                    IAxis xAxis = fChart.getAxisSet().getXAxis(0);
                    double endTime = xAxis.getDataCoordinate(e.x);
                    System.out.println("selected: " + endTime);
                }
            }
        });

    }

    private static final @Nullable LatencyAnalysis getLatencyAnalysis(ITmfTrace trace) {
        return TmfTraceUtils.getAnalysisModuleOfClass(trace, LatencyAnalysis.class, LatencyAnalysis.ID);
    }


    private void updateDisplay(List<ISegment> data) {
        if (data.isEmpty()) {
            return;
        }
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
        new Thread(new Runnable() {
            @Override
            public void run() {
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
                updateData(Lists.newArrayList(intersection));

            }
        }).start();
    }

    private void updateData(final List<ISegment> data) {
        Collections.sort(data, SegmentComparators.INTERVAL_LENGTH_COMPARATOR);
        Display.getDefault().asyncExec(new Runnable() {
            @Override
            public void run() {
                updateDisplay(NonNullUtils.checkNotNull(data));
            }
        });
        for (ContentChangedListener l : fListeners) {
            l.contentChanged(data);
        }
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
        fCurrentRange = NonNullUtils.checkNotNull(signal.getCurrentRange());
        updateWithRange();
    }

    private void updateWithRange() {
        final LatencyAnalysis module = fAnalysisModule;
        if (module == null) {
            return;
        }

        new Thread(new Runnable() {

            @Override
            public void run() {
                ISegmentStore<ISegment> results = module.getResults();
                if (results != null) {
                    final Iterable<ISegment> intersection = results.getIntersectingElements(fCurrentRange.getStartTime().getValue(), fCurrentRange.getEndTime().getValue());
                    updateData(Lists.newArrayList(intersection));
                }

            }
        }).start();
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
        if (fAnalysisModule != null && fListener != null) {
            fAnalysisModule.removeListener(fListener);
        }
        fDragZoomProvider.deregister();
        fTooltipProvider.deregister();
        super.dispose();
    }

    /**
     * Signal handler for handling of the trace opened signal.
     *
     * @param signal
     *            The trace opened signal {@link TmfTraceOpenedSignal}
     */
    @TmfSignalHandler
    public void traceOpened(TmfTraceOpenedSignal signal) {
        fTrace = signal.getTrace();
        loadTrace(getTrace());
    }

    /**
     * Signal handler for handling of the trace selected signal.
     *
     * @param signal
     *            The trace selected signal {@link TmfTraceSelectedSignal}
     */
    @TmfSignalHandler
    public void traceSelected(TmfTraceSelectedSignal signal) {
        if (fTrace != signal.getTrace()) {
            fTrace = signal.getTrace();
            loadTrace(getTrace());
        }
    }

    /**
     * Signal handler for handling of the trace closed signal.
     *
     * @param signal
     *            The trace closed signal {@link TmfTraceClosedSignal}
     */
    @TmfSignalHandler
    public void traceClosed(TmfTraceClosedSignal signal) {

        if (signal.getTrace() != fTrace) {
            return;
        }

        // Reset the internal data
        fTrace = null;
        clearContent();
    }

    /**
     * A Method to load a trace into the viewer.
     *
     * @param trace
     *            A trace to apply in the viewer
     */
    void loadTrace(@Nullable ITmfTrace trace) {
        fTrace = trace;
        TmfTraceContext ctx = TmfTraceManager.getInstance().getCurrentTraceContext();
        fCurrentRange = ctx.getWindowRange();

        clearContent();
        initializeDataSource();
        updateContent();
    }

    private void initializeDataSource() {
        ITmfTrace trace = getTrace();
        if (trace != null) {
            fAnalysisModule = getLatencyAnalysis(trace);
            final LatencyAnalysis module = fAnalysisModule;
            if (module != null) {
                fListener = new LatencyAnalysisListener() {
                    @Override
                    public void onComplete(LatencyAnalysis activeAnalysis, ISegmentStore<ISegment> data) {
                        updateWithRange();
                    }
                };
                module.addListener(fListener);
                module.schedule();
            }
        }
    }

    private void updateContent() {
        zoom(0, Long.MAX_VALUE);
    }

    /**
     * Clears the view content.
     */
    private void clearContent() {
        final Chart fChart2 = fChart;
        if (!fChart2.isDisposed()) {
            ISeriesSet set = fChart2.getSeriesSet();
            ISeries[] series = set.getSeries();
            for (int i = 0; i < series.length; i++) {
                set.deleteSeries(series[i].getId());
            }
            for (IAxis axis: fChart2.getAxisSet().getAxes()){
                axis.setRange(new Range(0,1));
            }
            fChart2.redraw();
        }
    }

    public void addContentChangedListener(ContentChangedListener contentChangedListener) {
        fListeners.add(contentChangedListener);
    }
}
