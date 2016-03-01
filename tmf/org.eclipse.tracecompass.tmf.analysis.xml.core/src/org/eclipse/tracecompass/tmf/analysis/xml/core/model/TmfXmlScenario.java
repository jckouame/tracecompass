/*******************************************************************************
 * Copyright (c) 2016 Ecole Polytechnique de Montreal, Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.tracecompass.tmf.analysis.xml.core.model;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.common.core.NonNullUtils;
import org.eclipse.tracecompass.tmf.analysis.xml.core.model.TmfXmlReadWriteScenarioStatus.ScenarioStatusType;
import org.eclipse.tracecompass.tmf.analysis.xml.core.module.IXmlStateSystemContainer;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.util.Pair;

/**
 * This Class implements a Scenario in the XML-defined state system
 *
 * @since 2.0
 */
public class TmfXmlScenario {
    private final IXmlStateSystemContainer fContainer;
    private final String fScenarioName;
    private final TmfXmlFsm fFsm;
    private ScenarioStatusType fStatus;
    private String fCurrState;
    private @Nullable ITmfEvent fCurrEvent;
    private boolean fNextStateAutomatic;
    private TmfXmlPatternEventHandler fPatternHandler;

    /**
     * Constructor
     *
     * @param name
     *            the name of the scenario
     * @param patternHandler
     *            The filter handler
     * @param fsmId
     *            the id of the fsm executed by this scenario
     * @param container
     *            The state system container this scenario belongs to
     */
    public TmfXmlScenario(String name, TmfXmlPatternEventHandler patternHandler, String fsmId, IXmlStateSystemContainer container) {
        fContainer = container;
        fScenarioName = name;
        fPatternHandler = patternHandler;
        TmfXmlFsm fsm = patternHandler.getFsmMap().get(fsmId);
        if (fsm == null) {
            throw new IllegalArgumentException();
        }
        fFsm = fsm;
        fFsm.incrementNbScenarioWaitingForStart();
        fCurrState = fFsm.getInitialStateId();
        fNextStateAutomatic = false;
        fStatus = ScenarioStatusType.WAITING_START;
    }

    /**
     * Get the status of this scenario
     *
     * @return The status of this scenario
     */
    public ScenarioStatusType getStatus() {
        return fStatus;
    }

    /**
     * Cancel the execution of this scenario
     */
    public void cancel() {
        if (fStatus == ScenarioStatusType.WAITING_START) {
            fFsm.decrementNbScenarioWaitingForStart();
        }
        fStatus = ScenarioStatusType.ABANDONED;
        TmfXmlReadWriteScenarioStatus.updateScenarioPatternDiscoveryEndTime(fCurrEvent, fContainer, fScenarioName);
//        TmfXmlReadWriteScenarioStatus.updateScenario(fCurrEvent, fContainer, fFilterHandlerName, fScenarioName, fCurrState, fStatus);
    }

    /**
     * Handle the ongoing event
     *
     * @param event
     *            The ongoing event
     */
    public void handleEvent(ITmfEvent event) {
        if (fCurrState.compareTo(fFsm.getInitialStateId()) == 0 && fCurrEvent == null) {
//            TmfXmlReadWriteScenarioStatus.updateScenario(event, fContainer, fFilterHandlerName, fScenarioName, fCurrState, fStatus);
            TmfXmlReadWriteScenarioStatus.updatePatternNbScenarios(event, fContainer);
            TmfXmlReadWriteScenarioStatus.updateScenarioPatternDiscoveryStartTime(event, fContainer, fScenarioName);
        }

        fCurrEvent = event;
        Pair<String, String[]> out = fFsm.next(event, fPatternHandler.getTransitionInputMap(), fScenarioName, fCurrState);
        final String[] actions = out.getSecond();
        for (int i = 0; i < actions.length; i++) {
            ISingleAction action = fPatternHandler.getActionMap().get(actions[i]);
            if (action != null) {
                action.execute(event, fScenarioName, fCurrState);
            }
        }

        if (fCurrState.equals(fFsm.getInitialStateId()) && !out.getFirst().equals(fFsm.getInitialStateId())) {
            fFsm.setStartNewScenario(true);
            fFsm.decrementNbScenarioWaitingForStart();
            fStatus = ScenarioStatusType.IN_PROGRESS;
            TmfXmlReadWriteScenarioStatus.updateScenarioPatternDiscoveryStartTime(event, fContainer, fScenarioName);
        } else if (out.getFirst().equals(fFsm.getAbandonStateId())) {
            fStatus = ScenarioStatusType.ABANDONED;
            TmfXmlReadWriteScenarioStatus.updateScenarioPatternDiscoveryEndTime(event, fContainer, fScenarioName);
        } else if (out.getFirst().equals(fFsm.getFinalStateId())) {
            fStatus = ScenarioStatusType.MATCHED;
            TmfXmlReadWriteScenarioStatus.updateScenarioPatternDiscoveryEndTime(fCurrEvent, fContainer, fScenarioName);
            TmfXmlReadWriteScenarioStatus.updatePatternMatchedCount(event, fContainer, fFsm.getId());
        }

        fNextStateAutomatic = (fCurrState != out.getFirst() && !fCurrState.equals(fFsm.getFinalStateId())) ? true : false;
        fCurrState = out.getFirst();
//        TmfXmlReadWriteScenarioStatus.updateScenario(fCurrEvent, fContainer, fFilterHandlerName, fScenarioName, fCurrState, fStatus);
        if (NonNullUtils.checkNotNull(fFsm.getStatesMap().get(fCurrState)).isAutomatic() && fNextStateAutomatic) {
            fNextStateAutomatic = false;
            handleEvent(event);
        }
    }
}
