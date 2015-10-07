package org.eclipse.tracecompass.internal.analysis.os.linux.ui.views.latency;

import static org.eclipse.tracecompass.common.core.NonNullUtils.nullToEmptyString;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tracecompass.common.core.NonNullUtils;
import org.eclipse.tracecompass.tmf.ui.views.TmfView;
import org.eclipse.ui.IActionBars;

/**
 * Latency Density view
 *
 * @author Matthew Khouzam
 *
 */
public class LatencyDensityView extends TmfView {
    /** The view's ID */
    public static final String ID = "org.eclipse.tracecompass.analysis.os.linux.views.latency.density"; //$NON-NLS-1$
    private @Nullable LatencyDensityViewer fChart;

    public LatencyDensityView() {
        super(ID);
    }

    @Override
    public void createPartControl(@Nullable Composite parent) {
        super.createPartControl(parent);
        fChart = new LatencyDensityViewer(NonNullUtils.checkNotNull(parent),
                nullToEmptyString("Latency Density"),
                nullToEmptyString("Duration (ns)"),
                nullToEmptyString("Count"));
        Action zoomOut = new Action() {
            @Override
            public void run() {
                fChart.zoom(0, Long.MAX_VALUE);
            }
        };
        IActionBars actionBars = getViewSite().getActionBars();
        IToolBarManager toolBar = actionBars.getToolBarManager();
        toolBar.add(zoomOut);
    }

    @Override
    public void setFocus() {
        final LatencyDensityViewer chart = fChart;
        if (chart != null) {
            chart.getControl().setFocus();
        }
    }

}
