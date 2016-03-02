/*******************************************************************************
 * Copyright (c) 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.tracecompass.tmf.analysis.xml.core.stateprovider;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.analysis.timing.core.segmentstore.ISegmentListener;
import org.eclipse.tracecompass.common.core.NonNullUtils;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.Activator;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
import org.eclipse.tracecompass.tmf.analysis.xml.core.model.ITmfXmlModelFactory;
import org.eclipse.tracecompass.tmf.analysis.xml.core.model.TmfXmlLocation;
import org.eclipse.tracecompass.tmf.analysis.xml.core.model.TmfXmlPatternEventHandler;
import org.eclipse.tracecompass.tmf.analysis.xml.core.model.readwrite.TmfXmlReadWriteModelFactory;
import org.eclipse.tracecompass.tmf.analysis.xml.core.module.IXmlStateSystemContainer;
import org.eclipse.tracecompass.tmf.analysis.xml.core.module.XmlUtils;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.statesystem.AbstractTmfStateProvider;
import org.eclipse.tracecompass.tmf.core.statesystem.ITmfStateProvider;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * State provider for the pattern analysis
 *
 * @author Jean-Christian Kouame
 * @since 2.0
 *
 */
public class XmlPatternStateProvider extends AbstractTmfStateProvider implements IXmlStateSystemContainer {

    private final IPath fFilePath;

    private final @NonNull String fStateId;

    /** Map for defined values */
    private final Map<String, String> fDefinedValues = new HashMap<>();

    /** List of all Locations */
    private final @NonNull Set<@NonNull TmfXmlLocation> fLocations;

    /** Map for stored values */
    private final Map<String, String> fStoredFields = new HashMap<>();

    private TmfXmlPatternEventHandler fHandler;

    private ISegmentListener fListener;

    /**
     * @param trace
     *            The active trace
     * @param stateid
     *            The state id, which corresponds to the id of the analysis
     *            defined in the XML file
     * @param file
     *            The XML file
     * @param listener
     *            Listener for segment creation
     */
    public XmlPatternStateProvider(@NonNull ITmfTrace trace, @NonNull String stateid, @Nullable IPath file, ISegmentListener listener) {
        super(trace, stateid);
        fStateId = stateid;
        fFilePath = file;
        fListener = listener;
        final String pathString = fFilePath.makeAbsolute().toOSString();
        Element doc = XmlUtils.getElementInFile(pathString, TmfXmlStrings.PATTERN, fStateId);
        if (doc == null) {
            fLocations = new HashSet<>();
            Activator.logError("Failed to find a pattern in " + pathString); //$NON-NLS-1$
            return;
        }

        /* parser for defined Fiels */
        NodeList storedFieldNodes = doc.getElementsByTagName(TmfXmlStrings.STORED_FIELD);
        for (int i = 0; i < storedFieldNodes.getLength(); i++) {
            Element element = (Element) storedFieldNodes.item(i);
            fStoredFields.put(element.getAttribute(TmfXmlStrings.NAME), element.getAttribute(TmfXmlStrings.ID));
        }

        /* parser for defined Values */
        NodeList definedStateNodes = doc.getElementsByTagName(TmfXmlStrings.DEFINED_VALUE);
        for (int i = 0; i < definedStateNodes.getLength(); i++) {
            Element element = (Element) definedStateNodes.item(i);
            fDefinedValues.put(element.getAttribute(TmfXmlStrings.NAME), element.getAttribute(TmfXmlStrings.VALUE));
        }

        ITmfXmlModelFactory modelFactory = TmfXmlReadWriteModelFactory.getInstance();
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
        NodeList nodes = doc.getElementsByTagName(TmfXmlStrings.PATTERN_HANDLER);
        fHandler = modelFactory.createPatternEventHandler(NonNullUtils.checkNotNull((Element) nodes.item(0)), this);
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
     * Get the stored fiels map
     *
     * @return The map of stored fields
     */
    public Map<String, String> getStoredFields() {
        return fStoredFields;
    }

    @Override
    public int getVersion() {
        return 0;
    }

    @Override
    public @NonNull ITmfStateProvider getNewInstance() {
        return new XmlPatternStateProvider(getTrace(), getStateId(), fFilePath, fListener);
    }

    /**
     * Get the state ID of the provider. It corresponds to the analysis ID.
     *
     * @return the state Id
     */
    public @NonNull String getStateId() {
        return fStateId;
    }

    @Override
    public ITmfStateSystem getStateSystem() {
        return getStateSystemBuilder();
    }

    @Override
    public @NonNull Iterable<@NonNull TmfXmlLocation> getLocations() {
        return fLocations;
    }

    @Override
    protected void eventHandle(@NonNull ITmfEvent event) {
        fHandler.handleEvent(event);
    }

    /**
     * Get the listerner for segments creation
     *
     * @return The segment listener
     */
    public ISegmentListener getListener() {
        return fListener;
    }

    @Override
    public void dispose() {
        waitForEmptyQueue();
        fListener.onNewSegment(XmlPatternSegmentStoreModule.END_SEGMENT);
        fHandler.dispose();
        super.dispose();
    }
}
