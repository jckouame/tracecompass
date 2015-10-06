package org.eclipse.tracecompass.tmf.analysis.xml.core.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.tmf.analysis.xml.core.module.IXmlStateSystemContainer;
import org.eclipse.tracecompass.tmf.analysis.xml.core.stateprovider.TmfXmlStrings;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * @author Jean-Christian Kouame
 * @since 2.0
 *
 */
public class TmfXmlFilterEventHandler {
    private static final String ROOT_FSM_ID = ":root:"; //$NON-NLS-1$
    /* list of states changes */
    private final String fName;
    private final IXmlStateSystemContainer fParent;

    private final String fInitialFsm;
    private final Map<String, TmfXmlTransitionInput> fTransitionInputMap = new HashMap<>();
    private final Map<String, ISingleAction> fActionMap = new HashMap<>();
    private final Map<String, TmfXmlFsm> fFsmMap = new HashMap<>();
    private final List<TmfXmlFsm> fActiveFsmList = new ArrayList<>();
    private final String REGEX_BARRE_VERTICAL = "\\|"; //$NON-NLS-1$
    /**
     * Empty string
     */
    public static final String EMPTY_STRING = ""; //$NON-NLS-1$

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
    public TmfXmlFilterEventHandler(ITmfXmlModelFactory modelFactory, Element node, IXmlStateSystemContainer parent) {
        fParent = parent;
        String name = node.getAttribute(TmfXmlStrings.FILTER_NAME);
        if (name == null) {
            throw new IllegalArgumentException();
        }
        fName = name;

        NodeList nodesInitialFsm = node.getElementsByTagName(TmfXmlStrings.INITIAL_FSM);
        if (nodesInitialFsm.getLength() != 1) {
            throw new IllegalArgumentException("initial fsm : there should one and only one initial finite state machine element in the xml file."); //$NON-NLS-1$
        }
        String initialFsm = ((Element) nodesInitialFsm.item(0)).getAttribute(TmfXmlStrings.ID);
        if (initialFsm == null) {
            throw new IllegalArgumentException();
        }
        fInitialFsm = initialFsm;

        NodeList nodesTransitionInput = node.getElementsByTagName(TmfXmlStrings.TRANSITION_INPUT);
        /* load transition input */
        for (int i = 0; i < nodesTransitionInput.getLength(); i++) {
            Element element = (Element) nodesTransitionInput.item(i);
            if (element == null) {
                throw new IllegalArgumentException();
            }
            TmfXmlTransitionInput transitionInput = modelFactory.createTransitionInput(element, fParent);
            fTransitionInputMap.put(transitionInput.getId(), transitionInput);
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
        fActionMap.put(ISingleAction.SPECIAL_ACTION_PREFIX + ISingleAction.CLEAR_SPECIAL_FIELD_STRING, new SpecialFieldsClearerAction(fParent));
        fActionMap.put(ISingleAction.SPECIAL_ACTION_PREFIX + ISingleAction.SAVE_SPECIAL_FIELD_STRING, new SpecialFieldsSaverAction(fParent));

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
     *            The id of the fsm to start
     * @param parentFsmId
     *            The id of the current fsm
     * @param event
     *            The current event
     */
    public void startScenario(@Nullable String fsmIds, String parentFsmId, @Nullable ITmfEvent event) {
        if (fsmIds == null) {
            return;
        }
        String fsmToStart[] = fsmIds.split(REGEX_BARRE_VERTICAL);
        for (int i = 0; i < fsmToStart.length; i++) {
            String fsmId = fsmToStart[i];
            TmfXmlFsm fsm = fFsmMap.get(fsmId);
            if (!fActiveFsmList.contains(fsm)) {
                fActiveFsmList.add(fsm);
            }
            fsm.createScenario(parentFsmId, event, this);
        }
    }

    /**
     * @return The name of the filter handler
     */
    public String getName() {
        return fName;
    }

    /**
     * @return The transition inputs
     */
    public Map<String, TmfXmlTransitionInput> getTransitionInputMap() {
        return fTransitionInputMap;
    }

    /**
     * @return The actions
     */
    public Map<String, ISingleAction> getActionMap() {
        return fActionMap;
    }

    /**
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
        for (Map.Entry<String, TmfXmlFsm> entry : fFsmMap.entrySet()) {
            if (entry.getValue().isStartNewScenario()) {
                startScenario(entry.getKey(), ROOT_FSM_ID, null);
            }
        }
        for (TmfXmlFsm fsm : fActiveFsmList) {
            fsm.handleEvent(event, fTransitionInputMap);
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
