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
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.Activator;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystemBuilder;
import org.eclipse.tracecompass.statesystem.core.exceptions.AttributeNotFoundException;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateSystemDisposedException;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateValueTypeException;
import org.eclipse.tracecompass.statesystem.core.interval.ITmfStateInterval;
import org.eclipse.tracecompass.statesystem.core.statevalue.ITmfStateValue;
import org.eclipse.tracecompass.statesystem.core.statevalue.TmfStateValue;
import org.eclipse.tracecompass.tmf.analysis.xml.core.module.IXmlStateSystemContainer;
import org.eclipse.tracecompass.tmf.analysis.xml.core.stateprovider.TmfXmlStrings;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.timestamp.ITmfTimestamp;

/**
 * Concrete factory for XML pattern model elements in read write mode
 *
 * @since 2.0
 */
public class TmfXmlReadWriteScenarioStatus {
    /**
    * The string 'status'
    */
    public static final String STATUS = "status"; //$NON-NLS-1$
    /**
     * The string for the status "in_progress"
     */
    public static final String IN_PROGRESS_STRING = "inProgress"; //$NON-NLS-1$
    /**
     * The string for the status "abandoned"
     */
    public static final String ABANDONED_STRING = "abandoned"; //$NON-NLS-1$
    /**
     * The string for the status "matched"
     */
    public static final String MATCHED_STRING = "matched"; //$NON-NLS-1$
    /**
     * The string for the status "waiting for start"
     */
    public static final String WAITING_START_STRING = "waiting_start"; //$NON-NLS-1$
    private static final String START_TIME = "startTime"; //$NON-NLS-1$
    private static final String END_TIME = "endTime"; //$NON-NLS-1$
    private static @Nullable TmfXmlReadWriteScenarioStatus fInstance = null;
    private static final String MATCHED_COUNT = "nbOfMatch"; //$NON-NLS-1$
    /**
     * Path to get all the scenarios
     */
    public static final String[] ALL_SCENARIOS_PATH = new String[] { TmfXmlStrings.PATTERN, "*", TmfXmlStrings.SCENARIOS, "*" }; //$NON-NLS-1$ //$NON-NLS-2$
    /**
     * The string for "state"
     */
    public static final String STATE = "state"; //$NON-NLS-1$
    /**
     * The string for "nbScenarios"
     */
    public static final String SCENARIO_COUNT = "nbScenarios"; //$NON-NLS-1$

    /**
     * All possible types of status for a scenario
     */
    public enum ScenarioStatusType {

        /**
         * scenario waiting for start point
         */
        WAITING_START,
        /**
         * scenario in progress
         */
        IN_PROGRESS,
        /**
         * scenario abandoned
         */
        ABANDONED,
        /**
         * scenario match with the pattern
         */
        MATCHED
    }

    /**
     * The unique instance of this Class
     *
     * @return The unique instance of this class
     */
    public static TmfXmlReadWriteScenarioStatus getInstance() {
        TmfXmlReadWriteScenarioStatus instance = fInstance;
        if (instance == null) {
            instance = new TmfXmlReadWriteScenarioStatus();
            fInstance = instance;
        }
        return instance;
    }

    private static void updateScenarioStatus(@Nullable ITmfEvent event, IXmlStateSystemContainer container, String scenarioName, ScenarioStatusType status) {
        ITmfStateSystemBuilder ss = (ITmfStateSystemBuilder) container.getStateSystem();
        long ts;
        if (event != null) {
            ts = event.getTimestamp().getValue();
        } else {
            ts = ss.getCurrentEndTime();
        }
        ITmfStateValue value;
        try {
            // save the status
            switch (status) {
            case IN_PROGRESS:
                value = TmfStateValue.newValueString(IN_PROGRESS_STRING);
                break;
            case ABANDONED:
                value = TmfStateValue.newValueString(ABANDONED_STRING);
                break;
            case MATCHED:
                value = TmfStateValue.newValueString(MATCHED_STRING);
                break;
            case WAITING_START:
                value = TmfStateValue.newValueString(WAITING_START_STRING);
                break;
            default:
                value = TmfStateValue.nullValue();
                break;
            }
            String[] scenarioStatusPath = new String[] { TmfXmlStrings.PATTERN, TmfXmlStrings.SCENARIOS, scenarioName, STATUS };
            int attributeQuark = ss.getQuarkAbsoluteAndAdd(scenarioStatusPath);
            ss.modifyAttribute(ts, value, attributeQuark);
        } catch (StateValueTypeException | AttributeNotFoundException e) {
            Activator.logError("failed to update scenario status"); //$NON-NLS-1$
        }
    }

