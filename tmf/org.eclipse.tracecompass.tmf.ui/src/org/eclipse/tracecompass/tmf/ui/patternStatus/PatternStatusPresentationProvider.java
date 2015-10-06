package org.eclipse.tracecompass.tmf.ui.patternStatus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.graphics.RGB;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
import org.eclipse.tracecompass.statesystem.core.exceptions.AttributeNotFoundException;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateSystemDisposedException;
import org.eclipse.tracecompass.statesystem.core.interval.ITmfStateInterval;
import org.eclipse.tracecompass.tmf.analysis.xml.core.model.readwrite.TmfXmlReadWriteScenarioStatus;
import org.eclipse.tracecompass.tmf.ui.patternStatus.PatternStatusEntry.Type;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.StateItem;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.TimeGraphPresentationProvider;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.ITimeEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.NullTimeEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.TimeEvent;

public class PatternStatusPresentationProvider extends TimeGraphPresentationProvider {
    enum StatusState {
        WAITING_START       (new RGB(200, 200, 200), 0),
        IN_PROGRESS         (new RGB(  0, 200,   0), 1),
        MATCHED             (new RGB(  0,   0, 200), 2),
        ABANDONED          (new RGB(200,   0, 100), 3);

        public final RGB rgb;
        public final int value;

        private StatusState(RGB rgb, int value) {
            this.rgb = rgb;
            this.value = value;
        }
    }

    private static List<StateItem> stateItemValues = new ArrayList<>();
    /*
     * Maps the value of an event with the corresponding index in the
     * stateValues list
     */
    private static Map<String, Integer> stateIndex = new HashMap<>();

    /**
     *
     */
    public final static Map<String, StatusState> statusStates = new HashMap<>();
    private final String MESSAGE = "(multiple)"; //$NON-NLS-1$
    /**
     *
     */
    public PatternStatusPresentationProvider() {
        statusStates.put(TmfXmlReadWriteScenarioStatus.WAITING_START_STRING, StatusState.WAITING_START);
        statusStates.put(TmfXmlReadWriteScenarioStatus.IN_PROGRESS_STRING, StatusState.IN_PROGRESS);
        statusStates.put(TmfXmlReadWriteScenarioStatus.MATCHED_STRING, StatusState.MATCHED);
        statusStates.put(TmfXmlReadWriteScenarioStatus.ABANDONED_STRING, StatusState.ABANDONED);
    }

    private static StatusState[] getStateValues() {
        return StatusState.values();
    }

    private static StatusState getEventState(TimeEvent event) {
        if (event.hasValue()) {
            int value = event.getValue();
//            FilterStatusEntry entry = (FilterStatusEntry) event.getEntry();
//            if (entry.getType() == Type.STATUS) {
                if (value == StatusState.WAITING_START.value) {
                    return StatusState.WAITING_START;
                } else if (value == StatusState.IN_PROGRESS.value) {
                    return StatusState.IN_PROGRESS;
                } else if (value == StatusState.MATCHED.value) {
                    return StatusState.MATCHED;
                } else if (value == StatusState.ABANDONED.value) {
                    return StatusState.ABANDONED;
                }
//            }
        }
        return null;
    }

    @Override
    public int getStateTableIndex(ITimeEvent event) {
        if (((PatternStatusEntry) event.getEntry()).getType() == Type.STATUS) {
            StatusState state = getEventState((TimeEvent) event);
            if (state != null) {
                return state.ordinal();
            }
        }
        if (((PatternStatusEntry) event.getEntry()).getType() == Type.STATE) {
            if (((TimeEvent)event).hasValue()) {
                return ((TimeEvent)event).getValue() + StatusState.values().length;
            }
        }

        if (event instanceof NullTimeEvent) {
            return INVISIBLE;
        }
        return TRANSPARENT;
    }

    @Override
    public StateItem[] getStateTable() {
        StatusState[] states = getStateValues();
        StateItem[] stateTable = new StateItem[states.length +stateItemValues.size()];
        int i;
        for (i = 0; i < states.length; i++) {
            StatusState state = states[i];
            stateTable[i] = new StateItem(state.rgb, state.toString());
        }
        for (int j=i; j<stateTable.length; j++) {
            stateTable[j] = stateItemValues.get(j-i);
        }
        return stateTable;
    }

    @Override
    public String getEventName(ITimeEvent event) {
        if (((PatternStatusEntry) event.getEntry()).getType() == Type.STATUS) {
            StatusState state = getEventState((TimeEvent) event);
            if (state != null) {
                return state.toString();
            }
        }
        if (((PatternStatusEntry) event.getEntry()).getType() == Type.STATE) {
            if (((TimeEvent)event).hasValue()) {
                return stateItemValues.get(((TimeEvent)event).getValue()).getStateString();
            }
        }
        if (event instanceof NullTimeEvent) {
            return null;
        }
        return MESSAGE;
    }

