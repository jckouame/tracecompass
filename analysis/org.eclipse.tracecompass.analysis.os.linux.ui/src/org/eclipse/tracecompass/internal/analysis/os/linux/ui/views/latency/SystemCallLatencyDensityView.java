/******************************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.analysis.os.linux.ui.views.latency;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tracecompass.analysis.timing.ui.views.segmentstore.AbstractSegmentStoreTableViewer;
import org.eclipse.tracecompass.common.core.NonNullUtils;

/**
 * Latency Density view
 *
 * @author Matthew Khouzam
 *
 */
public class SystemCallLatencyDensityView extends AbstractLatencyDensityView {
    /** The view's ID */
    public static final String ID = "org.eclipse.tracecompass.analysis.os.linux.views.latency.density"; //$NON-NLS-1$
    /**
     * Constructs a new density view.
     */
    public SystemCallLatencyDensityView() {
        super(ID);
    }

    @Override
    protected AbstractSegmentStoreTableViewer createLatencyTableViewer(Composite parent) {
        TableViewer t = new TableViewer(parent, SWT.FULL_SELECTION | SWT.VIRTUAL);
        return new LatencyTableViewer(t);
    }

    @Override
    protected AbstractDensityViewer createLatencyDensityViewer(Composite parent) {
        return new LatencyDensityViewer(NonNullUtils.checkNotNull(parent));
    }

}
