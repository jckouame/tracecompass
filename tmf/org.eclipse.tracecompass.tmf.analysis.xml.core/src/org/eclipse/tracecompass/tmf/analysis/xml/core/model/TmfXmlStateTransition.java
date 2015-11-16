package org.eclipse.tracecompass.tmf.analysis.xml.core.model;

import org.eclipse.tracecompass.tmf.analysis.xml.core.module.IXmlStateSystemContainer;
import org.eclipse.tracecompass.tmf.analysis.xml.core.stateprovider.TmfXmlStrings;
import org.w3c.dom.Element;

/**
 * @author Jean-Christian Kouame
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
     * @param modelFactory
     *            The factory used to create XML model elements
     * @param node
     *            The XML root of this state transition
     * @param container
     *            The state system container this state transition belongs to
     */
    public TmfXmlStateTransition(ITmfXmlModelFactory modelFactory, Element node, IXmlStateSystemContainer container) {
        String input = node.getAttribute(TmfXmlStrings.INPUT);
        if (input == null) {
            throw new IllegalArgumentException();
        }
        String[] InputArray = input.split(REGEX);
        if (InputArray == null) {
            throw new IllegalArgumentException();
        }
        fInput = InputArray;
        String next = node.getAttribute(TmfXmlStrings.NEXT);
        if (next == null) {
            throw new IllegalArgumentException();
        }
        fNext = next;
        String action = node.getAttribute(TmfXmlStrings.ACTION);
        if (action == null) {
            throw new IllegalArgumentException();
        }
        String[] actionArray = action.split(REGEX);
        if (actionArray == null) {
            throw new IllegalArgumentException();
        }
//        fAction = actionArray;
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
     * @return the input of this transition
     */
    public String[] getfInput() {
        return fInput;
    }

    /**
     * @return the next state this transition try to reach
     */
    public String getfNext() {
        return fNext;
    }

    /**
     * @return the action to execute if the input is validate
     */
    public String[] getfAction() {
        return fAction;
    }

    /**
     * @return If the special fields have to be saved or not
     */
    public boolean isSpecialFieldsTobeSaved() {
        return fSpecialFieldsToBeSaved;
    }

    /**
     * @return If the special fields have to cleared or not
     */
    public boolean isSpecialFieldsToBeCleared() {
        return fSpecialFieldsToBeCleared;
    }
}
