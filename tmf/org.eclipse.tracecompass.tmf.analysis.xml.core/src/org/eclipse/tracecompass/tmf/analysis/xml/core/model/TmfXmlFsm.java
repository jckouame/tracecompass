package org.eclipse.tracecompass.tmf.analysis.xml.core.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.common.core.NonNullUtils;
import org.eclipse.tracecompass.tmf.analysis.xml.core.model.readwrite.TmfXmlReadWriteScenarioStatus.ScenarioStatusType;
import org.eclipse.tracecompass.tmf.analysis.xml.core.module.IXmlStateSystemContainer;
import org.eclipse.tracecompass.tmf.analysis.xml.core.stateprovider.TmfXmlStrings;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.util.Pair;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * @author Jean-Christian Kouame
 * @since 2.0
 *
 */
public class TmfXmlFsm {

    private boolean fStartNewScenario;
    private final String fId;
    private final IXmlStateSystemContainer fContainer;
    private final String fInitialStateId;
    private final String fFinalStateId;
    private final String fAbandonStateId;
    private String fParentFsmId = ""; //$NON-NLS-1$
    private final boolean fInstanceMultipleEnabled;
    private int fNbScenarioWaitingForStart;
    private final Map<String, TmfXmlStateDefinition> fStatesMap = new HashMap<>();
    private final List<TmfXmlScenario> fScenarioList = new LinkedList<>();
    private final List<TmfXmlScenario> fNewScenariosList = new ArrayList<>();
    private final List<String> fPreconditions = new ArrayList<>();
    private int fScenarioCount;
    private static final String REGEX = ":"; //$NON-NLS-1$
    private static final String SAVED_SPECIAL_FIELDS_ACTION_STRING = ISingleAction.SPECIAL_ACTION_PREFIX + ISingleAction.SAVE_SPECIAL_FIELD_STRING + ISingleAction.ACTION_SEPARATOR;
    private static final String CLEAR_SPECIAL_FIELDS_ACTION_STRINGS = ISingleAction.ACTION_SEPARATOR + ISingleAction.SPECIAL_ACTION_PREFIX + ISingleAction.CLEAR_SPECIAL_FIELD_STRING;
    private static final String OTHER_STRING = TmfXmlStrings.CONSTANT_PREFIX + TmfXmlStrings.OTHER;
    private static final String SEPARATOR = "#"; //$NON-NLS-1$

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
        if (string == null) {
            throw new IllegalArgumentException();
        }
        fId = string;
        fInstanceMultipleEnabled = node.getAttribute(TmfXmlStrings.MULTIPLE).isEmpty() ? true : Boolean.parseBoolean(node.getAttribute(TmfXmlStrings.MULTIPLE));
        fStartNewScenario = false;
        fNbScenarioWaitingForStart = 0;

        NodeList nodesPreconditions = node.getElementsByTagName(TmfXmlStrings.PRECONDITION);
        for (int i = 0; i < nodesPreconditions.getLength(); i++) {
            fPreconditions.add(((Element) nodesPreconditions.item(i)).getAttribute(TmfXmlStrings.INPUT));
        }

        NodeList nodesInitialState = node.getElementsByTagName(TmfXmlStrings.INITIAL_STATE);
        if (nodesInitialState.getLength() != 1) {
            throw new IllegalArgumentException("initial state : there should be one and only one initial state."); //$NON-NLS-1$
        }
        string = ((Element) nodesInitialState.item(0)).getAttribute(TmfXmlStrings.ID);
        if (string == null) {
            throw new IllegalArgumentException();
        }
        fInitialStateId = string;

        NodeList nodesFinalState = node.getElementsByTagName(TmfXmlStrings.END_STATE);
        if (nodesFinalState.getLength() > 1) {
            throw new IllegalArgumentException("final state : there should be one and only one final state."); //$NON-NLS-1$
        } else if (nodesFinalState.getLength() == 1) {
            string = ((Element) nodesFinalState.item(0)).getAttribute(TmfXmlStrings.ID);
            if (string == null) {
                throw new IllegalArgumentException();
            }
            fFinalStateId = string;
        } else {
            fFinalStateId = TmfXmlFilterEventHandler.EMPTY_STRING;
        }

        NodeList nodesAbandonState = node.getElementsByTagName(TmfXmlStrings.ABANDON_STATE);
        if (nodesAbandonState.getLength() > 1) {
            throw new IllegalArgumentException("abandon state : there should be at most one abandon state in the fsm description."); //$NON-NLS-1$
        } else if (nodesAbandonState.getLength() == 1) {
            string = ((Element) nodesAbandonState.item(0)).getAttribute(TmfXmlStrings.ID);
            if (string == null) {
                throw new IllegalArgumentException();
            }
            fAbandonStateId = string;
        } else {
            fAbandonStateId = TmfXmlFilterEventHandler.EMPTY_STRING;
        }

