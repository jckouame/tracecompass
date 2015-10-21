package org.eclipse.tracecompass.tmf.analysis.xml.core.model.readwrite;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.common.core.NonNullUtils;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystemBuilder;
import org.eclipse.tracecompass.statesystem.core.exceptions.AttributeNotFoundException;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateSystemDisposedException;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateValueTypeException;
import org.eclipse.tracecompass.statesystem.core.interval.ITmfStateInterval;
import org.eclipse.tracecompass.statesystem.core.statevalue.ITmfStateValue;
import org.eclipse.tracecompass.statesystem.core.statevalue.TmfStateValue;
import org.eclipse.tracecompass.tmf.analysis.xml.core.model.Attributes;
import org.eclipse.tracecompass.tmf.analysis.xml.core.model.TmfXmlSyntheticEvent;
import org.eclipse.tracecompass.tmf.analysis.xml.core.module.IXmlStateSystemContainer;
import org.eclipse.tracecompass.tmf.analysis.xml.core.stateprovider.TmfXmlStrings;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;

/**
 * @since 2.0
 */
public class TmfXmlReadWriteScenarioStatus {
    /**
    *
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
    private static final String SCENARIOS = "scenarios"; //$NON-NLS-1$
    private static final String SPECIAL_FIELDS = "specialFields"; //$NON-NLS-1$
    /**
     * Path to get all the scenarios
     */
    public static final String[] ALL_SCENARIOS_PATH = new String[] { Attributes.FILTER, "*", Attributes.SCENARIOS, "*" }; //$NON-NLS-1$ //$NON-NLS-2$
    /**
     * The string for "state"
     */
    public static final String STATE = "state"; //$NON-NLS-1$
    /**
     * The string for "nbScenarios"
     */
    public static final String SCENARIO_COUNT = "nbScenarios"; //$NON-NLS-1$

    /**
     * @author Jean-Christian Kouamé
     */
    public enum ScenarioStatusType {

        /**
         * scenario waiting for start point
         */
        WAITING_START, /**
                        * scenario in progress
                        */
        IN_PROGRESS, /**
                      * scenario abandoned
                      */
        ABANDONED, /**
                    * scenario match with the filter
                    */
        MATCHED
    }

