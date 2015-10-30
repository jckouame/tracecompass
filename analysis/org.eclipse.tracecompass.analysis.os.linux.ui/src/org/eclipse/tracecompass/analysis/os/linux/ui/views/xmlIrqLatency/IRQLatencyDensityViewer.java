package org.eclipse.tracecompass.analysis.os.linux.ui.views.xmlIrqLatency;

import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.tracecompass.analysis.os.linux.core.latency.irq.IRQ;
import org.eclipse.tracecompass.analysis.os.linux.core.latency.irq.IRQLatencyAnalysis;
import org.eclipse.tracecompass.analysis.os.linux.core.latency.irq.XmlIrqUtils.TYPE;
import org.eclipse.tracecompass.analysis.timing.core.segmentstore.AbstractSegmentStoreAnalysisModule;
import org.eclipse.tracecompass.internal.analysis.os.linux.ui.views.latency.AbstractDensityViewer;
import org.eclipse.tracecompass.internal.analysis.os.linux.ui.views.latency.SimpleTooltipProvider;
import org.eclipse.tracecompass.segmentstore.core.ISegment;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;
import org.eclipse.tracecompass.tmf.ui.viewers.xycharts.linecharts.TmfCommonXLineChartViewer;
import org.swtchart.IAxis;
import org.swtchart.ILineSeries;
import org.swtchart.ILineSeries.PlotSymbolType;
import org.swtchart.ISeries;
import org.swtchart.ISeries.SeriesType;
import org.swtchart.Range;

/**
 * @author Jean-Christian Kouame
 *
 */
public class IRQLatencyDensityViewer extends AbstractDensityViewer {

    private boolean fShowIRQ = true;
    private boolean fShowSoftIRQ = true;
    private ISeries fIRQSeries;
    private ISeries fSoftIRQSeries;
    private double[] fYOrigSeriesTots;

    /**
     * Constructs a new density viewer.
     *
     * @param parent
     *            the parent of the viewer
     */
    public IRQLatencyDensityViewer(Composite parent) {
        super(parent);
        setTooltipProvider(new SimpleTooltipProvider(this){
            private int HIGHLIGHT_RADIUS = 5;

            @Override
            protected String setTooltipText(MouseEvent e, ISeries series) {
                double smallestDistance = Double.MAX_VALUE;
                IAxis xAxis = getChart().getAxisSet().getXAxis(0);
                double[] xS = fIRQSeries.getXSeries();
                int tooltipIndex = Integer.MIN_VALUE;
             // go over all data points
                for (int i = 0; i < xS.length; i++) {
                    int xs = Math.abs(xAxis.getPixelCoordinate(xS[i]) - e.x);
                    if ((xs < smallestDistance) && (xs < HIGHLIGHT_RADIUS )) {
                        smallestDistance = xs;
                        tooltipIndex = i;
                    }
                }
                double delta = 0;
                if (xS.length >= 2) {
                    delta = (xS[1] - xS[0]) / 2;
                }
                long x1 = Math.round(Math.max(0, xS[tooltipIndex] - delta));
                long x2 = Math.round(Math.max(0, xS[tooltipIndex] + delta));
                long y = Math.round(fYOrigSeriesTots[tooltipIndex] - 1);
                long yIRQ = Math.round(fIRQSeries.getYSeries()[tooltipIndex] - 1);
                long ySoftIRQ = Math.round(fSoftIRQSeries.getYSeries()[tooltipIndex] - 1);

                StringBuffer buffer = new StringBuffer();
                buffer.append("Duration: ["); //$NON-NLS-1$
                buffer.append(x1);
                buffer.append(", "); //$NON-NLS-1$
                buffer.append(x2);
                buffer.append("] ns"); //$NON-NLS-1$
                if (fShowIRQ) {
                    buffer.append("\n"); //$NON-NLS-1$
                    buffer.append("IRQ Count: "); //$NON-NLS-1$
                    buffer.append(yIRQ);
                }
                if (fShowSoftIRQ) {
                    buffer.append("\n"); //$NON-NLS-1$
                    buffer.append("SoftIRQ Count: "); //$NON-NLS-1$
                    buffer.append(ySoftIRQ);
                }
                if (fShowIRQ && fShowSoftIRQ) {
                    buffer.append("\n"); //$NON-NLS-1$
                    buffer.append("Total Count: "); //$NON-NLS-1$
                    buffer.append(y);
                }
                return buffer.toString();
            }
        });
    }

