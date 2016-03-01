/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.tracecompass.internal.tmf.analysis.xml.ui.views;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.ui.Activator;
import org.eclipse.tracecompass.tmf.analysis.xml.ui.module.TmfXmlAnalysisOutputSource;

/**
 * Class that manages information about a latency view for pattern analysis: its
 * title, the file, etc.
 *
 * @author Jean-Christian Kouame
 *
 */
public class XmlLatencyViewInfo {

    private static final String XML_LATENCY_VIEW_ANALYSIS_ID_PROPERTY = "XmlLatencyAnalysisId"; //$NON-NLS-1$
    private static final String XML_LATENCY_VIEW_LABEL_PROPERTY = "XmlLatencyViewLabel"; //$NON-NLS-1$

    private final String fViewId;
    private @Nullable String fId = null;
    private @Nullable String fLabel = null;

    /**
     * Constructor
     *
     * @param viewId
     *            The ID of the view
     */
    public XmlLatencyViewInfo(String viewId) {
        fViewId = viewId;

        IDialogSettings settings = getPersistentPropertyStore();

        fId = settings.get(XML_LATENCY_VIEW_ANALYSIS_ID_PROPERTY);
        fLabel = settings.get(XML_LATENCY_VIEW_LABEL_PROPERTY);
    }

    /**
     * Set the data for this view and retrieves from it the view ID and the file
     * path of the XML element this view uses.
     *
     * @param data
     *            A string of the form "XML analysis ID" +
     *            {@link TmfXmlAnalysisOutputSource#DATA_SEPARATOR} +
     *            "latency view type"
     */
    public void setViewData(String data) {
        String[] idFile = data.split(TmfXmlAnalysisOutputSource.DATA_SEPARATOR);
        fId = (idFile.length > 0) ? idFile[0] : null;
        fLabel = (idFile.length > 1) ? idFile[1] : null;
        savePersistentData();
    }

    private IDialogSettings getPersistentPropertyStore() {
        IDialogSettings settings = Activator.getDefault().getDialogSettings();
        IDialogSettings section = settings.getSection(fViewId);
        if (section == null) {
            section = settings.addNewSection(fViewId);
            if (section == null) {
                throw new IllegalStateException();
            }
        }
        return section;
    }

    private void savePersistentData() {
        IDialogSettings settings = getPersistentPropertyStore();

        settings.put(XML_LATENCY_VIEW_ANALYSIS_ID_PROPERTY, fId);
        settings.put(XML_LATENCY_VIEW_LABEL_PROPERTY, fLabel);
    }

    /**
     * Get the analysis ID this view is for
     *
     * @return The analysis ID this view
     */
    public String getViewAnalysisId() {
        return fId;
    }
}
