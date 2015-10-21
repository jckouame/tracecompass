/*******************************************************************************
 * Copyright (c) 2015 Ericsson
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
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.statesystem.core.exceptions.AttributeNotFoundException;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateValueTypeException;
import org.eclipse.tracecompass.statesystem.core.statevalue.ITmfStateValue;
import org.eclipse.tracecompass.tmf.analysis.xml.core.model.readwrite.TmfXmlReadWriteScenarioStatus;
import org.eclipse.tracecompass.tmf.analysis.xml.core.module.IXmlStateSystemContainer;
import org.eclipse.tracecompass.tmf.analysis.xml.core.stateprovider.TmfXmlStrings;
import org.eclipse.tracecompass.tmf.analysis.xml.core.stateprovider.XmlPatternStateProvider;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.event.TmfSyntheticEvent;
import org.eclipse.tracecompass.tmf.core.timestamp.ITmfTimestamp;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * This class define an synthetic event builder. It will use the XML description
 * of the synthetic event to generate it at runtime.
 *
 * @since 2.0
 *
 */
public class TmfXmlSyntheticEventBuilder {

    /**
     * The string unknown
     */
    public static final String UNKNOWN_STRING = "unknown"; //$NON-NLS-1$
    /**
     * Prefix for the synthetic events name
     */
    public static final String SYN_PREFIX = "syn/"; //$NON-NLS-1$
    private final ITmfXmlModelFactory fModelFactory;
    private final IXmlStateSystemContainer fContainer;
    private final @Nullable Element fContentElement;
    private final Element fTypeElement;

    /**
     * @param modelFactory
     *            The factory used to create XML model elements
     * @param node
     *            XML element of the synthetic event builder
     * @param parent
     *            The state system container this synthetic event builder
     *            belongs to
     */
    public TmfXmlSyntheticEventBuilder(ITmfXmlModelFactory modelFactory, Element node, IXmlStateSystemContainer parent) {
        fModelFactory = modelFactory;
        fContainer = parent;
        NodeList nodesEventType = node.getElementsByTagName(TmfXmlStrings.SYN_TYPE);
        Element element = (Element) nodesEventType.item(0);
        if (element == null) {
            throw new IllegalArgumentException();
        }
        fTypeElement = element;
        NodeList nodesEventContent = node.getElementsByTagName(TmfXmlStrings.SYN_CONTENT);
        fContentElement = (Element) nodesEventContent.item(0);
    }

    /**
     * Generate a synthetic event
     *
     * @param event
     *            The current event
     * @param start
     *            Start time of the synthetic event to generate
     * @param end
     *            End time of the synthetic event to generate
     * @param args
     *            The arguments used to generate the synthetic event
     * @return The synthetic event generated
     */
    public TmfSyntheticEvent generateSyntheticEvent(ITmfEvent event, ITmfTimestamp start, ITmfTimestamp end, String... args) {
        int scale = event.getTimestamp().getScale();
        long startValue = start.normalize(0, ITmfTimestamp.NANOSECOND_SCALE).getValue();
        long endValue = end.normalize(0, ITmfTimestamp.NANOSECOND_SCALE).getValue();
        String eventName = getEventName(event, args);
        Map<String, Object> fields = new HashMap<>();
        getSyntheticFields(event, start, end, fields, args);
        return new TmfSyntheticEvent(startValue, endValue, scale, eventName, fields);
    }

    private String getEventName(ITmfEvent event, String... args) {
        StringBuilder name = new StringBuilder(SYN_PREFIX);
        name.append(fTypeElement.getAttribute(TmfXmlStrings.EVENT_NAME));
        ITmfXmlStateValue nameStateValue = null;
        if (name.toString().equals(SYN_PREFIX)) {
            Element elementEventNameStateValue = (Element) fTypeElement.getElementsByTagName(TmfXmlStrings.STATE_VALUE).item(0);
            if (elementEventNameStateValue == null) {
                throw new IllegalArgumentException();
            }
            nameStateValue = fModelFactory.createStateValue(elementEventNameStateValue, fContainer, new ArrayList<ITmfXmlStateAttribute>());
            ITmfStateValue value;
            try {
                value = nameStateValue.getValue(event, args);
                switch (value.getType()) {
                case DOUBLE:
                    name.append(String.valueOf(value.unboxDouble()));
                    break;
                case INTEGER:
                    name.append(String.valueOf(value.unboxInt()));
                    break;
                case LONG:
                    name.append(String.valueOf(value.unboxLong()));
                    break;
                case NULL:
                    name.append(UNKNOWN_STRING);
                    break;
                case STRING:
                    name.append(value.unboxStr());
                    break;
                default:
                    throw new StateValueTypeException();
                }
            } catch (AttributeNotFoundException e) {
            }
        }
        return name.toString();
    }

