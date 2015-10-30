package org.eclipse.tracecompass.analysis.os.linux.ui.views.xmlIrqLatency;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tracecompass.analysis.timing.ui.views.segmentstore.AbstractSegmentStoreTableViewer;
import org.eclipse.tracecompass.analysis.timing.ui.views.segmentstore.density.AbstractSegmentStoreDensityView;
import org.eclipse.tracecompass.analysis.timing.ui.views.segmentstore.density.AbstractSegmentStoreDensityViewer;
import org.eclipse.tracecompass.common.core.NonNullUtils;

public class IRQLatencyDensityView extends AbstractSegmentStoreDensityView {

    public static final String ID  = "org.eclipse.tracecompass.analysis.os.linux.ui.irqDensityView";
    public IRQLatencyDensityView() {
        super(ID);
    }

    @Override
    protected AbstractSegmentStoreTableViewer createSegmentStoreTableViewer(Composite parent) {
        TableViewer t = new TableViewer(parent, SWT.FULL_SELECTION | SWT.VIRTUAL);
        return new IRQLatencyTableViewer(t);
    }

    @Override
    protected AbstractSegmentStoreDensityViewer createSegmentStoreDensityViewer(Composite parent) {
        return new IRQLatencyDensityViewer(NonNullUtils.checkNotNull(parent));
    }
}
