/*******************************************************************************
 * Copyright (c) 2016 Ecole Polytechnique de Montreal, Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.tracecompass.tmf.analysis.xml.core.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.common.core.NonNullUtils;
import org.eclipse.tracecompass.tmf.analysis.xml.core.module.IXmlStateSystemContainer;
import org.eclipse.tracecompass.tmf.analysis.xml.core.stateprovider.TmfXmlStrings;
import org.eclipse.tracecompass.tmf.analysis.xml.core.stateprovider.XmlPatternStateProvider;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * This Class implements a pattern handler tree in the XML-defined state
 * system.
 *
 * @author Jean-Christian Kouame
 * @since 2.0
 *
 */
public class TmfXmlPatternEventHandler {
    private static final String ROOT_FSM_ID = ":root:"; //$NON-NLS-1$
    /* list of states changes */
    private final XmlPatternStateProvider fParent;

    private final String[] fInitialFsm;
    private final Map<String, TmfXmlTest> fTestMap = new HashMap<>();
    private final Map<String, ISingleAction> fActionMap = new HashMap<>();
    private final Map<String, TmfXmlFsm> fFsmMap = new HashMap<>();
    private final List<TmfXmlFsm> fActiveFsmList = new ArrayList<>();

    /**
     * Constructor
     *
     * @param modelFactory
     *            The factory used to create XML model elements
     * @param node
     *            The XML root of this event handler
     * @param parent
     *            The state system container this event handler belongs to
     */
    public TmfXmlPatternEventHandler(ITmfXmlModelFactory modelFactory, Element node, IXmlStateSystemContainer parent) {
        fParent = (XmlPatternStateProvider) parent;
        String initialFsm = node.getAttribute(TmfXmlStrings.INITIAL);
        fInitialFsm = initialFsm.isEmpty() ? new String[]{} : initialFsm.split(TmfXmlStrings.AND_SEPARATOR);

        NodeList nodesTest = node.getElementsByTagName(TmfXmlStrings.TEST);
        /* load transition input */
        for (int i = 0; i < nodesTest.getLength(); i++) {
            Element element = (Element) nodesTest.item(i);
            if (element == null) {
                throw new IllegalArgumentException();
            }
            TmfXmlTest test = modelFactory.createTest(element, fParent);
            fTestMap.put(test.getId(), test);
        }

        NodeList nodesAction = node.getElementsByTagName(TmfXmlStrings.ACTION);
        /* load actions */
        for (int i = 0; i < nodesAction.getLength(); i++) {
            Element element = (Element) nodesAction.item(i);
            if (element == null) {
                throw new IllegalArgumentException();
            }
            ISingleAction action = modelFactory.createAction(element, fParent);
            fActionMap.put(((TmfXmlAction) action).getId(), action);
        }
        fActionMap.put(ISingleAction.SPECIAL_ACTION_PREFIX + ISingleAction.CLEAR_STORED_FIELDS_STRING, new StoredFieldsClearerAction(fParent));
        fActionMap.put(ISingleAction.SPECIAL_ACTION_PREFIX + ISingleAction.SAVE_STORED_FIELDS_STRING, new StoredFieldsSaverAction(fParent));

        NodeList nodesFsm = node.getElementsByTagName(TmfXmlStrings.FSM);
        /* load fsm */
        for (int i = 0; i < nodesFsm.getLength(); i++) {
            Element element = (Element) nodesFsm.item(i);
            if (element == null) {
                throw new IllegalArgumentException();
            }
            TmfXmlFsm fsm = modelFactory.createFsm(element, fParent);
            fFsmMap.put(fsm.getId(), fsm);
        }
    }

    /**
     * Start a new scenario for this specific fsm id. If the fsm support only a
     * single instance and this instance already exist, no new scenario is then
     * started. If the scenario is created we handle the current event directly.
     *
     * @param fsmIds
     *            The IDs of the fsm to start
     * @param parentFsmId
     *            The id of the current fsm
     * @param event
     *            The current event
     */
    public void startScenario(String[] fsmIds, String parentFsmId, @Nullable ITmfEvent event) {
        for (int i = 0; i < fsmIds.length; i++) {
            String fsmId = fsmIds[i];
            TmfXmlFsm fsm = NonNullUtils.checkNotNull(fFsmMap.get(fsmId));
            if (!fActiveFsmList.contains(fsm)) {
                fActiveFsmList.add(fsm);
            }
            fsm.createScenario(parentFsmId, event, this);
        }
    }

    /**
     * Get all the defined transition inputs
     *
     * @return The transition inputs
     */
    public Map<String, TmfXmlTest> getTransitionInputMap() {
        return fTestMap;
    }

    /**
     * Get all the defined actions
     *
     * @return The actions
     */
    public Map<String, ISingleAction> getActionMap() {
        return fActionMap;
    }

    /**
     * Get all defined FSMs
     *
     * @return The FSMs
     */
    public Map<String, TmfXmlFsm> getFsmMap() {
        return fFsmMap;
    }

    /**
     * If the filter handler can handle the event, it send the event to all
     * ongoing scenarios
     *
     * @param event
     *            The trace event to handle
     */
    public void handleEvent(ITmfEvent event) {

        if (fActiveFsmList.isEmpty()) {
            startScenario(fInitialFsm, ROOT_FSM_ID, null);
        }
        List<String> fsmToStart = new ArrayList<>();
        for (Map.Entry<String, TmfXmlFsm> entry : fFsmMap.entrySet()) {
            if (entry.getValue().isStartNewScenario()) {
                fsmToStart.add(entry.getKey());
            }
        }
        startScenario(NonNullUtils.checkNotNull(fsmToStart.stream().toArray(String[]::new)), ROOT_FSM_ID, null);
        for (TmfXmlFsm fsm : fActiveFsmList) {
            fsm.handleEvent(event, fTestMap);
        }
    }

    /**
     * Abandon all the ongoing scenarios
     */
    public void dispose() {
        for (TmfXmlFsm fsm : fActiveFsmList) {
            fsm.dispose();
        }
    }
}
