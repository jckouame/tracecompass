package org.eclipse.tracecompass.tmf.analysis.xml.core.model;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.tmf.analysis.xml.core.model.readwrite.TmfXmlReadWriteScenarioStatus;
import org.eclipse.tracecompass.tmf.analysis.xml.core.model.readwrite.TmfXmlReadWriteScenarioStatus.ScenarioStatusType;
import org.eclipse.tracecompass.tmf.analysis.xml.core.module.IXmlStateSystemContainer;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.util.Pair;

/**
 * @since 2.0
 */
public class TmfXmlScenario {
    private final IXmlStateSystemContainer fContainer;
    private final String fScenarioName;
    private final TmfXmlFsm fFsm;
    private final String fFilterHandlerName;
    private ScenarioStatusType fStatus;
    private String fCurrState;
    private @Nullable ITmfEvent fCurrEvent;
    private boolean fNextStateAutomatic;
    private TmfXmlFilterEventHandler fFilterHandler;

    /**
     * Constructor
     *
     * @param name
     *            the name of the scenario
     * @param filterHandler
     *            The filter handler
     * @param fsmId
     *            the id of the fsm executed by this scenario
     * @param container
     *            The state system container this scenario belongs to
     */
    public TmfXmlScenario(String name, TmfXmlFilterEventHandler filterHandler, String fsmId, IXmlStateSystemContainer container) {
        fContainer = container;
        fScenarioName = name;
        fFilterHandler = filterHandler;
        fFilterHandlerName = filterHandler.getName();
        TmfXmlFsm fsm = filterHandler.getFsmMap().get(fsmId);
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
        TmfXmlReadWriteScenarioStatus.updateScenarioPatternDiscoveryEndTime(fCurrEvent, fContainer, fFilterHandlerName, fScenarioName);
        TmfXmlReadWriteScenarioStatus.updateScenario(fCurrEvent, fContainer, fFilterHandlerName, fScenarioName, fCurrState, fStatus);
    }

    /**
     * @param event
     *            The current event
     */
    public void handleEvent(ITmfEvent event) {
        if (fCurrState.compareTo(fFsm.getInitialStateId()) == 0 && fCurrEvent == null) {
            TmfXmlReadWriteScenarioStatus.updateScenario(event, fContainer, fFilterHandlerName, fScenarioName, fCurrState, fStatus);
            TmfXmlReadWriteScenarioStatus.updateFilterNbScenarios(event, fContainer, fFilterHandlerName);
            TmfXmlReadWriteScenarioStatus.updateScenarioPatternDiscoveryStartTime(event, fContainer, fFilterHandlerName, fScenarioName);
        }
        fCurrEvent = event;
        Pair<String, String> out = fFsm.next(event, fFilterHandler.getTransitionInputMap(), fFilterHandlerName, fScenarioName, fCurrState);
        if (!out.getSecond().isEmpty()) {
            String[] actionIds = out.getSecond().split(ISingleAction.ACTION_SEPARATOR);
            for (int i = 0; i < actionIds.length; i++) {
                ISingleAction action = fFilterHandler.getActionMap().get(actionIds[i]);
                if (action != null) {
                    action.execute(event, fFilterHandlerName, fScenarioName, fCurrState, fFsm.getId());
                }
            }
        }

        if (fCurrState.equals(fFsm.getInitialStateId()) && !out.getFirst().equals(fFsm.getInitialStateId())) {
            fFsm.setStartNewScenario(true);
            fFsm.decrementNbScenarioWaitingForStart();
            fStatus = ScenarioStatusType.IN_PROGRESS;
            TmfXmlReadWriteScenarioStatus.updateScenarioPatternDiscoveryStartTime(event, fContainer, fFilterHandlerName, fScenarioName);
        } else if (out.getFirst().equals(fFsm.getAbandonStateId())) {
            fStatus = ScenarioStatusType.ABANDONED;
            TmfXmlReadWriteScenarioStatus.updateScenarioPatternDiscoveryEndTime(event, fContainer, fFilterHandlerName, fScenarioName);
        } else if (out.getFirst().equals(fFsm.getFinalStateId())) {
            fStatus = ScenarioStatusType.MATCHED;
            TmfXmlReadWriteScenarioStatus.updateScenarioPatternDiscoveryEndTime(fCurrEvent, fContainer, fFilterHandlerName, fScenarioName);
            TmfXmlReadWriteScenarioStatus.updateFilterMatchedCount(event, fContainer, fFilterHandlerName, fFsm.getId());
        }

        fNextStateAutomatic = (fCurrState != out.getFirst() && !fCurrState.equals(fFsm.getFinalStateId())) ? true : false;
        fCurrState = out.getFirst();
        TmfXmlReadWriteScenarioStatus.updateScenario(fCurrEvent, fContainer, fFilterHandlerName, fScenarioName, fCurrState, fStatus);
        if (fFsm.getStatesMap().get(fCurrState).isfAutomatic() && fNextStateAutomatic) {
            fNextStateAutomatic = false;
            handleEvent(event);
        }
    }
}
