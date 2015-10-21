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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.tracecompass.tmf.analysis.xml.core.module.IXmlStateSystemContainer;
import org.eclipse.tracecompass.tmf.analysis.xml.core.stateprovider.TmfXmlStrings;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * This Class implements a State Definition in the XML-defined state system.
 *
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
     * Get the name of this state definition
     *
     * @return The name of this state definition
     */
    public String getName() {
        return fName;
    }

    /**
     * Tell if this state definition has to wait the next event before being
     * processed or not
     *
     * @return true, if the execution of this state definition is automatic
     *         false, if not
     */
    public boolean isAutomatic() {
        return fAutomatic;
    }

    /**
     * Get the list of transition for this state definition
     *
     * @return The list of transition for this state definition
     */
    public List<TmfXmlStateTransition> getTransitionList() {
        return fTransitionList;
    }
}
