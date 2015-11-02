package org.eclipse.tracecompass.analysis.os.linux.ui.views.irqscatter;

import java.util.Collection;
import java.util.Iterator;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tracecompass.analysis.os.linux.core.latency.irq.IRQ;
import org.eclipse.tracecompass.analysis.os.linux.core.latency.irq.IRQLatencyAnalysis;
import org.eclipse.tracecompass.analysis.os.linux.core.latency.irq.XmlIrqUtils.TYPE;
import org.eclipse.tracecompass.analysis.timing.core.segmentstore.AbstractSegmentStoreAnalysisModule;
import org.eclipse.tracecompass.analysis.timing.ui.views.segmentstore.AbstractSegmentStoreScatterGraphViewer;
import org.eclipse.tracecompass.segmentstore.core.ISegment;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;
import org.eclipse.tracecompass.tmf.ui.viewers.xycharts.TmfChartTimeStampFormat;
import org.swtchart.Chart;
import org.swtchart.IAxis;
import org.swtchart.IAxisTick;
import org.swtchart.ILineSeries;
import org.swtchart.Range;

/**
 * Displays the IRQ latency analysis in a scatter graph
 * @author Jean-Christian Kouame
 *
 */
public class IRQLatencyScatterGraphViewer extends AbstractSegmentStoreScatterGraphViewer {

    private boolean fShowIRQ = true;
    private boolean fShowSoftIRQ = true;

    /**
     * Constructor
     *
     * @param parent
     *            parent composite
     * @param title
     *            name of the graph
     * @param xLabel
     *            name of the x axis
     * @param yLabel
     *            name of the y axis
     */
    public IRQLatencyScatterGraphViewer(Composite parent, String title, String xLabel, String yLabel) {
        super(parent, title, xLabel, yLabel);
    }

    @Override
    protected @Nullable AbstractSegmentStoreAnalysisModule getSegmentStoreAnalysisModule(@NonNull ITmfTrace trace) {
        return TmfTraceUtils.getAnalysisModuleOfClass(trace, IRQLatencyAnalysis.class, IRQLatencyAnalysis.ID);
    }

    @Override
    protected void updateData(final long start, final long end, int nb, @Nullable IProgressMonitor monitor) {
        // Third parameter is not used by implementation
        // Determine data that needs to be visible
        Collection<ISegment> data = fDisplayData;

        final int dataSize = (nb == 0) ? data.size() : nb;
        if (dataSize == 0 || end == start) {
            return;
        }

        final double[] xSeries = new double[dataSize];
        final double[] xSeries1 = new double[dataSize];
        final double[] ySeries1 = new double[dataSize];
        final double[] xSeries2 = new double[dataSize];
        final double[] ySeries2 = new double[dataSize];
        // For each visible latency, add start time to x value and duration
        // for y value
        Iterator<ISegment> modelIter = data.iterator();
        long maxTempY = 1;
        int irqCounter = 0;
        int softirqCounter = 0;
        for (int i = 0; i < dataSize; i++) {
            if (modelIter.hasNext()) {
                ISegment segment = modelIter.next();
                xSeries[i] = segment.getStart() - start;
                maxTempY = Math.max(maxTempY, segment.getLength());
                if (segment instanceof IRQ) {
                    IRQ irq =  (IRQ)segment;
                    if (irq.type == TYPE.IRQ) {
                        xSeries1[irqCounter] = segment.getStart() - start;
                        ySeries1[irqCounter] = segment.getLength();
                        irqCounter++;
                    } else {
                        xSeries2[softirqCounter] = segment.getStart() - start;
                        ySeries2[softirqCounter] = segment.getLength();
                        softirqCounter++;
                    }
                }
            }
        }
        final long maxY1 = maxTempY;
        setXAxis(xSeries);
        final Chart swtChart = getSwtChart();
        if (swtChart.isDisposed() || xSeries.length < 1) {
            return;
        }
        swtChart.updateLayout();
        setSeries(Messages.IRQLatencyScatterGraphViewer_IRQ_legend, ySeries1);
        final TmfChartTimeStampFormat tmfChartTimeStampFormat = new TmfChartTimeStampFormat(getTimeOffset());
        ILineSeries series1 = (ILineSeries) swtChart.getSeriesSet().getSeries(Messages.IRQLatencyScatterGraphViewer_IRQ_legend);
        if (series1 == null) {
            series1 = addSeries(Messages.IRQLatencyScatterGraphViewer_IRQ_legend);
        }
        series1.setXSeries(xSeries1);
        /* Find the minimal and maximum values in this series */
        series1.setYSeries(ySeries1);

        setSeries(Messages.IRQLatencyScatterGraphViewer_SoftIRQ_legend, ySeries2);
        ILineSeries series2 = (ILineSeries) swtChart.getSeriesSet().getSeries(Messages.IRQLatencyScatterGraphViewer_SoftIRQ_legend);
        if (series2 == null) {
            series2 = addSeries(Messages.IRQLatencyScatterGraphViewer_SoftIRQ_legend);
        }
        series2.setXSeries(xSeries2);
        /* Find the minimal and maximum values in this series */
        series2.setYSeries(ySeries2);


        series1.setVisible(fShowIRQ);
        series2.setVisible(fShowSoftIRQ);
        final IAxis xAxis = swtChart.getAxisSet().getXAxis(0);
        IAxisTick xTick = xAxis.getTick();
        xTick.setFormat(tmfChartTimeStampFormat);
        xAxis.setRange(new Range(0.0, end - start));
        if (maxY1 > 0.0) {
            swtChart.getAxisSet().getYAxis(0).setRange(new Range(0.0, maxY1));
        }
        swtChart.getLegend().setVisible(true);
        swtChart.redraw();

        ajustTimeAlignment(swtChart);
    }

    @Override
    protected void appendToTablePopupMenu(IMenuManager manager) {
        if (getTrace() != null) {
            IAction IRQAction;
            if (fShowIRQ == true) {
                IRQAction = new Action("Hide IRQs") {
                    @Override
                    public void run() {
                        fShowIRQ = !fShowIRQ;
                        getSwtChart().getSeriesSet().getSeries(Messages.IRQLatencyScatterGraphViewer_IRQ_legend).setVisible(false);
                        getSwtChart().redraw();
                    }
                };
            } else {
                IRQAction = new Action("Show IRQs") {
                    @Override
                    public void run() {
                        fShowIRQ = !fShowIRQ;
                        getSwtChart().getSeriesSet().getSeries(Messages.IRQLatencyScatterGraphViewer_IRQ_legend).setVisible(true);
                        getSwtChart().redraw();
                    }
                };
            }

            IAction softIRQAction;
            if (fShowSoftIRQ == true) {
                softIRQAction = new Action("Hide SoftIRQs") {
                    @Override
                    public void run() {
                        fShowSoftIRQ = !fShowSoftIRQ;
                        getSwtChart().getSeriesSet().getSeries(Messages.IRQLatencyScatterGraphViewer_SoftIRQ_legend).setVisible(false);
                        getSwtChart().redraw();
                    }
                };
            } else {
                softIRQAction = new Action("Show softIRQs") {
                    @Override
                    public void run() {
                        fShowSoftIRQ = !fShowSoftIRQ;
                        getSwtChart().getSeriesSet().getSeries(Messages.IRQLatencyScatterGraphViewer_SoftIRQ_legend).setVisible(true);
                        getSwtChart().redraw();
                    }
                };
            }
            manager.add(IRQAction);
            manager.add(softIRQAction);
        }
    }
}
