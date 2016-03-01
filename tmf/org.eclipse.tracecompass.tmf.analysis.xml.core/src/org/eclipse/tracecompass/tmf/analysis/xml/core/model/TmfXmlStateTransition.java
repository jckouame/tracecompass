/*******************************************************************************
 * Copyright (c) 2016 Ecole Polytechnique de Montreal, Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.tracecompass.tmf.analysis.xml.core.model;

import org.eclipse.tracecompass.tmf.analysis.xml.core.module.IXmlStateSystemContainer;
import org.eclipse.tracecompass.tmf.analysis.xml.core.stateprovider.TmfXmlStrings;
import org.w3c.dom.Element;

/**
 * This Class implements a state transition tree in the XML-defined state
 * system.
 *
 * @author Jean-Christian Kouame
 * @since 2.0
 *
 */
public class TmfXmlStateTransition extends TmfXmlBasicTransition {
    private static final String REGEX = ":"; //$NON-NLS-1$
    private final String fTarget;
    private final String[] fAction;
    private final boolean fStoredFieldsToBeSaved;
    private final boolean fStoredFieldsToBeCleared;
    private static final String SAVED_STORED_FIELDS_ACTION_STRING = ISingleAction.SPECIAL_ACTION_PREFIX + ISingleAction.SAVE_STORED_FIELDS_STRING;
    private static final String CLEAR_STORED_FIELDS_ACTION_STRINGS = ISingleAction.SPECIAL_ACTION_PREFIX + ISingleAction.CLEAR_STORED_FIELDS_STRING;

    /**
     * Constructor
     *
     * @param modelFactory
     *            The factory used to create XML model elements
     * @param node
     *            The XML root of this state transition
     * @param container
     *            The state system container this state transition belongs to
     */
    public TmfXmlStateTransition(ITmfXmlModelFactory modelFactory, Element node, IXmlStateSystemContainer container) {
        super(node);
        String target = node.getAttribute(TmfXmlStrings.TARGET);
        if (target.isEmpty()) {
            throw new IllegalStateException("No target state has been specified."); //$NON-NLS-1$
        }
        fTarget = target;
        String action = node.getAttribute(TmfXmlStrings.ACTION);
        String[] actionArray = action.equals(TmfXmlStrings.NULL) ? new String[]{} : action.split(REGEX);
        fStoredFieldsToBeSaved = (node.getAttribute(TmfXmlStrings.SAVE_STORED_FIELDS).equals(TmfXmlStrings.EMPTY_STRING) ? false : Boolean.parseBoolean(node.getAttribute(TmfXmlStrings.SAVE_STORED_FIELDS)));
        fStoredFieldsToBeCleared = (node.getAttribute(TmfXmlStrings.CLEAR_STORED_FIELDS).equals(TmfXmlStrings.EMPTY_STRING) ? false : Boolean.parseBoolean(node.getAttribute(TmfXmlStrings.CLEAR_STORED_FIELDS)));
        int nbSpecialAction = 0;
        if (fStoredFieldsToBeSaved) {
            nbSpecialAction++;
        }
        if (fStoredFieldsToBeCleared) {
            nbSpecialAction++;
        }
        fAction = new String[actionArray.length + nbSpecialAction];
        int index = 0;
        if (fStoredFieldsToBeSaved) {
            fAction[index] = SAVED_STORED_FIELDS_ACTION_STRING;
            index++;
        }
        for (int i = 0; i < actionArray.length; i++) {
            fAction[index] = actionArray[i];
            index++;
        }
        if (fStoredFieldsToBeCleared) {
            fAction[index] = CLEAR_STORED_FIELDS_ACTION_STRINGS;
        }
    }

    /**
     * The next state of the state machine this state transition belongs to.
     *
     * @return the next state this transition try to reach
     */
    public String getTarget() {
        return fTarget;
    }

    /**
     * The action to be executed when the input of this state transition is
     * validated
     *
     * @return the action to execute if the input is validate
     */
    public String[] getAction() {
        return fAction;
    }

    /**
     * Tell if the stored fields have to be saved at this step of the scenario
     *
     * @return If the stored fields have to be saved or not
     */
    public boolean isStoredFieldsTobeSaved() {
        return fStoredFieldsToBeSaved;
    }

    /**
     * Tell if the stored fields have to be cleared at this moment of this scenario
     *
     * @return If the stored fields have to cleared or not
     */
    public boolean isStoredFieldsToBeCleared() {
        return fStoredFieldsToBeCleared;
    }
}
