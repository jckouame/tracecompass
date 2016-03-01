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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.common.core.NonNullUtils;
import org.eclipse.tracecompass.tmf.analysis.xml.core.model.TmfXmlReadWriteScenarioStatus.ScenarioStatusType;
import org.eclipse.tracecompass.tmf.analysis.xml.core.module.IXmlStateSystemContainer;
import org.eclipse.tracecompass.tmf.analysis.xml.core.stateprovider.TmfXmlStrings;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.util.Pair;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * This Class implements a state machine (FSM) tree in the XML-defined state
 * system.
 *
 * @author Jean-Christian Kouame
 * @since 2.0
 *
 */
public class TmfXmlFsm {

//    private static final String OTHER_STRING = TmfXmlStrings.CONSTANT_PREFIX + TmfXmlStrings.OTHER;
    private static final String SEPARATOR = "#"; //$NON-NLS-1$
    private final Map<String, TmfXmlState> fStatesMap = new HashMap<>();
    private final List<TmfXmlScenario> fScenarioList = new LinkedList<>();
    private final List<TmfXmlScenario> fNewScenariosList = new ArrayList<>();
    private final List<TmfXmlBasicTransition> fPreconditions = new ArrayList<>();
    private final String fId;
    private final IXmlStateSystemContainer fContainer;
    private final String fFinalStateId;
    private final String fAbandonStateId;
    private final boolean fInstanceMultipleEnabled;
    private boolean fStartNewScenario;
    private String fInitialStateId;
    private String fParentFsmId = ""; //$NON-NLS-1$
    private int fNbScenarioWaitingForStart;
    private int fScenarioCount;

    /**
     * Constructor
     *
     * @param modelFactory
     *            The factory used to create XML model elements
     * @param node
     *            The XML root of this fsm
     * @param parent
     *            The state system container this fsm belongs to
     */
    public TmfXmlFsm(ITmfXmlModelFactory modelFactory, Element node, IXmlStateSystemContainer parent) {
        fContainer = parent;
        String string = node.getAttribute(TmfXmlStrings.ID);
        fId = string;
        fInstanceMultipleEnabled = node.getAttribute(TmfXmlStrings.MULTIPLE).isEmpty() ? true : Boolean.parseBoolean(node.getAttribute(TmfXmlStrings.MULTIPLE));
        fStartNewScenario = false;
        fNbScenarioWaitingForStart = 0;

        string = node.getAttribute(TmfXmlStrings.INITIAL);
        if (string.equals(TmfXmlStrings.NULL)) {
            NodeList nodesInitialState = node.getElementsByTagName(TmfXmlStrings.INITIAL);
            if (nodesInitialState.getLength() != 1) {
                throw new IllegalArgumentException("initial state : there should be one and only one initial state."); //$NON-NLS-1$
            }
            NodeList nodesTransition = ((Element)nodesInitialState.item(0)).getElementsByTagName(TmfXmlStrings.TRANSITION);
            if (nodesInitialState.getLength() != 1) {
                throw new IllegalArgumentException("initial state : there should be one and only one initial state."); //$NON-NLS-1$
            }
            string = ((Element) nodesTransition.item(0)).getAttribute(TmfXmlStrings.TARGET);

            //Get the preconditions
            NodeList nodesPreconditions = ((Element)nodesInitialState.item(0)).getElementsByTagName(TmfXmlStrings.PRECONDITION);
            for (int i = 0; i < nodesPreconditions.getLength(); i++) {
                fPreconditions.add(new TmfXmlBasicTransition(((Element) NonNullUtils.checkNotNull(nodesPreconditions.item(i)))));
            }
        }

        NodeList nodesState = node.getElementsByTagName(TmfXmlStrings.STATE);
        for (int i = 0; i < nodesState.getLength(); i++) {
            Element element = (Element) NonNullUtils.checkNotNull(nodesState.item(i));
            TmfXmlState state = modelFactory.createState(element, fContainer, null);
            fStatesMap.put(state.getId(), state);

            // If the initial state was not already set, we use the first state
            // declared in the fsm description as initial state
            if (i == 0 && string.isEmpty()) {
                string = state.getId();
            }
        }
        fInitialStateId = string;

        NodeList nodesFinalState = node.getElementsByTagName(TmfXmlStrings.FINAL);
        if (nodesFinalState.getLength() > 1) {
            throw new IllegalArgumentException("fsm " + fId + " has more than one final state."); //$NON-NLS-1$ //$NON-NLS-2$
        } else if (nodesFinalState.getLength() != 0) {
            final Element finalElement = NonNullUtils.checkNotNull((Element) nodesFinalState.item(0));
            string = finalElement.getAttribute(TmfXmlStrings.ID);
            fFinalStateId = string;
            if (!fFinalStateId.isEmpty()) {
                TmfXmlState finalState = modelFactory.createState(finalElement, parent, null);
                fStatesMap.put(finalState.getId(), finalState);
            }
        } else {
            fFinalStateId = TmfXmlStrings.NULL;
        }

        NodeList nodesAbandonState = node.getElementsByTagName(TmfXmlStrings.ABANDON_STATE);
        if (nodesAbandonState.getLength() > 1) {
            throw new IllegalArgumentException("abandon state : there should be at most one abandon state in the fsm description."); //$NON-NLS-1$
        } else if (nodesAbandonState.getLength() != 0) {
            final Element abandonElement = NonNullUtils.checkNotNull((Element) nodesAbandonState.item(0));
            string = abandonElement.getAttribute(TmfXmlStrings.ID);
            fAbandonStateId = string;
            if (!fAbandonStateId.isEmpty()) {
                TmfXmlState abandonState = modelFactory.createState(abandonElement, parent, null);
                fStatesMap.put(abandonState.getId(), abandonState);
            }
        } else {
            fAbandonStateId = TmfXmlStrings.NULL;
        }

        //initialize the count of scenarios created from this fsm
        fScenarioCount = 0;

        if (fInitialStateId.isEmpty()) {
            throw new IllegalStateException("No initial state has been declared in fsm " + fId); //$NON-NLS-1$
        }
    }

