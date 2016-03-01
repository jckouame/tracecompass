/*******************************************************************************
 * Copyright (c) 2016 Ecole Polytechnique de Montreal, Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.tracecompass.tmf.analysis.xml.core.model;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.tmf.analysis.xml.core.stateprovider.TmfXmlStrings;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.w3c.dom.Element;

/**
 * Basic implementation of a transition in the XML file
 *
 * @author Jean-Christian Kouame
 * @since 2.0
 *
 */
public class TmfXmlBasicTransition {

    private static final String OTHER_STRING = TmfXmlStrings.CONSTANT_PREFIX + TmfXmlStrings.OTHER;
    private String[] fCond;
    private String fAcceptedEvents[];

    /**
     * Constructor
     *
     * @param element
     *            the XML basic transition element
     */
    public TmfXmlBasicTransition(Element element) {
        final String[] eventNames = (element.getAttribute(TmfXmlStrings.EVENT)).split(TmfXmlStrings.OR_SEPARATOR);
        fAcceptedEvents = eventNames;
        final @NonNull String conditions = element.getAttribute(TmfXmlStrings.COND);
        fCond = conditions.isEmpty() ? new String[]{} : conditions.split(TmfXmlStrings.AND_SEPARATOR);
    }

    /**
     * Validate the transition with the current event
     *
     * @param event
     *            The input event
     * @param scenarioName
     *            The scenario name
     * @param activeState
     *            The current state
     * @param tests
     *            The map of test in the XML file]
     * @return true if the transition is validate false if not
     */
    public boolean validate(ITmfEvent event, String scenarioName, String activeState, Map<String, TmfXmlTest> tests) {
        if (fAcceptedEvents.length == 0 && fCond.length == 0) {
            return true;
        }

        boolean isValide = false;
        if (fAcceptedEvents.length != 0) {
            isValide |= validateEvent(event);
        }

        if (fCond.length != 0) {
            boolean condTest = true;
            for (int i = 0; i < fCond.length && condTest; i++) {
                if (fCond[i].equals(OTHER_STRING)) {
                    condTest &= true;
                } else {
                    TmfXmlTest test = tests.get(fCond[i]);
                    if (test == null) {
                        throw new IllegalStateException("Failed to find cond " + fCond[i]); //$NON-NLS-1$
                    }
                    condTest &= test.validate(event, scenarioName, activeState);
                }
            }
            isValide &= condTest;
        }
        return isValide;
    }

    private boolean validateEvent(ITmfEvent event) {
        String eventName = event.getName();

        for (String name : fAcceptedEvents) {

            if (name.equals(TmfXmlStrings.ANY)) {
                return true;
            }

            /* test for full name */
            if (eventName.equals(name)) {
                return true;
            }

            /* test for the wildcard at the end */
            if (name.endsWith(TmfXmlStrings.WILDCARD) && name.startsWith(TmfXmlStrings.WILDCARD) && eventName.startsWith(name.substring(1, name.length() - 1))) {
                return true;
            }

            /* test for the wildcard at the end */
            if ((name.endsWith(TmfXmlStrings.WILDCARD) && eventName.startsWith(name.substring(0, name.length() - 1)))) {
                return true;
            }
        }
        return false;
    }
}
