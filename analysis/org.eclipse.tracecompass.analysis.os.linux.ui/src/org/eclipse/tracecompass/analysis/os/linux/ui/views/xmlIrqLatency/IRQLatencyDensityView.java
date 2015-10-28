package org.eclipse.tracecompass.analysis.os.linux.ui.views.xmlIrqLatency;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tracecompass.analysis.timing.ui.views.segmentstore.AbstractSegmentStoreTableViewer;
import org.eclipse.tracecompass.common.core.NonNullUtils;
import org.eclipse.tracecompass.internal.analysis.os.linux.ui.views.latency.AbstractDensityViewer;
import org.eclipse.tracecompass.internal.analysis.os.linux.ui.views.latency.AbstractLatencyDensityView;

public class IRQLatencyDensityView extends AbstractLatencyDensityView {

    public static final String ID  = "org.eclipse.tracecompass.analysis.os.linux.ui.irqDensityView";
    public IRQLatencyDensityView() {
        super(ID);
    }

    @Override
    protected AbstractSegmentStoreTableViewer createLatencyTableViewer(Composite parent) {
        TableViewer t = new TableViewer(parent, SWT.FULL_SELECTION | SWT.VIRTUAL);
        return new IRQLatencyTableViewer(t);
    }

    @Override
    protected AbstractDensityViewer createLatencyDensityViewer(Composite parent) {
        return new IRQLatencyDensityViewer(NonNullUtils.checkNotNull(parent));
    }
}