    /**
     * @return the id of this fsm
     */
    public String getId() {
        return fId;
    }

    /**
     * @return the id of the initial state of this finite state machine
     */
    public String getInitialStateId() {
        return fInitialStateId;
    }

    /**
     * @return the id of the final state of this finite state machine
     */
    public String getFinalStateId() {
        return fFinalStateId;
    }

    /**
     * @return the id of the abandon state of this finite state machine
     */
    public String getAbandonStateId() {
        return fAbandonStateId;
    }

    /**
     * @return The map containing all state definition for this fsm
     */
    public Map<String, TmfXmlState> getStatesMap() {
        return fStatesMap;
    }

    /**
     * @return If we have to start a new scenario for this fsm
     */
    public boolean isStartNewScenario() {
        return fStartNewScenario;
    }

    /**
     * @param startNewScenario
     *            The new value of <b>fStartNewScenario</b>
     */
    public void setStartNewScenario(boolean startNewScenario) {
        this.fStartNewScenario = startNewScenario;
    }

    /**
     * Increment the number of scenarios that are waiting for start
     */
    public void incrementNbScenarioWaitingForStart() {
        fNbScenarioWaitingForStart++;
    }

    /**
     * Decrement the number of scenarios that are waiting for start
     */
    public void decrementNbScenarioWaitingForStart() {
        fNbScenarioWaitingForStart = fNbScenarioWaitingForStart <= 0 ? 0 : --fNbScenarioWaitingForStart;
    }

    /**
     * @param event
     *            The event to process
     * @param tests
     *            The list of possible transitions of the state machine
     * @param scenarioName
     *            The name of the scenario
     * @param activeState
     *            The current state of the state machine
     * @return A pair containing the next state of the state machine and the
     *         action to execute
     */
    public Pair<String, String[]> next(ITmfEvent event, Map<String, TmfXmlTest> tests, String scenarioName, String activeState) {
        boolean matched = false;
        TmfXmlStateTransition stateTransition = null;
        TmfXmlState state = NonNullUtils.checkNotNull(fStatesMap.get(activeState));
        if (event.getName().startsWith("syscall")) {
            toString();
        }
        for (int i = 0; i < state.getTransitionList().size() && !matched; i++) {
//            stateTransition = stateDefinition.getTransitionList().get(i);
//            String[] andInputArray = stateTransition.getTransitionTest();
//
//            boolean inputMatched = true;
//            for (int j = 0; j < andInputArray.length && inputMatched; j++) {
//                boolean singleMatched = false;
//                if (andInputArray[j].equals(OTHER_STRING)) {
//                    singleMatched = true;
//                } else {
//                    TmfXmlTest transitionInput = NonNullUtils.checkNotNull(tests.get(andInputArray[j]));
//                    if (transitionInput.validate(event, scenarioName, currState)) {
//                        singleMatched = true;
//                    }
//                }
//                inputMatched &= singleMatched;
//            }
//            matched = inputMatched;
            stateTransition = state.getTransitionList().get(i);
            matched = stateTransition.validate(event, scenarioName, activeState, tests);
        }
        return getNextStep(stateTransition, matched, activeState);
    }

