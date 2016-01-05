package org.eclipse.tracecompass.tmf.ui.views.xmlSegment;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.tracecompass.analysis.timing.ui.views.segmentstore.AbstractSegmentStoreTableView;
import org.eclipse.tracecompass.analysis.timing.ui.views.segmentstore.AbstractSegmentStoreTableViewer;

public class XMLSegmentTableView extends AbstractSegmentStoreTableView {

    public static final String ID = "org.eclipse.tracecompass.tmf.ui.views.xmlSegmentTable"; //$NON-NLS-1$

    @Override
    protected @NonNull AbstractSegmentStoreTableViewer getSegmentStoreViewer(@NonNull TableViewer tableViewer) {
        return new XMLSegmentTableViewer(tableViewer);
    }

}