    @Override
    protected void appendToTablePopupMenu(IMenuManager manager) {
        if (getTrace() != null) {
            IAction IRQAction;
            if (fShowIRQ == true) {
                IRQAction = new Action(Messages.IRQLatencyDensityViewer_Hide_IRQs) {
                    @Override
                    public void run() {
                        fShowIRQ = !fShowIRQ;
                        getChart().getSeriesSet().getSeries(Messages.IRQLatencyDensityViewer_IRQ_Legend).setVisible(false);
                        getChart().redraw();
                    }
                };
            } else {
                IRQAction = new Action(Messages.IRQLatencyDensityViewer_Show_IRQs) {
                    @Override
                    public void run() {
                        fShowIRQ = !fShowIRQ;
                        getChart().getSeriesSet().getSeries("IRQ").setVisible(true); //$NON-NLS-1$
                        getChart().redraw();
                    }
                };
            }

            IAction softIRQAction;
            if (fShowSoftIRQ == true) {
                softIRQAction = new Action(Messages.IRQLatencyDensityViewer_Hide_SoftIRQs) {
                    @Override
                    public void run() {
                        fShowSoftIRQ = !fShowSoftIRQ;
                        getChart().getSeriesSet().getSeries(Messages.IRQLatencyDensityViewer_SoftIRQ_Legend).setVisible(false);
                        getChart().redraw();
                    }
                };
            } else {
                softIRQAction = new Action(Messages.IRQLatencyDensityViewer_Show_SoftIRQ) {
                    @Override
                    public void run() {
                        fShowSoftIRQ = !fShowSoftIRQ;
                        getChart().getSeriesSet().getSeries("softIRQ").setVisible(true); //$NON-NLS-1$
                        getChart().redraw();
                    }
                };
            }
            manager.add(IRQAction);
            manager.add(softIRQAction);
        }
    }

    @Override
    protected @Nullable AbstractSegmentStoreAnalysisModule getSegmentStoreAnalysisModule(ITmfTrace trace) {
        return TmfTraceUtils.getAnalysisModuleOfClass(trace, IRQLatencyAnalysis.class, IRQLatencyAnalysis.ID);
    }

    @Override
    protected void updateDisplay(List<ISegment> data) {
        if (data.isEmpty()) {
            return;
        }

        fIRQSeries = getChart().getSeriesSet().createSeries(SeriesType.LINE, "IRQ"); //$NON-NLS-1$
        fSoftIRQSeries = getChart().getSeriesSet().createSeries(SeriesType.LINE, "softIRQ"); //$NON-NLS-1$
        addSerie(((ILineSeries)fIRQSeries), 0, fShowIRQ);
        addSerie(((ILineSeries)fSoftIRQSeries), 1, fShowSoftIRQ);
        int barWidth = 4;
        final int width = getChart().getPlotArea().getBounds().width / barWidth;
        double[] xOrigSeries = new double[width];
        double[] yOrigSeries = new double[width];
        double[] yOrigSeries1 = new double[width];
        fYOrigSeriesTots = new double[width];
        Arrays.fill(yOrigSeries, 1.0);
        Arrays.fill(yOrigSeries1, 1.0);
        long maxLength = data.get(data.size() - 1).getLength();
        double maxFactor = 1.0 / (maxLength + 1.0);
        long minX = Long.MAX_VALUE;
        for (ISegment segment : data) {
            double xBox = segment.getLength() * maxFactor * width;
            if (segment instanceof IRQ) {
                IRQ irq = (IRQ) segment;
                if (irq.type == TYPE.IRQ) {
                    yOrigSeries[(int) xBox]++;
                } else if (irq.type == TYPE.SOFTIRQ) {
                    yOrigSeries1[(int) xBox]++;
                }
            }
            minX = Math.min(minX, segment.getLength());
        }
        for (int i = 0; i < width; i++) {
            xOrigSeries[i] = i * maxLength / width;
            fYOrigSeriesTots[i] = yOrigSeries1[i] + yOrigSeries[i] - 1;
        }
        double maxY = Double.MIN_VALUE;
        for (int i = 0; i < width; i++) {
            maxY = Math.max(maxY, Math.max(yOrigSeries1[i], yOrigSeries[i]));
        }
        if (minX == maxLength) {
            maxLength++;
            minX--;
        }
        fIRQSeries.setYSeries(yOrigSeries);
        fIRQSeries.setXSeries(xOrigSeries);
        fSoftIRQSeries.setYSeries(yOrigSeries1);
        fSoftIRQSeries.setXSeries(xOrigSeries);
        getChart().getAxisSet().getXAxis(0).setRange(new Range(minX, maxLength));
        getChart().getAxisSet().getYAxis(0).setRange(new Range(1.0, maxY));
        getChart().getAxisSet().getYAxis(0).enableLogScale(true);
        getChart().getLegend().setVisible(true);
        getChart().redraw();
    }

    private static void addSerie (ILineSeries serie, int index, boolean visible) {
        serie.setVisible(visible);
        serie.setLineColor(Display.getDefault().getSystemColor(TmfCommonXLineChartViewer.LINE_COLORS[index % TmfCommonXLineChartViewer.LINE_COLORS.length]));
        serie.setSymbolType(PlotSymbolType.NONE);
        serie.enableArea(true);
    }
}
