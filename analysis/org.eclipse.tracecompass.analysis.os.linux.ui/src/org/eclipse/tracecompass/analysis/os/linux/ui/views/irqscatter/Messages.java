package org.eclipse.tracecompass.analysis.os.linux.ui.views.irqscatter;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.eclipse.tracecompass.analysis.os.linux.ui.views.irqscatter.messages"; //$NON-NLS-1$
    public static String IRQLatencyScatterGraphViewer_IRQ_legend;
    public static String IRQLatencyScatterGraphViewer_SoftIRQ_legend;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