    /**
     * Compute all the field and their value for this synthetic event
     *
     * @param event
     *            The current event
     * @param start
     *            The start timestamp of this event
     * @param end
     *            The end timestamp of this event
     * @param fields
     *            The map that will contained all the fields
     * @param args
     *            The arguments used to get the fields
     */
    public void getSyntheticFields(ITmfEvent event, ITmfTimestamp start, ITmfTimestamp end, Map<String, Object> fields, String... args) {
        if (fContentElement != null) {
            NodeList nodesEventField = fContentElement.getElementsByTagName(TmfXmlStrings.SYN_FIELD);
            for (int i = 0; i < nodesEventField.getLength(); i++) {
                Element fieldElement = (Element) nodesEventField.item(i);
                String fieldName = fieldElement.getAttribute(TmfXmlStrings.NAME);
                String fieldType = fieldElement.getAttribute(TmfXmlStrings.TYPE);
                String fieldValue = fieldElement.getAttribute(TmfXmlStrings.VALUE);
                ITmfXmlStateValue fieldStateValue = null;
                if (fieldValue.isEmpty()) {
                    Element elementFieldStateValue = (Element) fieldElement.getElementsByTagName(TmfXmlStrings.STATE_VALUE).item(0);
                    if (elementFieldStateValue == null) {
                        throw new IllegalArgumentException();
                    }
                    fieldStateValue = fModelFactory.createStateValue(elementFieldStateValue, fContainer, new ArrayList<ITmfXmlStateAttribute>());
                }

                try {
                    // FIXME The state value can only be a query state value and
                    // do not support event field.
                    switch (fieldType) {
                    case TmfXmlStrings.TYPE_INT:
                        Integer valueInt = (fieldStateValue != null && fieldValue.isEmpty()) ? fieldStateValue.getValue(event, args).unboxInt() : Integer.valueOf(fieldValue);
                        fields.put(fieldName, valueInt);
                        break;
                    case TmfXmlStrings.TYPE_LONG:
                        Long valueLong = (fieldStateValue != null && fieldValue.isEmpty()) ? fieldStateValue.getValue(event, args).unboxLong() : Long.valueOf(fieldValue);
                        fields.put(fieldName, valueLong);
                        break;
                    case TmfXmlStrings.TYPE_STRING:
                        String valueString = (fieldStateValue != null && fieldValue.isEmpty()) ? fieldStateValue.getValue(event, args).unboxStr() : fieldValue;
                        fields.put(fieldName, valueString);
                        break;
                    default:
                        throw new IllegalArgumentException("Synthetic field : Synthetic field should be a value or a state value query"); //$NON-NLS-1$
                    }
                } catch (NumberFormatException e) {
                } catch (AttributeNotFoundException e) {
                }
            }
        }
        for (Entry<String, String> entry :  ((XmlPatternStateProvider) fContainer).getDefinedFields().entrySet()) {
            final String fieldName = entry.getValue();
            ITmfStateValue value = TmfXmlReadWriteScenarioStatus.getInstance().getScenarioSpecialFieldValue(event, fContainer, fieldName, args);
            switch (value.getType()) {
            case STRING:
                String valueString = value.unboxStr();
                fields.put(fieldName, valueString);
                break;
            case DOUBLE:
                Double valueDouble = value.unboxDouble();
                fields.put(fieldName, valueDouble);
                break;
            case INTEGER:
                Integer valueInt = value.unboxInt();
                fields.put(fieldName, valueInt);
                break;
            case LONG:
                Long valueLong = value.unboxLong();
                fields.put(fieldName, valueLong);
                break;
            case NULL:
            default:
                break;
            }
        }
    }
}
