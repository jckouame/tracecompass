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

import org.eclipse.tracecompass.statesystem.core.statevalue.ITmfStateValue;
import org.eclipse.tracecompass.statesystem.core.statevalue.TmfStateValue;
import org.eclipse.tracecompass.tmf.analysis.xml.core.stateprovider.XmlPatternStateProvider;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.event.ITmfEventField;

/**
 * This action will update the value of the stored values in the state system
 * based on the current event data.
 *
 * @author Jean-Christian Kouame
 * @since 2.0
 *
 */
public class StoredFieldsSaverAction implements ISingleAction {

    private final XmlPatternStateProvider fParent;

    /**
     * Constructor
     *
     * @param parent
     *            The state system container this action belongs to
     */
    public StoredFieldsSaverAction(XmlPatternStateProvider parent) {
        fParent = parent;
    }

    @Override
    public void execute(ITmfEvent event, String scenarioName, String activeState) {
        for (Entry<String, String> entry : fParent.getStoredFields().entrySet()) {
            ITmfEventField eventField = event.getContent().getField(entry.getKey());
            ITmfStateValue stateValue = null;
            if (eventField != null) {
                Object field = eventField.getValue();
                if (field instanceof String) {
                    stateValue = TmfStateValue.newValueString((String) field);
                } else if (field instanceof Long) {
                    stateValue = TmfStateValue.newValueLong(((Long) field).longValue());
                } else if (field instanceof Integer) {
                    stateValue = TmfStateValue.newValueInt(((Integer) field).intValue());
                } else if (field instanceof Double) {
                    stateValue = TmfStateValue.newValueDouble(((Double) field).doubleValue());
                }
                final String name = entry.getValue();
                if (stateValue == null) {
                    throw new IllegalArgumentException();
                }
                TmfXmlReadWriteScenarioStatus.saveStoredFields(event, fParent, name, stateValue, scenarioName);
            }
        }
    }
}
