/*******************************************************************************
 * Copyright (c) 2015 Ecole Polytechnique de Montreal, Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Jean-Christian Kouame - Initial API and implementation
 ******************************************************************************/
package org.eclipse.tracecompass.tmf.analysis.xml.core.model;

import java.util.Map.Entry;

import org.eclipse.tracecompass.statesystem.core.statevalue.ITmfStateValue;
import org.eclipse.tracecompass.statesystem.core.statevalue.TmfStateValue;
import org.eclipse.tracecompass.tmf.analysis.xml.core.model.readwrite.TmfXmlReadWriteScenarioStatus;
import org.eclipse.tracecompass.tmf.analysis.xml.core.stateprovider.XmlPatternStateProvider;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.event.ITmfEventField;

/**
 * This action will set the value of the special fields in the state system
 * based on their values in the event, if the fields exist.
 *
 * @since 2.0
 *
 */
public class SpecialFieldsSaverAction implements ISingleAction {

    private final XmlPatternStateProvider fParent;

    /**
     * Constructor
     *
     * @param parent
     *            The state system container this action belongs to
     */
    public SpecialFieldsSaverAction(XmlPatternStateProvider parent) {
        fParent = parent;
    }

    @Override
    public void execute(ITmfEvent event, String... args) {
        for (Entry<String, String> entry : fParent.getDefinedFields().entrySet()) {
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
                TmfXmlReadWriteScenarioStatus.saveSpecialFields(event, fParent, name, stateValue, args);
            }
        }
    }
}
