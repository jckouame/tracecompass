/*******************************************************************************
 * Copyright (c) 2016 Ecole Polytechnique de Montreal, Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.tracecompass.tmf.analysis.xml.core.model;

import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;

/**
 * Interface for single action behavior
 *
 * @author Jean-Christian Kouame
 * @since 2.0
 *
 */
public interface ISingleAction {

    /**
     * The separator use for multiple actions in the XML file
     */
    public final static String ACTION_SEPARATOR = ":"; //$NON-NLS-1$

    /**
     * The character used to start special word in the XML file
     */
    public final static String SPECIAL_ACTION_PREFIX = "#"; //$NON-NLS-1$

    /**
     * The save special field action label
     */
    public final static String SAVE_STORED_FIELDS_STRING = "saveSpecialField"; //$NON-NLS-1$

    /**
     * The clear special field action label
     */
    public final static String CLEAR_STORED_FIELDS_STRING = "clearSpecialField"; //$NON-NLS-1$

    /**
     * Execute the action
     *
     * @param event
     *            The current event to process
     * @param scenarioName
     *            The active scenario name
     * @param activeState
     *            The active scenario state
     */
    void execute(ITmfEvent event, String scenarioName, String activeState);
}