    private static RGB calcColor(int value) {
        int x = (value * 255) % 1530;
        int r = 0, g = 0, b = 0;
        if (x >= 0 && x < 255) {
            r = 255;
            g = x;
            b = 0;
        }
        if (x >= 255 && x < 510) {
            r = 510 - x;
            g = 255;
            b = 0;
        }
        if (x >= 510 && x < 765) {
            r = 0;
            g = 255;
            b = x - 510;
        }
        if (x >= 765 && x < 1020) {
            r = 0;
            g = 1020 - x;
            b = 255;
        }
        if (x >= 1020 && x < 1275) {
            r = x - 1020;
            g = 0;
            b = 255;
        }
        if (x >= 1275 && x <= 1530) {
            r = 255;
            g = 0;
            b = 1530 - x;
        }
        return new RGB(r, g, b);
    }

    /**
     * @return
     */
    public static Map<String, Integer> getStateIndex() {
        return stateIndex;
    }

    /**
     * @return
     */
    public static List<StateItem> getStateItemValues() {
        return stateItemValues;
    }

    /**
     * @param state
     */
    public static void addNewStateItem(String state) {
        stateIndex.put(state, stateItemValues.size());
        stateItemValues.add(new StateItem(calcColor(stateItemValues.size()), state));
    }

    @Override
    public Map<String, String> getEventHoverToolTipInfo(ITimeEvent event, long hoverTime) {

        Map<String, String> retMap = new LinkedHashMap<>();
        if (event instanceof TimeEvent && ((TimeEvent) event).hasValue()) {

            TimeEvent tcEvent = (TimeEvent) event;
            PatternStatusEntry entry = (PatternStatusEntry) event.getEntry();

            if (tcEvent.hasValue()) {

                ITmfStateSystem ss = entry.getSs();
                if (ss == null) {
                    return new LinkedHashMap<>();
                }
                if (entry.getType() == Type.STATE) {
                    int parentQuark = entry.getQuark();
                    try {
                        ITmfStateInterval stateValue = ss.querySingleState(event.getTime(), parentQuark);
                        String actualState = stateValue.getStateValue().unboxStr();
                        String[] path = new String[]{actualState, "*"}; //$NON-NLS-1$
                        int rootQuark = -1;
                        List<Integer> quarks = Collections.singletonList(rootQuark);
                        int i = 0;
                        try {
                            while (i < path.length) {
                                List<Integer> subQuarks = new LinkedList<>();
                                /* Replace * by .* to have a regex string */
                                String name = path[i].replaceAll("\\*", ".*"); //$NON-NLS-1$ //$NON-NLS-2$
                                for (int relativeQuark : quarks) {
                                    for (int quark : ss.getSubAttributes(relativeQuark, false, name)) {
                                        subQuarks.add(quark);
                                    }
                                }
                                quarks = subQuarks;
                                i++;
                            }
                        } catch (AttributeNotFoundException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        for (Integer quark : quarks) {
                            if (quark != null && quark.intValue() != -1) {
                                ITmfStateInterval value = ss.querySingleState(event.getTime(), quark);
                                if (!value.getStateValue().isNull()) {
                                    String name = ss.getAttributeName(quark);
                                    switch (value.getStateValue().getType()) {
                                    case DOUBLE:
                                        retMap.put(name, String.valueOf(value.getStateValue().unboxDouble()));
                                        break;
                                    case INTEGER:
                                        retMap.put(name, String.valueOf(value.getStateValue().unboxInt()));
                                        break;
                                    case LONG:
                                        retMap.put(name, String.valueOf(value.getStateValue().unboxLong()));
                                        break;
                                    case NULL:
                                        break;
                                    case STRING:
                                        retMap.put(name, value.getStateValue().unboxStr());
                                        break;
                                    default:
                                        break;
                                    }
                                }
                            }
                        }
                    } catch (AttributeNotFoundException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (StateSystemDisposedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
                if (entry.getType() == Type.STATUS) {
                    int parentQuark = entry.getQuark();
                    List<Integer> subQuarks;
                    try {
                        subQuarks = ss.getSubAttributes(parentQuark, false, ".*"); //$NON-NLS-1$
                        for (Integer quark : subQuarks) {
                            if (quark != null && quark.intValue() != -1 && !ss.getAttributeName(quark).equals("state") && !ss.getAttributeName(quark).equals("status")) { //$NON-NLS-1$ //$NON-NLS-2$
                                ITmfStateInterval value = ss.querySingleState(event.getTime(), quark);
                                if (!value.getStateValue().isNull()) {
                                    String name = ss.getAttributeName(quark);
                                    switch (value.getStateValue().getType()) {
                                    case DOUBLE:
                                        retMap.put(name, String.valueOf(value.getStateValue().unboxDouble()));
                                        break;
                                    case INTEGER:
                                        retMap.put(name, String.valueOf(value.getStateValue().unboxInt()));
                                        break;
                                    case LONG:
                                        retMap.put(name, String.valueOf(value.getStateValue().unboxLong()));
                                        break;
                                    case NULL:
                                        break;
                                    case STRING:
                                        retMap.put(name, value.getStateValue().unboxStr());
                                        break;
                                    default:
                                        break;
                                    }
                                }
                            }
                        }
                    } catch (AttributeNotFoundException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (StateSystemDisposedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        }
        return retMap;
    }

}