    private static void updateScenarioState(final @Nullable ITmfEvent event, final IXmlStateSystemContainer container, final String scenarioName, final String currState) {
        ITmfStateSystemBuilder ss = (ITmfStateSystemBuilder) container.getStateSystem();
        long ts;
        if (event != null) {
            ts = event.getTimestamp().getValue();
        } else {
            ts = ss.getCurrentEndTime();
        }
        try {
            // save the status
            ITmfStateValue value = TmfStateValue.newValueString(currState);
            String[] statePath = new String[] { TmfXmlStrings.PATTERN, TmfXmlStrings.SCENARIOS, scenarioName, TmfXmlStrings.STATE };
            int attributeQuark = ss.getQuarkAbsoluteAndAdd(statePath);
            ss.modifyAttribute(ts, value, attributeQuark);
        } catch (StateValueTypeException | AttributeNotFoundException e) {
            Activator.logError("failed to update scenario state"); //$NON-NLS-1$
        }
    }

    /**
     * Update the start time of the matching process of this scenario
     *
     * @param event
     *            The current event
     * @param container
     *            The state system container this class use
     * @param scenarioName
     *            The name of the current scenario
     */
    public static void updateScenarioPatternDiscoveryStartTime(final ITmfEvent event, final IXmlStateSystemContainer container, final String scenarioName) {
        ITmfStateSystemBuilder ss = (ITmfStateSystemBuilder) container.getStateSystem();
        long ts = event.getTimestamp().normalize(0, ITmfTimestamp.NANOSECOND_SCALE).getValue();
        try {
            // save the status
            ITmfStateValue value = TmfStateValue.newValueLong(ts);
            String[] statePath = new String[] { TmfXmlStrings.PATTERN, TmfXmlStrings.SCENARIOS, scenarioName, START_TIME };
            int attributeQuark = ss.getQuarkAbsoluteAndAdd(statePath);
            ss.modifyAttribute(ts, value, attributeQuark);
        } catch (StateValueTypeException | AttributeNotFoundException e) {
            Activator.logError("failed to update the start time of the scenario " + scenarioName); //$NON-NLS-1$
        }
    }

    /**
     * Update the end time of the matching process of this scenario
     *
     * @param event
     *            The current event
     * @param container
     *            The state system container this class use
     * @param scenarioName
     *            The name of the current scenario
     */
    public static void updateScenarioPatternDiscoveryEndTime(final @Nullable ITmfEvent event, final IXmlStateSystemContainer container, final String scenarioName) {
        ITmfStateSystemBuilder ss = (ITmfStateSystemBuilder) container.getStateSystem();
        long ts;
        if (event == null) {
            ts = ss.getCurrentEndTime();
        } else {
            ts = event.getTimestamp().normalize(0, ITmfTimestamp.NANOSECOND_SCALE).getValue();
        }
        try {
            // save the status
            ITmfStateValue value = TmfStateValue.newValueLong(ts);
            String[] statePath = new String[] { TmfXmlStrings.PATTERN, TmfXmlStrings.SCENARIOS, scenarioName, END_TIME };
            int attributeQuark = ss.getQuarkAbsoluteAndAdd(statePath);
            ss.modifyAttribute(ts, value, attributeQuark);
        } catch (StateValueTypeException | AttributeNotFoundException e) {
            Activator.logError("failed to update the end time of the scenario " + scenarioName); //$NON-NLS-1$
        }
    }

    /**
     * Update the start time of specified state
     *
     * @param event
     *            The current event
     * @param container
     *            The state system container this class use
     * @param patternName
     *            The name of the current pattern
     * @param scenarioName
     *            The name of the current scenario
     * @param stateName
     *            The name of the current state of the scenario
     */
    private static void updateScenarioSpecificStateStartTime(@Nullable ITmfEvent event, IXmlStateSystemContainer container, String scenarioName, String stateName) {
        ITmfStateSystemBuilder ss = (ITmfStateSystemBuilder) container.getStateSystem();
        long ts;
        if (event != null) {
            ts = event.getTimestamp().normalize(0, ITmfTimestamp.NANOSECOND_SCALE).getValue();
        } else {
            ts = ss.getCurrentEndTime();
        }
        String[] startTimePath = new String[] { TmfXmlStrings.PATTERN, TmfXmlStrings.SCENARIOS, scenarioName, TmfXmlStrings.STATE, stateName, START_TIME };
        String[] statePath = new String[] { TmfXmlStrings.PATTERN, TmfXmlStrings.SCENARIOS, scenarioName, TmfXmlStrings.STATE };
        try {
            int stateQuark = ss.getQuarkAbsoluteAndAdd(statePath);
            String ongoingState = ss.queryOngoingState(stateQuark).unboxStr();
            if (ongoingState.compareTo(stateName) != 0) {
                int attributeQuark = ss.getQuarkAbsoluteAndAdd(startTimePath);
                ITmfStateValue value = TmfStateValue.newValueLong(ts);
                ss.modifyAttribute(ts, value, attributeQuark);
            }
        } catch (StateValueTypeException | AttributeNotFoundException e) {
            Activator.logError("failed to update the start time of the state"); //$NON-NLS-1$
        }
    }

