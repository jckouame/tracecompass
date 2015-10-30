/******************************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.analysis.os.linux.ui.views.latency;

import static org.eclipse.tracecompass.common.core.NonNullUtils.nullToEmptyString;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.tracecompass.analysis.timing.core.segmentstore.AbstractSegmentStoreAnalysisModule;
import org.eclipse.tracecompass.analysis.timing.core.segmentstore.IAnalysisProgressListener;
import org.eclipse.tracecompass.common.core.NonNullUtils;
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

public abstract class AbstractDensityViewer extends TmfViewer {

    interface ContentChangedListener {
        void contentChanged(List<ISegment> data);

        void selectionChanged(List<ISegment> data);
    }

    private final Chart fChart;

    private @Nullable IAnalysisProgressListener fListener;

    private @Nullable AbstractSegmentStoreAnalysisModule fAnalysisModule;
    private TmfTimeRange fCurrentRange = TmfTimeRange.NULL_RANGE;
    private MouseDragZoomProvider fDragZoomProvider;
    private MouseSelectionProvider fDragProvider;
    private SimpleTooltipProvider fTooltipProvider;
    private MenuManager fTablePopupMenuManager;

    private @Nullable ITmfTrace fTrace;
    private List<ContentChangedListener> fListeners;

    /**
     * Constructs a new density viewer.
     *
     * @param parent
     *            the parent of the viewer
     */
    public AbstractDensityViewer(Composite parent) {
        super(parent);
        fListeners = new ArrayList<>();
        fChart = new Chart(parent, SWT.NONE);
        fChart.getLegend().setVisible(false);
        fChart.getTitle().setVisible(false);
        fChart.getAxisSet().getXAxis(0).getTitle().setText(nullToEmptyString(Messages.LatencyDensityView_TimeAxisLabel));
        fChart.getAxisSet().getYAxis(0).getTitle().setText(nullToEmptyString(Messages.LatencyDensityView_CountAxisLabel));
        fChart.getAxisSet().getXAxis(0).getGrid().setStyle(LineStyle.NONE);
        fChart.getAxisSet().getYAxis(0).getGrid().setStyle(LineStyle.NONE);

        fDragZoomProvider = new MouseDragZoomProvider(this);
        fDragZoomProvider.register();

        fDragProvider = new MouseSelectionProvider(this);
        fDragProvider.register();
        fTooltipProvider = new SimpleTooltipProvider(this);

        fTablePopupMenuManager = new MenuManager();
        fTablePopupMenuManager.setRemoveAllWhenShown(true);

        fTablePopupMenuManager.addMenuListener(new IMenuListener() {
            @Override
            public void menuAboutToShow(final @Nullable IMenuManager manager) {
                if (manager != null) {
                    appendToTablePopupMenu(manager);
                }
            }
        });

        Menu tablePopup = fTablePopupMenuManager.createContextMenu(getChart());
        getChart().setMenu(tablePopup);
    }

    /**
     * Add items to the menu manager
     *
     * @param manager
     *            The menu manager
     */
    protected void appendToTablePopupMenu(IMenuManager manager) {
    }

    /**
     * Returns the segment store analysis module
     *
     * @param trace
     *            The trace to consider
     * @return the analysis module
     */
    protected @Nullable abstract AbstractSegmentStoreAnalysisModule getSegmentStoreAnalysisModule(ITmfTrace trace);

    @Nullable
    protected static ITmfTrace getTrace() {
        return TmfTraceManager.getInstance().getActiveTrace();
    }

    protected void updateDisplay(List<ISegment> data) {
        if (data.isEmpty()) {
            return;
        }
        IBarSeries series = (IBarSeries) getChart().getSeriesSet().createSeries(SeriesType.BAR, Messages.LatencyDensityViewer_SeriesLabel);
        series.setVisible(true);
        series.setBarPadding(0);
        int barWidth = 4;
        final int width = getChart().getPlotArea().getBounds().width / barWidth;
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
        getChart().getAxisSet().getXAxis(0).setRange(new Range(minX, maxLength));
        getChart().getAxisSet().getYAxis(0).setRange(new Range(1.0, maxY));
        getChart().getAxisSet().getYAxis(0).enableLogScale(true);
        getChart().redraw();
    }

    @Override
    public Chart getControl() {
        return getChart();
    }

    public void select(Range durationRange) {
        computeDataAsync(fCurrentRange, durationRange).thenAccept((data) -> {
            for (ContentChangedListener l : fListeners) {
                l.selectionChanged(data);
            }
        });
    }

    public void zoom(Range durationRange) {
        computeDataAsync(fCurrentRange, durationRange).thenAccept((data) -> applyData(data));
    }

    private void updateWithRange(final TmfTimeRange range) {
        computeDataAsync(range, new Range(Double.MIN_VALUE, Double.MAX_VALUE)).thenAccept((data) -> applyData(data));
    }

    private CompletableFuture<List<ISegment>> computeDataAsync(final TmfTimeRange range, final Range durationRange) {
        return CompletableFuture.supplyAsync(() -> computeData(range, durationRange));
    }

    private @Nullable ArrayList<ISegment> computeData(final TmfTimeRange range, final Range durationRange) {
        AbstractSegmentStoreAnalysisModule analysisModule = fAnalysisModule;
        if (analysisModule == null) {
            return null;
        }
        ISegmentStore<ISegment> results = analysisModule.getResults();
        if (results == null) {
            return null;
        }

        Iterable<ISegment> intersectingElements = results.getIntersectingElements(range.getStartTime().getValue(), range.getEndTime().getValue());

        if (durationRange.lower > Double.MIN_VALUE || durationRange.upper < Double.MAX_VALUE) {
            Predicate<? super ISegment> predicate = new Predicate<ISegment>() {
                @Override
                public boolean apply(@Nullable ISegment input) {

                    return input != null && input.getLength() >= durationRange.lower && input.getLength() <= durationRange.upper;
                }
            };
            final UnmodifiableIterator<ISegment> intersection = Iterators.<ISegment> filter(intersectingElements.iterator(), predicate);
            return Lists.newArrayList(intersection);
        }

        return Lists.newArrayList(intersectingElements);
    }

    private void applyData(final List<ISegment> data) {
        Collections.sort(data, SegmentComparators.INTERVAL_LENGTH_COMPARATOR);
        Display.getDefault().asyncExec(() -> {
            updateDisplay(NonNullUtils.checkNotNull(data));
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
            return;
        }
        fAnalysisModule = getSegmentStoreAnalysisModule(trace);
        final TmfTimeRange currentRange = NonNullUtils.checkNotNull(signal.getCurrentRange());
        updateWithRange(currentRange);
        fCurrentRange = currentRange;
    }

    @Override
    public void refresh() {
        getChart().redraw();
    }

    @Override
    public void dispose() {
        if (fAnalysisModule != null && fListener != null) {
            fAnalysisModule.removeListener(fListener);
        }
        fDragZoomProvider.deregister();
        getTooltipProvider().deregister();
        fDragProvider.deregister();
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

        fTrace = null;
        clearContent();
    }

    /**
     * A Method to load a trace into the viewer.
     *
     * @param trace
     *            A trace to apply in the viewer
     */
    protected void loadTrace(@Nullable ITmfTrace trace) {
        clearContent();

        fTrace = trace;
        TmfTraceContext ctx = TmfTraceManager.getInstance().getCurrentTraceContext();
        TmfTimeRange windowRange = ctx.getWindowRange();
        fCurrentRange = windowRange;

        if (trace != null) {
            fAnalysisModule = getSegmentStoreAnalysisModule(trace);
            final AbstractSegmentStoreAnalysisModule module = fAnalysisModule;
            if (module != null) {
                fListener = (activeAnalysis, data) -> updateWithRange(windowRange);
                module.addListener(fListener);
                module.schedule();
            }
        }
        zoom(new Range(0, Long.MAX_VALUE));
    }

    /**
     * Clears the view content.
     */
    private void clearContent() {
        final Chart chart = getChart();
        if (!chart.isDisposed()) {
            ISeriesSet set = chart.getSeriesSet();
            ISeries[] series = set.getSeries();
            for (int i = 0; i < series.length; i++) {
                set.deleteSeries(series[i].getId());
            }
            for (IAxis axis : chart.getAxisSet().getAxes()) {
                axis.setRange(new Range(0, 1));
            }
            chart.redraw();
        }
    }

    public void addContentChangedListener(ContentChangedListener contentChangedListener) {
        fListeners.add(contentChangedListener);
    }

    public void removeContentChangedListener(ContentChangedListener contentChangedListener) {
        fListeners.remove(contentChangedListener);
    }

    protected Chart getChart() {
        return fChart;
    }

    private SimpleTooltipProvider getTooltipProvider() {
        return fTooltipProvider;
    }

    protected void setTooltipProvider(SimpleTooltipProvider tooltipProvider) {
        if (fTooltipProvider != null) {
            fTooltipProvider.deregister();
        }
        fTooltipProvider = tooltipProvider;
        tooltipProvider.register();
    }
}