/*******************************************************************************
 * Copyright (c) 2014, 2015 Ecole Polytechnique de Montreal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Florian Wininger - Initial API and implementation
 *   Naser Ezzati - Add the comparison operators
 *   Patrick Tasse - Add message to exceptions
 ******************************************************************************/

package org.eclipse.tracecompass.tmf.analysis.xml.core.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.common.core.NonNullUtils;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
import org.eclipse.tracecompass.statesystem.core.exceptions.AttributeNotFoundException;
import org.eclipse.tracecompass.statesystem.core.statevalue.ITmfStateValue;
import org.eclipse.tracecompass.tmf.analysis.xml.core.module.IXmlStateSystemContainer;
import org.eclipse.tracecompass.tmf.analysis.xml.core.module.XmlUtils;
import org.eclipse.tracecompass.tmf.analysis.xml.core.stateprovider.TmfXmlStrings;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.w3c.dom.Element;

/**
 * This Class implement a condition tree in the XML-defined state system.
 *
 * <pre>
 * example:
 * <and>
 *   <condition>
 *       <stateAttribute type="location" value="CurrentThread" />
 *       <stateAttribute type="constant" value="System_call" />
 *       <stateValue type="null" />
 *   </condition>
 *   <condition>
 *   </condition>
 * </and>
 * </pre>
 *
 * @author Florian Wininger
 */
public class TmfXmlCondition {

    private final List<TmfXmlCondition> fConditions = new ArrayList<>();
    private final List<ITmfXmlStateValue> fStateValueList = new ArrayList<>();
    private @Nullable TmfXmlTimestampsCondition fTimeCondition;
    private final LogicalOperator fOperator;
    private final ConditionType fType;
    private final IXmlStateSystemContainer fContainer;
    private final ConditionOperator fConditionOperator;
    private final int fNbOperande;

    private enum LogicalOperator {
        NONE, NOT, AND, OR,
    }

    private enum ConditionOperator {
        NONE, EQ, NE, GE, GT, LE, LT
    }

    private enum ConditionType {
        EVENT, TIME, NONE
    }

    /**
     * Constructor
     *
     * @param modelFactory
     *            The factory used to create XML model elements
     * @param node
     *            The XML root of this condition
     * @param container
     *            The state system container this condition belongs to
     */
    public TmfXmlCondition(ITmfXmlModelFactory modelFactory, Element node, IXmlStateSystemContainer container) {
        fContainer = container;

        Element rootNode = node;
        /* Process the conditions: in each case, only process Element nodes */
        List<Element> childElements = XmlUtils.getChildElements(rootNode);

        /*
         * If the node is an if, take the child as the root condition
         *
         * FIXME: Maybe the caller should do this instead.
         */
        if (node.getNodeName().equals(TmfXmlStrings.IF) || node.getNodeName().equals(TmfXmlStrings.TIME)) {
            if (childElements.isEmpty()) {
                throw new IllegalArgumentException("TmfXmlCondition constructor: IF node has no child element"); //$NON-NLS-1$
            }
            rootNode = childElements.get(0);
            childElements = XmlUtils.getChildElements(rootNode);
        }
        fNbOperande = Math.max(rootNode.getElementsByTagName(TmfXmlStrings.STATE_VALUE).getLength(), rootNode.getElementsByTagName(TmfXmlStrings.TIME_CONDITION).getLength());
        if (fNbOperande > 2) {
            throw new IllegalArgumentException("TmfXmlCondition: a condition has 2 state values at most"); //$NON-NLS-1$
        }

        switch (rootNode.getNodeName()) {
        case TmfXmlStrings.CONDITION:
        case TmfXmlStrings.CONDITION_FILTER_BY_EVENT:
            fOperator = LogicalOperator.NONE;
            fType = ConditionType.EVENT;
            if (fNbOperande == 1) {
                fConditionOperator = getConditionOperator(rootNode);
                getStateValuesForXmlCondition(modelFactory, NonNullUtils.checkNotNull(childElements));
            } else {
                fConditionOperator = ConditionOperator.EQ;
                for (Element element : childElements) {
                    if (element == null) {
                        throw new IllegalArgumentException();
                    }
                    fStateValueList.add(modelFactory.createStateValue(element, fContainer, new ArrayList<ITmfXmlStateAttribute>()));
                }
            }
            break;
        case TmfXmlStrings.CONDITION_FILTER_BY_TIME:
            Element timeCondition = childElements.get(0);
            if (timeCondition == null) {
                throw new IllegalArgumentException();
            }
            fOperator = LogicalOperator.NONE;
            fType = ConditionType.TIME;
            fConditionOperator = ConditionOperator.EQ;
            if (childElements.size() == 1) {
                if (childElements.get(0).getNodeName().equals(TmfXmlStrings.TIME_CONDITION)) {
                    fTimeCondition = modelFactory.createTimestampsCondition(timeCondition, fContainer);
                } else {
                    throw new IllegalArgumentException("TmfXmlCondtion: Invalid condition"); //$NON-NLS-1$
                }
            }
            break;
        case TmfXmlStrings.NOT:
            fOperator = LogicalOperator.NOT;
            fType = ConditionType.NONE;
            fConditionOperator = ConditionOperator.NONE;
            Element element = childElements.get(0);
            if (element == null) {
                throw new IllegalArgumentException();
            }
            fConditions.add(modelFactory.createCondition(element, fContainer));
            break;
        case TmfXmlStrings.AND:
            fOperator = LogicalOperator.AND;
            fType = ConditionType.NONE;
            fConditionOperator = ConditionOperator.NONE;
            for (Element condition : childElements) {
                if (condition == null) {
                    continue;
                }
                fConditions.add(modelFactory.createCondition(condition, fContainer));
            }
            break;
        case TmfXmlStrings.OR:
            fOperator = LogicalOperator.OR;
            fType = ConditionType.NONE;
            fConditionOperator = ConditionOperator.NONE;
            for (Element condition : childElements) {
                if (condition == null) {
                    continue;
                }
                fConditions.add(modelFactory.createCondition(condition, fContainer));
            }
            break;
        default:
            throw new IllegalArgumentException("TmfXmlCondition constructor: XML node is of the wrong type"); //$NON-NLS-1$
        }
    }