    /**
     * Update the count of matched scenarios
     *
     * @param event
     *            The current event
     * @param container
     *            The state system container this class use
     * @param fsmId
     *            The current fsm id
     */
    public static void updatePatternMatchedCount(ITmfEvent event, IXmlStateSystemContainer container, String fsmId) {
        ITmfStateSystemBuilder ss = (ITmfStateSystemBuilder) container.getStateSystem();
        long ts = event.getTimestamp().getValue();
        String[] countPath = new String[] { TmfXmlStrings.PATTERN, fsmId, MATCHED_COUNT };
        try {
            int attributeQuark = ss.getQuarkAbsoluteAndAdd(countPath);
            ss.incrementAttribute(ts, attributeQuark);
        } catch (StateValueTypeException | AttributeNotFoundException e) {
            Activator.logError("failed to update the matched scenarios count"); //$NON-NLS-1$
        }
    }

    /**
     * Update the number of scenarios
     *
     * @param event
     *            The current event
     * @param container
     *            The state system container this class use
     */
    public static void updatePatternNbScenarios(ITmfEvent event, IXmlStateSystemContainer container) {
        ITmfStateSystemBuilder ss = (ITmfStateSystemBuilder) container.getStateSystem();
        long ts = event.getTimestamp().getValue();
        String[] countPath = new String[] { TmfXmlStrings.PATTERN, SCENARIO_COUNT };
        try {
            int attributeQuark = ss.getQuarkAbsoluteAndAdd(countPath);
            ss.incrementAttribute(ts, attributeQuark);
        } catch (StateValueTypeException | AttributeNotFoundException e) {
            Activator.logError("failed to update the number of scenarios"); //$NON-NLS-1$
        }
    }

    /**
     * Get the scenario state start time
     *
     * @param event
     *            The current event
     * @param container
     *            The state system container this class use
     * @param scenarioName
     *            The name of the current scenario
     * @param stateName
     *            The name of the current state of the scenario
     * @return The start time for the specified state
     */
    public long getScenarioSpecificStateStartTime(ITmfEvent event, IXmlStateSystemContainer container, String scenarioName, String stateName) {
        long ts = event.getTimestamp().getValue();
        ITmfStateSystemBuilder ss = (ITmfStateSystemBuilder) container.getStateSystem();
        String[] statePath = new String[] { TmfXmlStrings.PATTERN, TmfXmlStrings.SCENARIOS, scenarioName, TmfXmlStrings.STATE, stateName, START_TIME };
        int attributeQuark = ss.getQuarkAbsoluteAndAdd(statePath);
        try {
            ITmfStateInterval state = ss.querySingleState(ts, attributeQuark);
            return state.getStartTime();
        } catch (AttributeNotFoundException | StateSystemDisposedException e) {
            Activator.logError("failed the start time of the state " + stateName); //$NON-NLS-1$
        }
        return -1l;
    }

    /**
     * Get the scenario matched process start time
     *
     * @param event
     *            The current event
     * @param container
     *            The state system container this class use
     * @param scenarioName
     *            The name of the current scenario
     * @return The start time of the matching process for the specified scenario
     */
    public long getScenarioPatternDiscoveryStartTime(ITmfEvent event, IXmlStateSystemContainer container, String scenarioName) {
        ITmfStateSystemBuilder ss = (ITmfStateSystemBuilder) container.getStateSystem();
        long ts = event.getTimestamp().getValue();
        String[] statePath = new String[] { TmfXmlStrings.PATTERN, TmfXmlStrings.SCENARIOS, scenarioName, START_TIME };
        int attributeQuark = ss.getQuarkAbsoluteAndAdd(statePath);
        try {
            ITmfStateInterval state = ss.querySingleState(ts, attributeQuark);
            return state.getStartTime();
        } catch (AttributeNotFoundException | StateSystemDisposedException e) {
            Activator.logError("failed to get the start time of the scenario " + scenarioName); //$NON-NLS-1$
        }
        return -1L;
    }