    private static Pair<String, String[]> getNextStep(@Nullable TmfXmlStateTransition stateTransition, boolean matched, String currState) {
        String nextState = currState;
        String[] actions = new String[]{};
        if (matched && stateTransition != null) {
            nextState = stateTransition.getTarget();
            actions  = stateTransition.getAction();
        }
        return new Pair<>(nextState, actions);
    }

    /**
     * @param event
     *            The current event
     * @param tests
     *            The transition inputs
     * @return True if one of the precondition is validated, false otherwise
     */
    public boolean validatePreconditions(ITmfEvent event, Map<String, TmfXmlTest> tests) {
        if (fPreconditions.isEmpty()) {
            return true;
        }
        for (TmfXmlBasicTransition precondition : fPreconditions) {
            if (precondition.validate(event, TmfXmlStrings.NULL, TmfXmlStrings.NULL, tests)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param event
     *            The current event
     * @param transitionMap
     *            The transitions of the pattern
     */
    public void handleEvent(ITmfEvent event, Map<String, TmfXmlTest> transitionMap) {
        if (!validatePreconditions(event, transitionMap)) {
            return;
        }
        for (TmfXmlScenario scenario : fNewScenariosList) {
            fScenarioList.add(scenario);
        }
        Iterator<TmfXmlScenario> currentItr = fScenarioList.iterator();
        while (currentItr.hasNext()) {
            boolean removed = false;
            TmfXmlScenario scenario = currentItr.next();
            if (scenario.getStatus() == ScenarioStatusType.ABANDONED || scenario.getStatus() == ScenarioStatusType.MATCHED && currentItr.hasNext()) {
                currentItr.remove();
                removed = true;
            }
            if (!removed) {
                handleScenario(scenario, event);
            }
        }
        fNewScenariosList.clear();
    }

    /**
     * Abandon all ongoing scenarios
     */
    public void dispose() {
        for (TmfXmlScenario scenario : fScenarioList) {
            if (scenario.getStatus() == ScenarioStatusType.WAITING_START || scenario.getStatus() == ScenarioStatusType.IN_PROGRESS) {
                scenario.cancel();
            }
        }
    }

    private static void handleScenario(TmfXmlScenario scenario, ITmfEvent event) {
            if (scenario.getStatus() == ScenarioStatusType.WAITING_START || scenario.getStatus() == ScenarioStatusType.IN_PROGRESS) {
                scenario.handleEvent(event);
            }
    }

    /**
     * @param parentFsmId
     *            The id of the parent fsm. If the scenario to start has no
     *            parent, the parent id is <i>root</i>.
     * @param event
     *            The current event, null if not
     * @param eventHandler
     *            The event handler this fsm belongs
     */
    public void createScenario(String parentFsmId, @Nullable ITmfEvent event, TmfXmlPatternEventHandler eventHandler) {
        boolean toStart = false;
        // Check if we have the right to create a new scenario.
        // A new scenario can be created if it will be the first instance of the
        // fsm or if the fsm accepts multiple scenarios running concurrently.
        if (fScenarioList.isEmpty()) {
            toStart = true;
        } else if (fInstanceMultipleEnabled && fNbScenarioWaitingForStart == 0) {
            toStart = true;
        }
        if (toStart) {
            if (fParentFsmId.isEmpty()) {
                fParentFsmId = parentFsmId;
            }
            boolean oldStartNewScenario = fStartNewScenario;
            fStartNewScenario = false;
            TmfXmlScenario scenario = new TmfXmlScenario(fId + SEPARATOR + fScenarioCount, eventHandler, fId, fContainer);
            fScenarioCount++;
            fNewScenariosList.add(scenario);
            // if the scenario is started by the action "startFSM", it is
            // directly executed. No need to wait the next event.
            if (event != null && !oldStartNewScenario) {
                scenario.handleEvent(event);
            }
        }
    }
}
