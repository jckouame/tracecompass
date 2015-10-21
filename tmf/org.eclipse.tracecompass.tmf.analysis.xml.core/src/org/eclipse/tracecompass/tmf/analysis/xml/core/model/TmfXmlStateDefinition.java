package org.eclipse.tracecompass.tmf.analysis.xml.core.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.tracecompass.tmf.analysis.xml.core.module.IXmlStateSystemContainer;
import org.eclipse.tracecompass.tmf.analysis.xml.core.stateprovider.TmfXmlStrings;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * @author Jean-Christian Kouame
 * @since 2.0
 *
 */
public class TmfXmlStateDefinition {

    private final String fName;
    private final boolean fAutomatic;
    private final IXmlStateSystemContainer fParent;
    private final List<TmfXmlStateTransition> fTransitionList = new ArrayList<>();

    /**
     * Constructor
     *
     * @param modelFactory
     *            The factory used to create XML model elements
     * @param node
     *            The XML root of this state definition
     * @param parent
     *            The state system container this state definition belongs to
     */
    public TmfXmlStateDefinition(ITmfXmlModelFactory modelFactory, Element node, IXmlStateSystemContainer parent) {
        fParent = parent;
        final String name = node.getAttribute(TmfXmlStrings.NAME);
        if (name == null) {
            throw new IllegalArgumentException();
        }
        fName = name;
        fAutomatic = (node.getAttribute(TmfXmlStrings.AUTOMATIC).equals(TmfXmlStrings.EMPTY_STRING) ? false : Boolean.parseBoolean(node.getAttribute(TmfXmlStrings.AUTOMATIC)));

        NodeList nodesTransition = node.getElementsByTagName(TmfXmlStrings.TRANSITION);
        for (int i = 0; i < nodesTransition.getLength(); i++) {
            final Element element = (Element) nodesTransition.item(i);
            if (element == null) {
                throw new IllegalArgumentException();
            }
            TmfXmlStateTransition transition = modelFactory.createStateTransition(element, fParent);
            fTransitionList.add(transition);
        }
    }

    /**
     * @return The name of this state definition
     */
    public String getName() {
        return fName;
    }

    /**
     * @return true, if the execution of this state definition is automatic
     *         false, if not
     */
    public boolean isfAutomatic() {
        return fAutomatic;
    }

    /**
     * @return The list of transition for this state definition
     */
    public List<TmfXmlStateTransition> getfTransitionList() {
        return fTransitionList;
    }
}