        /* load the state table */
        NodeList nodesStateTable = node.getElementsByTagName(TmfXmlStrings.STATE_TABLE);
        if (nodesStateTable.getLength() != 1) {
            throw new IllegalArgumentException("state table : there should be one and only one state table."); //$NON-NLS-1$
        }
        Element nodeStateTable = ((Element) nodesStateTable.item(0));
        NodeList nodesStateDefinition = nodeStateTable.getElementsByTagName(TmfXmlStrings.STATE_DEFINITION);
        for (int i = 0; i < nodesStateDefinition.getLength(); i++) {
            Element element = (Element) nodesStateDefinition.item(i);
            if (element == null) {
                throw new IllegalArgumentException();
            }
            TmfXmlStateDefinition stateDefinition = modelFactory.createStateDefinition(element, fContainer);
            fStatesMap.put(stateDefinition.getName(), stateDefinition);
        }
        fScenarioCount = 0;
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
    public Map<String, TmfXmlStateDefinition> getStatesMap() {
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
     * @param transitionInputMap
     *            The list of possible transitions of the state machine
     * @param fFilterName
     *            The name of the filter handler
     * @param scenarioName
     *            The name of the scenario
     * @param currState
     *            The current state of the state machine
     * @return A pair containing the next state of the state machine and the
     *         action to execute
     */
    public Pair<String, String> next(ITmfEvent event, Map<String, TmfXmlTransitionInput> transitionInputMap, String fFilterName, String scenarioName, String currState) {
        boolean matched = false;
        TmfXmlStateTransition stateTransition = null;
        TmfXmlStateDefinition stateDefinition = NonNullUtils.checkNotNull(fStatesMap.get(currState));
        for (int i = 0; i < stateDefinition.getfTransitionList().size() && !matched; i++) {
            stateTransition = stateDefinition.getfTransitionList().get(i);
            String input = stateTransition.getfInput();
            String[] andInputArray = input.split(REGEX);

            boolean inputMatched = true;
            for (int j = 0; j < andInputArray.length && inputMatched; j++) {
                boolean singleMatched = false;
                if (andInputArray[j].startsWith(OTHER_STRING)) {
                    singleMatched = true;
                } else {
                    TmfXmlTransitionInput transitionInput = NonNullUtils.checkNotNull(transitionInputMap.get(andInputArray[j]));
                    if (transitionInput.validate(event, fFilterName, scenarioName, currState)) {
                        singleMatched = true;
                    }
                }
                inputMatched &= singleMatched;
            }
            matched = inputMatched;
        }
        return getNextStep(stateTransition, matched, currState);
    }

    private static Pair<String, String> getNextStep(@Nullable TmfXmlStateTransition stateTransition, boolean matched, String currState) {
        String nextState = currState;
        StringBuilder action = new StringBuilder();
        if (matched && stateTransition != null) {
            nextState = stateTransition.getfNext();
            if (stateTransition.isSpecialFieldsTobeSaved()) {
                action.append(SAVED_SPECIAL_FIELDS_ACTION_STRING);
            }
            action.append(stateTransition.getfAction());
            if (stateTransition.isSpecialFieldsToBeCleared()) {
                action.append(CLEAR_SPECIAL_FIELDS_ACTION_STRINGS);
            }
        }
        return new Pair<>(nextState, NonNullUtils.nullToEmptyString(action.toString()));
    }

    /**
     * @param event
     *            The current event
     * @param transitionInputs
     *            The transition inputs
     * @return True if one of the precondition is validated, false otherwise
     */
    public boolean validatePreconditions(ITmfEvent event, Map<String, TmfXmlTransitionInput> transitionInputs) {
        if (fPreconditions.isEmpty()) {
            return true;
        }
        for (String input : fPreconditions) {
            TmfXmlTransitionInput precondition = transitionInputs.get(input);
            if (NonNullUtils.checkNotNull(precondition).validate(event)) {
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
    public void handleEvent(ITmfEvent event, Map<String, TmfXmlTransitionInput> transitionMap) {
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
                handleScenario(scenario, event, transitionMap);
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

    private void handleScenario(TmfXmlScenario scenario, ITmfEvent event, Map<String, TmfXmlTransitionInput> transitionInputMap) {
        if (validatePreconditions(event, transitionInputMap)) {
            if (scenario.getStatus() == ScenarioStatusType.WAITING_START || scenario.getStatus() == ScenarioStatusType.IN_PROGRESS) {
                scenario.handleEvent(event);
            }
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
    public void createScenario(String parentFsmId, @Nullable ITmfEvent event, TmfXmlFilterEventHandler eventHandler) {
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
