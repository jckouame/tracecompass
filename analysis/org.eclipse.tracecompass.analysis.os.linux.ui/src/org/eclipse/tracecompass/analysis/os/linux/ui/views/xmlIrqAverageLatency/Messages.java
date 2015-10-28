/*******************************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Jean-christian Kouame - Initial API and implementation
 *******************************************************************************/
package org.eclipse.tracecompass.analysis.os.linux.ui.views.xmlIrqAverageLatency;

import org.eclipse.osgi.util.NLS;

/**
 * Messages used in the XML IRQ average view and viewers
 *
 * @author Jean-Christian Kouame
 *
 */
@SuppressWarnings("javadoc")
public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.eclipse.tracecompass.analysis.os.linux.ui.views.xmlIrqAverageLatency.messages"; //$NON-NLS-1$
    public static String XMLIrqAverageLatencyViewer_Average;
    public static String XMLIrqAverageLatencyViewer_Count;
    public static String XMLIrqAverageLatencyViewer_GoToMaxIRQ;
    public static String XMLIrqAverageLatencyViewer_GoToMinIRQ;
    public static String XMLIrqAverageLatencyViewer_Interrupt;
    public static String XMLIrqAverageLatencyViewer_IRQ;
    public static String XMLIrqAverageLatencyViewer_Max;
    public static String XMLIrqAverageLatencyViewer_Min;
    public static String XMLIrqAverageLatencyViewer_Reset;
    public static String XMLIrqAverageLatencyViewer_SOFTIRQ;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