    /**
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

    /**
     * @param event
     *            The current event
     * @param container
     *            The state system container this class use
     * @param filterName
     *            The name of the current filter
     * @param scenarioName
     *            The name of the current scenario
     * @param status
     *            The name of the current status
     * @param currState
     *            The name of the current state of the scenario
     */
    private static void updateScenarioStatus(@Nullable ITmfEvent event, IXmlStateSystemContainer container, String filterName, String scenarioName, ScenarioStatusType status) {
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
            String[] scenarioStatusPath = new String[] { TmfXmlStrings.FILTER, filterName, SCENARIOS, scenarioName, STATUS };
            int attributeQuark = ss.getQuarkAbsoluteAndAdd(scenarioStatusPath);
            ss.modifyAttribute(ts, value, attributeQuark);
        } catch (StateValueTypeException e) {
        } catch (AttributeNotFoundException e) {
        }
    }

    /**
     * @param event
     *            The current event
     * @param container
     *            The state system container this class use
     * @param filterName
     *            The name of the current filter
     * @param scenarioName
     *            The name of the current scenario
     * @param currState
     *            The name of the current state of the scenario
     */
    private static void updateScenarioState(final @Nullable ITmfEvent event, final IXmlStateSystemContainer container, final String filterName, final String scenarioName, final String currState) {
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
            String[] statePath = new String[] { TmfXmlStrings.FILTER, filterName, SCENARIOS, scenarioName, TmfXmlStrings.STATE };
            int attributeQuark = ss.getQuarkAbsoluteAndAdd(statePath);
            ss.modifyAttribute(ts, value, attributeQuark);
        } catch (StateValueTypeException e) {
        } catch (AttributeNotFoundException e) {
        }
    }

    /**
     * @param event
     *            The current event
     * @param container
     *            The state system container this class use
     * @param filterName
     *            The name of the current filter
     * @param scenarioName
     *            The name of the current scenario
     */
    public static void updateScenarioPatternDiscoveryStartTime(final ITmfEvent event, final IXmlStateSystemContainer container, final String filterName, final String scenarioName) {
        ITmfStateSystemBuilder ss = (ITmfStateSystemBuilder) container.getStateSystem();
        long ts = event.getTimestamp().getValue();
        try {
            // save the status
            ITmfStateValue value = TmfStateValue.newValueLong(ts);
            String[] statePath = new String[] { TmfXmlStrings.FILTER, filterName, SCENARIOS, scenarioName, START_TIME };
            int attributeQuark = ss.getQuarkAbsoluteAndAdd(statePath);
            ss.modifyAttribute(ts, value, attributeQuark);
        } catch (StateValueTypeException e) {
        } catch (AttributeNotFoundException e) {
        }
    }

    /**
     * @param event
     *            The current event
     * @param container
     *            The state system container this class use
     * @param filterName
     *            The name of the current filter
     * @param scenarioName
     *            The name of the current scenario
     */
    public static void updateScenarioPatternDiscoveryEndTime(final @Nullable ITmfEvent event, final IXmlStateSystemContainer container, final String filterName, final String scenarioName) {
        ITmfStateSystemBuilder ss = (ITmfStateSystemBuilder) container.getStateSystem();
        long ts;
        if (event == null) {
            ts = ss.getCurrentEndTime();
        } else {
            if (event instanceof TmfXmlSyntheticEvent) {
                ts = ((TmfXmlSyntheticEvent) event).getTimestampEnd().getValue();
            } else {
                ts = event.getTimestamp().getValue();
            }
        }
        try {
            // save the status
            ITmfStateValue value = TmfStateValue.newValueLong(ts);
            String[] statePath = new String[] { TmfXmlStrings.FILTER, filterName, SCENARIOS, scenarioName, END_TIME };
            int attributeQuark = ss.getQuarkAbsoluteAndAdd(statePath);
            ss.modifyAttribute(ts, value, attributeQuark);
        } catch (StateValueTypeException e) {
        } catch (AttributeNotFoundException e) {
        }
    }

    /**
     * @param event
     *            The current event
     * @param container
     *            The state system container this class use
     * @param filterName
     *            The name of the current filter
     * @param scenarioName
     *            The name of the current scenario
     * @param stateName
     *            The name of the current state of the scenario
     */
    private static void updateScenarioSpecificStateStartTime(@Nullable ITmfEvent event, IXmlStateSystemContainer container, String filterName, String scenarioName, String stateName) {
        ITmfStateSystemBuilder ss = (ITmfStateSystemBuilder) container.getStateSystem();
        long ts;
        if (event != null) {
            ts = event.getTimestamp().getValue();
        } else {
            ts = ss.getCurrentEndTime();
        }
        String[] startTimePath = new String[] { TmfXmlStrings.FILTER, filterName, SCENARIOS, scenarioName, TmfXmlStrings.STATE, stateName, START_TIME };
        String[] statePath = new String[] { TmfXmlStrings.FILTER, filterName, SCENARIOS, scenarioName, TmfXmlStrings.STATE };
        try {
            int stateQuark = ss.getQuarkAbsoluteAndAdd(statePath);
            String ongoingState = ss.queryOngoingState(stateQuark).unboxStr();
            if (ongoingState.compareTo(stateName) != 0) {
                int attributeQuark = ss.getQuarkAbsoluteAndAdd(startTimePath);
                ITmfStateValue value = TmfStateValue.newValueLong(ts);
                ss.modifyAttribute(ts, value, attributeQuark);
            }
        } catch (StateValueTypeException e) {
        } catch (AttributeNotFoundException e) {
        }
    }

    /**
     * @param event
     *            The current event
     * @param container
     *            The state system container this class use
     * @param filterName
     *            The name of the current filter
     * @param fsmId
     *            The current fsm id
     */
    public static void updateFilterMatchedCount(ITmfEvent event, IXmlStateSystemContainer container, String filterName, String fsmId) {
        ITmfStateSystemBuilder ss = (ITmfStateSystemBuilder) container.getStateSystem();
        long ts = event.getTimestamp().getValue();
        String[] countPath = new String[] { TmfXmlStrings.FILTER, filterName, fsmId, MATCHED_COUNT };
        try {
            int attributeQuark = ss.getQuarkAbsoluteAndAdd(countPath);
            ss.incrementAttribute(ts, attributeQuark);
        } catch (StateValueTypeException e) {
        } catch (AttributeNotFoundException e) {
        }
    }

    /**
     * @param event
     *            The current event
     * @param container
     *            The state system container this class use
     * @param filterName
     *            The name of the current filter
     */
    public static void updateFilterNbScenarios(ITmfEvent event, IXmlStateSystemContainer container, String filterName) {
        ITmfStateSystemBuilder ss = (ITmfStateSystemBuilder) container.getStateSystem();
        long ts = event.getTimestamp().getValue();
        String[] countPath = new String[] { TmfXmlStrings.FILTER, filterName, SCENARIO_COUNT };
        try {
            int attributeQuark = ss.getQuarkAbsoluteAndAdd(countPath);
            ss.incrementAttribute(ts, attributeQuark);
        } catch (StateValueTypeException e) {
        } catch (AttributeNotFoundException e) {
        }
    }

    /**
     * @param event
     *            The current event
     * @param container
     *            The state system container this class use
     * @param filterName
     *            The name of the current filter
     * @param scenarioName
     *            The name of the current scenario
     * @param stateName
     *            The name of the current state of the scenario
     * @return The start time for the specified state
     */
    public long getScenarioSpecificStateStartTime(ITmfEvent event, IXmlStateSystemContainer container, String filterName, String scenarioName, String stateName) {
        long ts = event.getTimestamp().getValue();
        ITmfStateSystemBuilder ss = (ITmfStateSystemBuilder) container.getStateSystem();
        String[] statePath = new String[] { TmfXmlStrings.FILTER, filterName, SCENARIOS, scenarioName, TmfXmlStrings.STATE, stateName, START_TIME };
        int attributeQuark = ss.getQuarkAbsoluteAndAdd(statePath);
        try {
            ITmfStateInterval state = ss.querySingleState(ts, attributeQuark);
            return state.getStartTime();
        } catch (AttributeNotFoundException e) {
        } catch (StateSystemDisposedException e) {
        }
        return -1l;
    }

    /**
     * @param event
     *            The current event
     * @param container
     *            The state system container this class use
     * @param filterName
     *            The name of the current filter
     * @param scenarioName
     *            The name of the current scenario
     * @return The start time of the matching process for the specified scenario
     */
    public long getScenarioPatternDiscoveryStartTime(ITmfEvent event, IXmlStateSystemContainer container, String filterName, String scenarioName) {
        ITmfStateSystemBuilder ss = (ITmfStateSystemBuilder) container.getStateSystem();
        long ts = event.getTimestamp().getValue();
        String[] statePath = new String[] { TmfXmlStrings.FILTER, filterName, SCENARIOS, scenarioName, START_TIME };
        int attributeQuark = ss.getQuarkAbsoluteAndAdd(statePath);
        try {
            ITmfStateInterval state = ss.querySingleState(ts, attributeQuark);
            return state.getStartTime();
        } catch (StateSystemDisposedException e) {
        } catch (AttributeNotFoundException e) {
        }
        return -1L;
    }

    /**
     * @param event
     *            The current event
     * @param container
     *            The state system container this class use
     * @param filterName
     *            The name of the current filter
     * @param scenarioName
     *            The name of the current scenario
     * @return The end time of the matching process for the specified scenario
     */
    public long getScenarioPatternDiscoveryEndTime(ITmfEvent event, IXmlStateSystemContainer container, String filterName, String scenarioName) {
        ITmfStateSystemBuilder ss = (ITmfStateSystemBuilder) container.getStateSystem();
        long ts = event.getTimestamp().getValue();
        String[] statePath = new String[] { TmfXmlStrings.FILTER, filterName, SCENARIOS, scenarioName, END_TIME };
        int attributeQuark = ss.getQuarkAbsoluteAndAdd(statePath);
        try {
            ITmfStateInterval state = ss.querySingleState(ts, attributeQuark);
            return state.getStartTime();
        } catch (StateSystemDisposedException e) {
        } catch (AttributeNotFoundException e) {
        }
        return -1L;
    }

    /**
     * @param event
     *            The current event
     * @param container
     *            The state system container this class use
     * @param filterName
     *            The name of the current filter
     * @param scenarioName
     *            The name of the current scenario
     * @param stateName
     *            The name of the current state of the scenario
     * @param status
     *            The current status of the specified scenario
     */
    public static void updateScenario(@Nullable ITmfEvent event, IXmlStateSystemContainer container, String filterName, String scenarioName, String stateName, ScenarioStatusType status) {
        updateScenarioSpecificStateStartTime(event, container, filterName, scenarioName, stateName);
        updateScenarioState(event, container, filterName, scenarioName, stateName);
        updateScenarioStatus(event, container, filterName, scenarioName, status);
    }

    /**
     * @param event
     *            The current event
     * @param container
     *            The state system container this class use
     * @param attributeName
     *            The name of the attribute to save
     * @param value
     *            The value of the attribute to save
     * @param args
     *            The arguments necessary to save the data
     */
    public static void saveSpecialFields(ITmfEvent event, IXmlStateSystemContainer container, String attributeName, ITmfStateValue value, String[] args) {
        ITmfStateSystemBuilder ss = (ITmfStateSystemBuilder) container.getStateSystem();
        long ts = event.getTimestamp().getValue();
        String[] attributePath = new String[] { TmfXmlStrings.FILTER, args[0], SCENARIOS, args[1], SPECIAL_FIELDS, attributeName };
        int attributeQuark = ss.getQuarkAbsoluteAndAdd(attributePath);
        try {
            ss.modifyAttribute(ts, value, attributeQuark);
        } catch (StateValueTypeException e) {
        } catch (AttributeNotFoundException e) {
        }
    }

    /**
     * @param event
     *            The current event
     * @param container
     *            The state system container this class use
     * @param attributeName
     *            The name of the attribute to save
     * @param args
     *            The arguments necessary to clear the data
     */
    public static void clearSpecialFields(ITmfEvent event, IXmlStateSystemContainer container, String attributeName, String[] args) {
        ITmfStateSystemBuilder ss = (ITmfStateSystemBuilder) container.getStateSystem();
        long ts = event.getTimestamp().getValue();
        String[] attributePath = new String[] { TmfXmlStrings.FILTER, args[0], SCENARIOS, args[1], SPECIAL_FIELDS, attributeName };
        int attributeQuark = ss.getQuarkAbsoluteAndAdd(attributePath);
        ITmfStateValue value = TmfStateValue.nullValue();
        try {
            ss.modifyAttribute(ts, value, attributeQuark);
        } catch (StateValueTypeException e) {
        } catch (AttributeNotFoundException e) {
        }
    }

    /**
     * @param event
     *            The current event
     * @param container
     *            The state system container this class use
     * @param attributeName
     *            The attribute name of the special field
     * @param args
     *            The arguments necessary to access the value of the special
     *            field
     * @return The value of a special field saved into the state system
     */
    public ITmfStateValue getScenarioSpecialFieldValue(ITmfEvent event, IXmlStateSystemContainer container, String attributeName, String[] args) {
        ITmfStateSystemBuilder ss = (ITmfStateSystemBuilder) container.getStateSystem();
        long ts = event.getTimestamp().getValue();
        String[] attributePath = new String[] { TmfXmlStrings.FILTER, args[0], SCENARIOS, args[1], SPECIAL_FIELDS, attributeName };
        int attributeQuark = ss.getQuarkAbsoluteAndAdd(attributePath);
        ITmfStateInterval state = null;
        try {
            state = ss.querySingleState(ts, attributeQuark);
        } catch (AttributeNotFoundException e) {
        } catch (StateSystemDisposedException e) {
        }
        return (state != null) ? NonNullUtils.checkNotNull(state.getStateValue()) : TmfStateValue.nullValue();
    }
}
