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
package org.eclipse.tracecompass.tmf.analysis.xml.core.stateprovider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.common.core.NonNullUtils;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystemBuilder;
import org.eclipse.tracecompass.tmf.analysis.xml.core.model.ITmfXmlModelFactory;
import org.eclipse.tracecompass.tmf.analysis.xml.core.model.TmfXmlLocation;
import org.eclipse.tracecompass.tmf.analysis.xml.core.model.TmfXmlPatternEventHandler;
import org.eclipse.tracecompass.tmf.analysis.xml.core.model.readwrite.TmfXmlReadWriteModelFactory;
import org.eclipse.tracecompass.tmf.analysis.xml.core.module.IXmlStateSystemContainer;
import org.eclipse.tracecompass.tmf.analysis.xml.core.module.XmlUtils;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.event.TmfSyntheticEvent;
import org.eclipse.tracecompass.tmf.core.statesystem.AbstractTmfStateProvider;
import org.eclipse.tracecompass.tmf.core.statesystem.ITmfStateProvider;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * This is the state change input plug-in for Trace Compass's state system which handles
 * the XML Pattern Format
 *
 * @since 2.0
 */
public class XmlPatternStateProvider extends AbstractTmfStateProvider implements IXmlStateSystemContainer {

    private final IPath fFilePath;
    @NonNull
    private final String fStateId;

    /** List of all Event Handlers */
    private final Map<String, TmfXmlPatternEventHandler> fFilterEventHandlers = new HashMap<>();

    /** List of all Locations */
    private final Set<TmfXmlLocation> fLocations;

    /** Map for defined values */
    private final Map<String, String> fDefinedValues = new HashMap<>();

    /** Map for defined values */
    private final Map<String, String> fDefinedFields = new HashMap<>();

    private final Queue<ITmfEvent> fSyncEventQueue = new LinkedList<>();

    private List<ITmfEvent> fSynEventList = new ArrayList<>();

    /**
     * Instantiate a new state provider plug-in.
     *
     * @param trace
     *            The trace
     * @param stateid
     *            The state system id, corresponding to the analysis_id
     *            attribute of the filter state provider element of the XML file
     * @param file
     *            Path to the XML file containing the filter state provider
     *            definition
     */
    public XmlPatternStateProvider(@NonNull ITmfTrace trace, @NonNull String stateid, IPath file) {
        super(trace, stateid);
        fStateId = stateid;
        fFilePath = file;
        Element doc = XmlUtils.getElementInFile(fFilePath.makeAbsolute().toOSString(), TmfXmlStrings.FILTER, fStateId);
        if (doc == null) {
            fLocations = new HashSet<>();
            return;
        }

        ITmfXmlModelFactory modelFactory = TmfXmlReadWriteModelFactory.getInstance();
        /* parser for defined Fiels */
        NodeList definedFieldNodes = doc.getElementsByTagName(TmfXmlStrings.DEFINED_FIELD);
        for (int i = 0; i < definedFieldNodes.getLength(); i++) {
            Element element = (Element) definedFieldNodes.item(i);
            fDefinedFields.put(element.getAttribute(TmfXmlStrings.NAME), element.getAttribute(TmfXmlStrings.ID));
        }

        /* parser for defined Values */
        NodeList definedStateNodes = doc.getElementsByTagName(TmfXmlStrings.DEFINED_VALUE);
        for (int i = 0; i < definedStateNodes.getLength(); i++) {
            Element element = (Element) definedStateNodes.item(i);
            fDefinedValues.put(element.getAttribute(TmfXmlStrings.NAME), element.getAttribute(TmfXmlStrings.VALUE));
        }

        /* parser for the locations */
        NodeList locationNodes = doc.getElementsByTagName(TmfXmlStrings.LOCATION);
        Set<TmfXmlLocation> locations = new HashSet<>();
        for (int i = 0; i < locationNodes.getLength(); i++) {
            Element element = (Element) locationNodes.item(i);
            if (element == null) {
                continue;
            }
            TmfXmlLocation location = modelFactory.createLocation(element, this);
            locations.add(location);
        }
        fLocations = Collections.unmodifiableSet(locations);

        /* parser for the event handlers */
        NodeList nodes = doc.getElementsByTagName(TmfXmlStrings.FILTER_HANDLER);
        for (int i = 0; i < nodes.getLength(); i++) {
            Element element = (Element) nodes.item(i);
            if (element == null) {
                continue;
            }
            TmfXmlPatternEventHandler handler = modelFactory.createFilterEventHandler(element, this);
            fFilterEventHandlers.put(handler.getName(), handler);
        }
    }

