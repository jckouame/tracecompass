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

import org.eclipse.tracecompass.tmf.analysis.xml.core.model.readwrite.TmfXmlReadWriteScenarioStatus;
import org.eclipse.tracecompass.tmf.analysis.xml.core.stateprovider.XmlPatternStateProvider;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;

/**
 * This action will reset the value of the special fields to a null state value
 * in the state system
 *
 * @since 2.0
 *
 */
public class SpecialFieldsClearerAction implements ISingleAction {

    private final XmlPatternStateProvider fParent;

    /**
     * @param parent
     *            The state system container this action belongs to
     */
    public SpecialFieldsClearerAction(XmlPatternStateProvider parent) {
        fParent = parent;
    }

    @Override
    public void execute(ITmfEvent event, String... args) {
        for (Entry<String, String> entry : fParent.getDefinedFields().entrySet()) {
            TmfXmlReadWriteScenarioStatus.clearSpecialFields(event, fParent, entry.getValue(), args);
        }
    }
}
