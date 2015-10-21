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
    private final String fInput;
    private final String fNext;
    private final String fAction;
    private final boolean fSpecialFieldsToBeSaved;
    private final boolean fSpecialFieldsToBeCleared;

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
        fInput = input;
        String next = node.getAttribute(TmfXmlStrings.NEXT);
        if (next == null) {
            throw new IllegalArgumentException();
        }
        fNext = next;
        String action = node.getAttribute(TmfXmlStrings.ACTION);
        if (action == null) {
            throw new IllegalArgumentException();
        }
        fAction = action;
        fSpecialFieldsToBeSaved = (node.getAttribute(TmfXmlStrings.SAVE_SPECIAL_FIELDS).equals(TmfXmlStrings.EMPTY_STRING) ? false : Boolean.parseBoolean(node.getAttribute(TmfXmlStrings.SAVE_SPECIAL_FIELDS)));
        fSpecialFieldsToBeCleared = (node.getAttribute(TmfXmlStrings.CLEAR_SPECIAL_FIELDS).equals(TmfXmlStrings.EMPTY_STRING) ? false : Boolean.parseBoolean(node.getAttribute(TmfXmlStrings.CLEAR_SPECIAL_FIELDS)));
    }

    /**
     * @return the input of this transition
     */
    public String getfInput() {
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
    public String getfAction() {
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
