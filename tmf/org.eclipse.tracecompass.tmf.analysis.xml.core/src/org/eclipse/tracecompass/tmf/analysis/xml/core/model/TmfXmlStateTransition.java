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

import org.eclipse.tracecompass.tmf.analysis.xml.core.module.IXmlStateSystemContainer;
import org.eclipse.tracecompass.tmf.analysis.xml.core.stateprovider.TmfXmlStrings;
import org.w3c.dom.Element;

/**
 * This Class implements a State Transition in the XML-defined state system.
 * @since 2.0
 *
 */
public class TmfXmlStateTransition {
    private static final String REGEX = ":"; //$NON-NLS-1$
    private final String[] fInput;
    private final String fNext;
    private final String[] fAction;
    private final boolean fSpecialFieldsToBeSaved;
    private final boolean fSpecialFieldsToBeCleared;
    private static final String SAVED_SPECIAL_FIELDS_ACTION_STRING = ISingleAction.SPECIAL_ACTION_PREFIX + ISingleAction.SAVE_SPECIAL_FIELD_STRING;
    private static final String CLEAR_SPECIAL_FIELDS_ACTION_STRINGS = ISingleAction.SPECIAL_ACTION_PREFIX + ISingleAction.CLEAR_SPECIAL_FIELD_STRING;

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
        String input = node.getAttribute(TmfXmlStrings.INPUT);
        String[] InputArray = input.split(REGEX);
        fInput = InputArray;
        String next = node.getAttribute(TmfXmlStrings.NEXT);
        fNext = next;
        String action = node.getAttribute(TmfXmlStrings.ACTION);
        String[] actionArray = action.split(REGEX);
        fSpecialFieldsToBeSaved = (node.getAttribute(TmfXmlStrings.SAVE_SPECIAL_FIELDS).equals(TmfXmlStrings.EMPTY_STRING) ? false : Boolean.parseBoolean(node.getAttribute(TmfXmlStrings.SAVE_SPECIAL_FIELDS)));
        fSpecialFieldsToBeCleared = (node.getAttribute(TmfXmlStrings.CLEAR_SPECIAL_FIELDS).equals(TmfXmlStrings.EMPTY_STRING) ? false : Boolean.parseBoolean(node.getAttribute(TmfXmlStrings.CLEAR_SPECIAL_FIELDS)));
        int nbSpecialAction = 0;
        if (fSpecialFieldsToBeSaved) {
            nbSpecialAction++;
        }
        if (fSpecialFieldsToBeCleared) {
            nbSpecialAction++;
        }
        fAction = new String[actionArray.length + nbSpecialAction];
        int index = 0;
        if (fSpecialFieldsToBeSaved) {
            fAction[index] = SAVED_SPECIAL_FIELDS_ACTION_STRING;
            index++;
        }
        for (int i = 0; i < actionArray.length; i++) {
            fAction[index] = actionArray[i];
            index++;
        }
        if (fSpecialFieldsToBeCleared) {
            fAction[index] = CLEAR_SPECIAL_FIELDS_ACTION_STRINGS;
        }
    }

    /**
     * The ID input condition for this transition.
     *
     * @return the input of this transition
     */
    public String[] getInput() {
        return fInput;
    }

    /**
     * The next state of the state machine this state transition belongs to.
     *
     * @return the next state this transition try to reach
     */
    public String getNext() {
        return fNext;
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
     * Tell if the specials fields have to be saved at this step of the scenario
     *
     * @return If the special fields have to be saved or not
     */
    public boolean isSpecialFieldsTobeSaved() {
        return fSpecialFieldsToBeSaved;
    }

    /**
     * Tell if the special fields have to be cleared at this moment of this scenario
     *
     * @return If the special fields have to cleared or not
     */
    public boolean isSpecialFieldsToBeCleared() {
        return fSpecialFieldsToBeCleared;
    }
}