    /**
     * Get the scenario matched process end time
     *
     * @param event
     *            The current event
     * @param container
     *            The state system container this class use
     * @param scenarioName
     *            The name of the current scenario
     * @return The end time of the matching process for the specified scenario
     */
    public long getScenarioPatternDiscoveryEndTime(ITmfEvent event, IXmlStateSystemContainer container, String scenarioName) {
        ITmfStateSystemBuilder ss = (ITmfStateSystemBuilder) container.getStateSystem();
        long ts = event.getTimestamp().getValue();
        String[] statePath = new String[] { TmfXmlStrings.PATTERN, TmfXmlStrings.SCENARIOS, scenarioName, END_TIME };
        int attributeQuark = ss.getQuarkAbsoluteAndAdd(statePath);
        try {
            ITmfStateInterval state = ss.querySingleState(ts, attributeQuark);
            return state.getStartTime();
        } catch (AttributeNotFoundException | StateSystemDisposedException e) {
            Activator.logError("failed to get the end time of the scenario"); //$NON-NLS-1$
        }
        return -1L;
    }

    /**
     * Update the scenario internal data
     *
     * @param event
     *            The current event
     * @param container
     *            The state system container this class use
     * @param scenarioName
     *            The name of the current scenario
     * @param stateName
     *            The name of the current state of the scenario
     * @param status
     *            The current status of the specified scenario
     */
    public static void updateScenario(@Nullable ITmfEvent event, IXmlStateSystemContainer container, String scenarioName, String stateName, ScenarioStatusType status) {
        updateScenarioSpecificStateStartTime(event, container, scenarioName, stateName);
        updateScenarioState(event, container, scenarioName, stateName);
        updateScenarioStatus(event, container, scenarioName, status);
    }

    /**
     * Save the special fields
     *
     * @param event
     *            The current event
     * @param container
     *            The state system container this class use
     * @param attributeName
     *            The name of the attribute to save
     * @param value
     *            The value of the attribute to save
     * @param scenarioName
     *            The active scenario name
     */
    public static void saveStoredFields(ITmfEvent event, IXmlStateSystemContainer container, String attributeName, ITmfStateValue value, String scenarioName) {
        ITmfStateSystemBuilder ss = (ITmfStateSystemBuilder) container.getStateSystem();
        long ts = event.getTimestamp().getValue();
        String[] attributePath = new String[] { TmfXmlStrings.PATTERN, TmfXmlStrings.SCENARIOS, scenarioName, TmfXmlStrings.STORED_FIELDS, attributeName };
        int attributeQuark = ss.getQuarkAbsoluteAndAdd(attributePath);
        try {
            ss.modifyAttribute(ts, value, attributeQuark);
        } catch (StateValueTypeException | AttributeNotFoundException e) {
            Activator.logError("failed to save the stored field " + attributeName); //$NON-NLS-1$
        }
    }

    /**
     * Clear the special fields
     *
     * @param event
     *            The current event
     * @param container
     *            The state system container this class use
     * @param attributeName
     *            The name of the attribute to save
     * @param scenarioName
     *            The active scenario name
     */
    public static void clearStoredFields(ITmfEvent event, IXmlStateSystemContainer container, String attributeName, String scenarioName) {
        ITmfStateSystemBuilder ss = (ITmfStateSystemBuilder) container.getStateSystem();
        long ts = event.getTimestamp().getValue();
        String[] attributePath = new String[] { TmfXmlStrings.PATTERN, TmfXmlStrings.SCENARIOS, scenarioName, TmfXmlStrings.STORED_FIELDS, attributeName };
        int attributeQuark = ss.getQuarkAbsoluteAndAdd(attributePath);
        ITmfStateValue value = TmfStateValue.nullValue();
        try {
            ss.modifyAttribute(ts, value, attributeQuark);
        } catch (StateValueTypeException | AttributeNotFoundException e) {
            Activator.logError("failed to clear the stored fields"); //$NON-NLS-1$
        }
    }

    /**
     * Get the value of a special field in the state system
     *
     * @param event
     *            The current event
     * @param container
     *            The state system container this class use
     * @param attributeName
     *            The attribute name of the special field
     * @param scenarioName
     *            The active scenario name
     * @return The value of a special field saved into the state system
     */
    public ITmfStateValue getScenarioStoredFieldValue(ITmfEvent event, IXmlStateSystemContainer container, String attributeName, String scenarioName) {
        ITmfStateSystemBuilder ss = (ITmfStateSystemBuilder) container.getStateSystem();
        long ts = event.getTimestamp().getValue();
        String[] attributePath = new String[] { TmfXmlStrings.PATTERN, TmfXmlStrings.SCENARIOS,scenarioName, TmfXmlStrings.STORED_FIELDS, attributeName };
        int attributeQuark = ss.getQuarkAbsoluteAndAdd(attributePath);
        ITmfStateInterval state = null;
        try {
            state = ss.querySingleState(ts, attributeQuark);
        } catch (AttributeNotFoundException | StateSystemDisposedException e) {
            Activator.logError("failed to get the value of the stored field " + attributeName); //$NON-NLS-1$
        }
        return (state != null) ? NonNullUtils.checkNotNull(state.getStateValue()) : TmfStateValue.nullValue();
    }
}
