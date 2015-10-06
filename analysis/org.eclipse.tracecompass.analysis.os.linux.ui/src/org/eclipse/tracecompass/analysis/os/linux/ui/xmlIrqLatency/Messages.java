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
package org.eclipse.tracecompass.analysis.os.linux.ui.xmlIrqLatency;

import org.eclipse.osgi.util.NLS;

/**
 * Messages used in the XML IRQ average view and viewers
 *
 * @author Jean-Christian Kouame
 *
 */
@SuppressWarnings("javadoc")
public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.eclipse.tracecompass.analysis.os.linux.ui.xmlIrqLatency.messages"; //$NON-NLS-1$
    public static String XMLIrqLatencyViewer_ElapsedTime;
    public static String XMLIrqLatencyViewer_EndTime;
    public static String XMLIrqLatencyViewer_Interrupt;
    public static String XMLIrqLatencyViewer_NonApplicable;
    public static String XMLIrqLatencyViewer_Reset;
    public static String XMLIrqLatencyViewer_StartTime;
    public static String XMLIrqLatencyViewer_TimeToEentry;
    public static String XMLIrqLatencyViewer_TimeToExit;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
