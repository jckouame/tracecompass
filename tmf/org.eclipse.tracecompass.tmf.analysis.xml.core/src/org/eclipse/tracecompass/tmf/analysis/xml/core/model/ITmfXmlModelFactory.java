/*******************************************************************************
 * Copyright (c) 2015 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial API and implementation
 *   Jean-christian Kouame - added the support for the XML pattern models
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.analysis.xml.core.model;

import java.util.List;

import org.eclipse.tracecompass.tmf.analysis.xml.core.module.IXmlStateSystemContainer;
import org.w3c.dom.Element;

/**
 * Interface to create XML model elements in different contexts. This allows to
 * reuse the same XML syntax and parsers, but use the elements differently
 * depending on the what is intended to be done with them.
 *
 * @author Geneviève Bastien
 */
public interface ITmfXmlModelFactory {

    /**
     * Create a new XML state attribute
     *
     * @param attribute
     *            XML element of the attribute
     * @param container
     *            The state system container this state attribute belongs to
     * @return The new state attribute
     */
    ITmfXmlStateAttribute createStateAttribute(Element attribute, IXmlStateSystemContainer container);

    /**
     * Create a new state value where the value corresponds to a path of
     * {@link ITmfXmlStateAttribute}
     *
     * @param node
     *            The state value XML element
     * @param container
     *            The state system container this state value belongs to
     * @param attributes
     *            The attributes representing the path to this value
     * @return The new state value
     */
    ITmfXmlStateValue createStateValue(Element node, IXmlStateSystemContainer container, List<ITmfXmlStateAttribute> attributes);

    /**
     * Create a new state value where the value corresponds to a field in an
     * event
     *
     * @param node
     *            The state value XML element
     * @param container
     *            The state system container this state value belongs to
     * @param eventField
     *            The event field where to get the value
     * @return The new state value
     */
    ITmfXmlStateValue createStateValue(Element node, IXmlStateSystemContainer container, String eventField);

    /**
     * Create a new XML condition
     *
     * @param node
     *            The XML root of this condition
     * @param container
     *            The state system container this condition belongs to
     * @return The new XML condition
     */
    TmfXmlCondition createCondition(Element node, IXmlStateSystemContainer container);

    /**
     * Create a new XML event handler
     *
     * @param node
     *            The XML event handler element
     * @param container
     *            The state system container this state value belongs to
     * @return The new XML event handler
     */
    TmfXmlEventHandler createEventHandler(Element node, IXmlStateSystemContainer container);

    /**
     * Create a new XML state change
     *
     * @param node
     *            The XML state change element
     * @param container
     *            The state system container this state change belongs to
     * @return The new XML state change
     */
    TmfXmlStateChange createStateChange(Element node, IXmlStateSystemContainer container);

    /**
     * Create a new XML location
     *
     * @param node
     *            The XML location element
     * @param container
     *            The state system container this location belongs to
     * @return The new XML location
     */
    TmfXmlLocation createLocation(Element node, IXmlStateSystemContainer container);

    /**
     * Create a new XML filter event handler
     *
     * @param node
     *            The XML filter event handler element
     * @param container
     *            The state system container this filter event handler belongs to
     * @return The new XML filter event handler
     * @since 2.0
     */
    TmfXmlPatternEventHandler createFilterEventHandler(Element node, IXmlStateSystemContainer container);

    /**
     * Create a new XML transition input
     *
     * @param node
     *            The XML transition input
     * @param container
     *            The state system container this transition input belongs to
     * @return The new XML transition input
     * @since 2.0
     */
    TmfXmlTransitionInput createTransitionInput(Element node, IXmlStateSystemContainer container);

    /**
     * Create a new XML action
     *
     * @param node
     *            The XML action
     * @param container
     *            The state system container this action belongs to
     * @return The new XML action
     * @since 2.0
     */
    TmfXmlAction createAction(Element node, IXmlStateSystemContainer container);

    /**
     * Create a new XML FSM
     *
     * @param node
     *            The XML FSM
     * @param container
     *            The state system container this FSM belongs to
     * @return The new XML FSM
     * @since 2.0
     */
    TmfXmlFsm createFsm(Element node, IXmlStateSystemContainer container);

    /**
     * Create a new XML state definition
     *
     * @param node
     *            The XML state definition
     * @param container
     *            The state system container this state definition belongs to
     * @return The new XML state definition
     * @since 2.0
     */
    TmfXmlStateDefinition createStateDefinition(Element node, IXmlStateSystemContainer container);

    /**
     * Create a new XML state transition
     *
     * @param node
     *            The XML state transition
     * @param container
     *            The state system container this state transition belongs to
     * @return The new XML state transition
     * @since 2.0
     */
    TmfXmlStateTransition createStateTransition(Element node, IXmlStateSystemContainer container);

    /**
     * Create a new XML timestamp condition
     *
     * @param node
     *            The XML timestamp condition
     * @param container
     *            The state system container this timestamp condition belongs to
     * @return The new XML timestamp condition
     * @since 2.0
     */
    TmfXmlTimestampCondition createTimestampsCondition(Element node, IXmlStateSystemContainer container);

    /**
     * Create a new XML synthetic event
     *
     * @param node
     *            The XML synthetic event
     * @param container
     *            The state system container this synthetic event belongs to
     * @return The new XML synthetic event
     * @since 2.0
     */
    TmfXmlSyntheticEventBuilder createSyntheticEventBuilder(Element node, IXmlStateSystemContainer container);
}
