/*******************************************************************************
 * Copyright (c) 2016 Ecole Polytechnique de Montreal, Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.tracecompass.tmf.analysis.xml.core.model;

import java.util.Map.Entry;

import org.eclipse.tracecompass.tmf.analysis.xml.core.stateprovider.XmlPatternStateProvider;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;

/**
 * This action will reset the value of each stored values to a null state value
 * in the state system
 *
 * @since 2.0
 *
 */
public class StoredFieldsClearerAction implements ISingleAction {

    private final XmlPatternStateProvider fParent;

    /**
     * @param parent
     *            The state system container this action belongs to
     */
    public StoredFieldsClearerAction(XmlPatternStateProvider parent) {
        fParent = parent;
    }

    @Override
    public void execute(ITmfEvent event, String scenarioName, String activeState) {
        for (Entry<String, String> entry : fParent.getStoredFields().entrySet()) {
            TmfXmlReadWriteScenarioStatus.clearStoredFields(event, fParent, entry.getValue(), scenarioName);
        }
    }
}