    private void getStateValuesForXmlCondition(ITmfXmlModelFactory modelFactory, List<Element> childElements) {
        Element stateValueElement = childElements.remove(childElements.size() - 1);
        if (stateValueElement == null) {
            throw new IllegalStateException();
        }

        /*
         * A state value is either preceded by an eventField or a number of
         * state attributes
         */
        if (childElements.size() == 1 && childElements.get(0).getNodeName().equals(TmfXmlStrings.ELEMENT_FIELD)) {
            String attribute = childElements.get(0).getAttribute(TmfXmlStrings.NAME);
            if (attribute == null) {
                throw new IllegalArgumentException();
            }
            fStateValueList.add(modelFactory.createStateValue(stateValueElement, fContainer, attribute));
        } else {
            List<ITmfXmlStateAttribute> attributes = new ArrayList<>();
            for (Element element : childElements) {
                if (!element.getNodeName().equals(TmfXmlStrings.STATE_ATTRIBUTE)) {
                    throw new IllegalArgumentException("TmfXmlCondition: a condition either has a eventField element or a number of TmfXmlStateAttribute elements before the state value"); //$NON-NLS-1$
                }
                ITmfXmlStateAttribute attribute = modelFactory.createStateAttribute(element, fContainer);
                attributes.add(attribute);
            }
            fStateValueList.add(modelFactory.createStateValue(stateValueElement, fContainer, attributes));
        }
    }

    private static ConditionOperator getConditionOperator(Element rootNode) {
        String equationType = rootNode.getAttribute(TmfXmlStrings.OPERATOR);
        switch (equationType) {
        case TmfXmlStrings.EQ:
            return ConditionOperator.EQ;
        case TmfXmlStrings.NE:
            return ConditionOperator.NE;
        case TmfXmlStrings.GE:
            return ConditionOperator.GE;
        case TmfXmlStrings.GT:
            return ConditionOperator.GT;
        case TmfXmlStrings.LE:
            return ConditionOperator.LE;
        case TmfXmlStrings.LT:
            return ConditionOperator.LT;
        case TmfXmlStrings.NULL:
            return ConditionOperator.EQ;
        default:
            throw new IllegalArgumentException("TmfXmlCondition: invalid comparison operator."); //$NON-NLS-1$
        }
    }