    /**
     * Get the state id of the state provider
     *
     * @return The state id of the state provider
     */
    @NonNull
    public String getStateId() {
        return fStateId;
    }

    @Override
    public int getVersion() {
        Element ssNode = XmlUtils.getElementInFile(fFilePath.makeAbsolute().toOSString(), TmfXmlStrings.FILTER, fStateId);
        if (ssNode != null) {
            return Integer.parseInt(ssNode.getAttribute(TmfXmlStrings.VERSION));
        }
        /*
         * The version attribute is mandatory and XML files that don't validate
         * with the XSD are ignored, so this should never happen
         */
        throw new IllegalStateException("The state provider XML node should have a version attribute"); //$NON-NLS-1$
    }

    @Override
    public ITmfStateProvider getNewInstance() {
        return new XmlPatternStateProvider(this.getTrace(), getStateId(), fFilePath);
    }

    @Override
    protected void eventHandle(ITmfEvent event) {
        while (!fSyncEventQueue.isEmpty()) {
            ITmfEvent synEvent = fSyncEventQueue.poll();
            if (synEvent != null) {
                for (Map.Entry<String, TmfXmlPatternEventHandler> entry : fFilterEventHandlers.entrySet()) {
                    entry.getValue().handleEvent(synEvent);
                }
            }
        }
        for (Map.Entry<String, TmfXmlPatternEventHandler> entry : fFilterEventHandlers.entrySet()) {
            entry.getValue().handleEvent(event);
        }
    }

    /**
     * Get the defined value associated with a constant
     *
     * @param constant
     *            The constant defining this value
     * @return The actual value corresponding to this constant
     */
    public String getDefinedValue(String constant) {
        return fDefinedValues.get(constant);
    }

    /**
     * Get the defined fields list
     *
     * @return The list of defined fields
     */
    public Map<String, String> getDefinedFields() {
        return fDefinedFields;
    }

    @Override
    public String getAttributeValue(String name) {
        String attribute = name;
        if (attribute.startsWith(TmfXmlStrings.VARIABLE_PREFIX)) {
            /* search the attribute in the map without the fist character $ */
            attribute = getDefinedValue(attribute.substring(1));
        }
        return attribute;
    }

    @Override
    public ITmfStateSystem getStateSystem() {
        return getStateSystemBuilder();
    }

    @Override
    public Iterable<TmfXmlLocation> getLocations() {
        return fLocations;
    }

    @Override
    public void dispose() {
        waitForEmptyQueue();
        for (Map.Entry<String, TmfXmlPatternEventHandler> entry : fFilterEventHandlers.entrySet()) {
            entry.getValue().dispose();
        }
        super.dispose();
    }

    @Override
    public void assignTargetStateSystem(ITmfStateSystemBuilder ssb) {
        super.assignTargetStateSystem(ssb);
    }

    /**
     * Handle the generated synthetic event
     *
     * @param synEvent
     *            The generated synthetic event
     */
    public void handleSyntheticEvent(TmfSyntheticEvent synEvent) {
        fSynEventList.add(synEvent);
        fSyncEventQueue.add(synEvent);
    }

    /**
     * Start a new scenario of this specific FSM
     *
     * @param filterHandlerName
     *            The name of the filter handler
     * @param parentFsmId
     *            The current FSM
     * @param fsmToScheduleList
     *            The list of FSM to start
     * @param event
     *            The current event
     */
    public void startFsm(String filterHandlerName, String parentFsmId, List<String> fsmToScheduleList, ITmfEvent event) {
        TmfXmlPatternEventHandler filterHandler = fFilterEventHandlers.get(filterHandlerName);
        if (filterHandler != null) {
            for (String fsmId : fsmToScheduleList) {
                filterHandler.startScenario(fsmId, NonNullUtils.checkNotNull(parentFsmId), event);
            }
        }
    }

    /**
     * Get the list of synthetic events generated
     *
     * @return The list of synthetic events
     */
    public List<ITmfEvent> getSyntheticEventList() {
        return fSynEventList;
    }
}
