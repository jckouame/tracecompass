/*******************************************************************************
 * Copyright (c) 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.tracecompass.tmf.analysis.xml.core.stateprovider;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.analysis.timing.core.segmentstore.ISegmentListener;
import org.eclipse.tracecompass.tmf.core.statesystem.ITmfStateProvider;
import org.eclipse.tracecompass.tmf.core.statesystem.TmfStateSystemAnalysisModule;

/**
 * State system analysis for patter matching analysis described in XML. This
 * module will parse the XML description of the analyses and execute it against
 * the trace and will proceed all required action
 *
 * @author Jean-Christian Kouame
 * @since 2.0
 *
 */
public class XmlPatternStateSystemModule extends TmfStateSystemAnalysisModule {

    private @Nullable IPath fXmlFile;
    private ISegmentListener fListener;

    /**
     * Constructor
     *
     * @param listener
     *            Listener for segments that will be created
     */
    public XmlPatternStateSystemModule(ISegmentListener listener) {
        fListener = listener;
    }

    @Override
    protected @NonNull ITmfStateProvider createStateProvider() {
        String id = getId();
        return new XmlPatternStateProvider(checkNotNull(getTrace()), id, fXmlFile, fListener);
    }

    /**
     * Sets the file path of the XML file containing the state provider
     *
     * @param file
     *            The full path to the XML file
     */
    public void setXmlFile(IPath file) {
        fXmlFile = file;
    }
}