    /**
     * Test the result of the condition for an event
     *
     * @param event
     *            The event on which to test the condition
     * @param args
     *            The arguments required to test this event
     * @return Whether the condition is true or not
     * @throws AttributeNotFoundException
     *             The state attribute was not found
     * @since 2.0
     */
    public boolean testForEvent(ITmfEvent event, String... args) throws AttributeNotFoundException {
        ITmfStateSystem ss = fContainer.getStateSystem();
        switch (fType) {
        case EVENT:
            return testForEvent(event, NonNullUtils.checkNotNull(ss), args);
        case TIME:
            if (fTimeCondition != null) {
                return fTimeCondition.testForEvent(event, args);
            }
            break;
        case NONE:
            if (!fConditions.isEmpty()) {
                /* Verify a condition tree */
                switch (fOperator) {
                case AND:
                    for (TmfXmlCondition childCondition : fConditions) {
                        if (!childCondition.testForEvent(event, args)) {
                            return false;
                        }
                    }
                    return true;
                case NONE:
                    break;
                case NOT:
                    return !fConditions.get(0).testForEvent(event, args);
                case OR:
                    for (TmfXmlCondition childCondition : fConditions) {
                        if (childCondition.testForEvent(event, args)) {
                            return true;
                        }
                    }
                    return false;
                default:
                    break;

                }
            }
            break;
        default:
            throw new IllegalStateException("TmfXmlCondition: the condition should be either a state value or be the result of a condition tree"); //$NON-NLS-1$
        }
        return true;
    }

    private boolean testForEvent(ITmfEvent event, ITmfStateSystem ss, String... args) throws AttributeNotFoundException {
        /*
         * The condition is either the equality check of a state value or a
         * boolean operation on other conditions
         */
        if (fStateValueList.isEmpty()) {
            throw new IllegalStateException();
        }
        switch (fStateValueList.size()) {
        case 1:
            ITmfXmlStateValue filter = fStateValueList.get(0);
            int quark = IXmlStateSystemContainer.ROOT_QUARK;
            for (ITmfXmlStateAttribute attribute : filter.getAttributes()) {
                quark = attribute.getAttributeQuark(event, quark, args);
                /*
                 * When verifying a condition, the state attribute must exist,
                 * if it does not, the query is not valid, we stop the condition
                 * check
                 */
                if (quark == IXmlStateSystemContainer.ERROR_QUARK) {
                    throw new AttributeNotFoundException(ss.getSSID() + " Attribute:" + attribute); //$NON-NLS-1$
                }
            }

            /* Get the value to compare to from the XML file */
            ITmfStateValue valueXML;
            valueXML = filter.getValue(event);

            /*
             * The actual value: it can be either queried in the state system or
             * found in the event
             */
            ITmfStateValue valueState = (quark != IXmlStateSystemContainer.ROOT_QUARK) ? ss.queryOngoingState(quark) : filter.getEventFieldValue(event);
            if (valueState == null) {
                throw new IllegalStateException();
            }
            return compare(valueState, valueXML, fConditionOperator);
        case 2:
            ITmfStateValue valuesXML[] = new ITmfStateValue[fStateValueList.size()];
            valuesXML[0] = fStateValueList.get(0).getValue(event, args);
            valuesXML[1] = fStateValueList.get(1).getValue(event, args);
            return valuesXML[0].equals(valuesXML[1]);
        default:
            throw new IllegalStateException();
        }
    }

    @Override
    public String toString() {
        return "TmfXmlCondition: " + fOperator + " on " + fConditions; //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * Compare two ITmfStateValues based on the given comparison operator
     *
     * @param source
     *            the state value to compare to
     * @param dest
     *            the state value to be compared with
     * @param comparisonOperator
     *            the operator to compare the inputs
     * @return the boolean result of the comparison
     */
    public boolean compare(ITmfStateValue source, ITmfStateValue dest, ConditionOperator comparisonOperator) {
        switch (comparisonOperator) {
        case EQ:
            return (source.compareTo(dest) == 0);
        case NE:
            return (source.compareTo(dest) != 0);
        case GE:
            return (source.compareTo(dest) >= 0);
        case GT:
            return (source.compareTo(dest) > 0);
        case LE:
            return (source.compareTo(dest) <= 0);
        case LT:
            return (source.compareTo(dest) < 0);
        case NONE:
        default:
            throw new IllegalArgumentException("TmfXmlCondition: invalid comparison operator."); //$NON-NLS-1$
        }

    }

}