package org.eclipse.tracecompass.tmf.ui.patternStatus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
import org.eclipse.tracecompass.statesystem.core.StateSystemUtils;
import org.eclipse.tracecompass.statesystem.core.exceptions.AttributeNotFoundException;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateSystemDisposedException;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateValueTypeException;
import org.eclipse.tracecompass.statesystem.core.exceptions.TimeRangeException;
import org.eclipse.tracecompass.statesystem.core.interval.ITmfStateInterval;
import org.eclipse.tracecompass.tmf.analysis.xml.core.model.readwrite.TmfXmlReadWriteScenarioStatus;
import org.eclipse.tracecompass.tmf.analysis.xml.core.stateprovider.XmlPatternStateSystemModule;
import org.eclipse.tracecompass.tmf.core.statesystem.ITmfAnalysisModuleWithStateSystems;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;
import org.eclipse.tracecompass.tmf.ui.patternStatus.PatternStatusEntry.Type;
import org.eclipse.tracecompass.tmf.ui.views.timegraph.AbstractTimeGraphView;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.ITimeEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.ITimeGraphEntry;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.NullTimeEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.TimeEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.TimeGraphEntry;

public class PatternStatusView extends AbstractTimeGraphView {

    /**
     *
     */
    public static @NonNull String ID = "org.eclipse.tracecompass.tmf.ui.patternStatus"; //$NON-NLS-1$
    // Timeout between updates in the build thread in ms
    private static final long BUILD_UPDATE_TIMEOUT = 500;
    private List<ITmfStateSystem> fSsList;
    private Map<String, PatternStatusEntry> scenariosTypesMap = new HashMap<>();

    /**
     *
     */
    public PatternStatusView() {
        super(ID, new PatternStatusPresentationProvider());
    }

    List<ITmfStateSystem> getStateSystems(ITmfTrace trace) {
        List<ITmfStateSystem> ssList = new ArrayList<>();
        for (ITmfAnalysisModuleWithStateSystems module : TmfTraceUtils.getAnalysisModulesOfClass(trace, ITmfAnalysisModuleWithStateSystems.class)) {
            // module.schedule();
            if (module instanceof XmlPatternStateSystemModule) {
                ((XmlPatternStateSystemModule) module).waitForInitialization();
                for (ITmfStateSystem ssq : module.getStateSystems()) {
                    if (ssq == null) {
                        return new ArrayList<>();
                    }
                    ssList.add(ssq);
                }
            }
        }
        return ssList;
    }

    @Override
    protected void buildEventList(@Nullable ITmfTrace trace, ITmfTrace parentTrace, IProgressMonitor monitor) {
        if (trace == null) {
            return;
        }
        //        ITmfStateSystem ssq = TmfStateSystemAnalysisModule.getStateSystem(trace, "id"); //$NON-NLS-1$
        fSsList = getStateSystems(trace);
        for (ITmfStateSystem ssq : fSsList) {
            if (ssq == null) {
                return;
            }
            Comparator<ITimeGraphEntry> comparator = new Comparator<ITimeGraphEntry>() {
                @Override
                public int compare(ITimeGraphEntry o1, ITimeGraphEntry o2) {
                    return ((PatternStatusEntry) o1).compareTo(o2);
                }
            };

            Map<Integer, PatternStatusEntry> entryMap = new HashMap<>();
            TimeGraphEntry traceEntry = null;

            long startTime = ssq.getStartTime();
            long start = startTime;
            setStartTime(Math.min(getStartTime(), startTime));
            boolean complete = false;

            while (!complete) {
                if (monitor.isCanceled()) {
                    return;
                }
                complete = ssq.waitUntilBuilt(BUILD_UPDATE_TIMEOUT);
                if (ssq.isCancelled()) {
                    return;
                }
                long end = ssq.getCurrentEndTime();
                if (start == end && !complete) { // when complete execute one
                                                 // last time regardless of end
                                                 // time
                    continue;
                }
                long endTime = end + 1;
                setEndTime(Math.max(getEndTime(), endTime));

                if (traceEntry == null) {
                    traceEntry = new PatternStatusEntry(trace, trace.getName(), startTime, endTime, 0);
                    traceEntry.sortChildren(comparator);
                    List<TimeGraphEntry> entryList = Collections.singletonList(traceEntry);
                    addToEntryList(parentTrace, entryList);
                } else {
                    traceEntry.updateEndTime(endTime);
                }

                int rootQuark = -1;
                List<Integer> scenariosQuarks = Collections.singletonList(rootQuark);
                int i = 0;
                try {
                    while (i < TmfXmlReadWriteScenarioStatus.ALL_SCENARIOS_PATH.length) {
                        List<Integer> subQuarks = new LinkedList<>();
                        /* Replace * by .* to have a regex string */
                        String name = TmfXmlReadWriteScenarioStatus.ALL_SCENARIOS_PATH[i].replaceAll("\\*", ".*"); //$NON-NLS-1$ //$NON-NLS-2$
                        for (int relativeQuark : scenariosQuarks) {
                            for (int quark : ssq.getSubAttributes(relativeQuark, false, name)) {
                                subQuarks.add(quark);
                            }
                        }
                        scenariosQuarks = subQuarks;
                        i++;
                    }
                } catch (AttributeNotFoundException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                if (traceEntry == null) {
                    throw new NullPointerException();
                }
                for (Integer scenarioQuark : scenariosQuarks) {
                    if (scenarioQuark != rootQuark) {
                        String[] scenarioInfos = ssq.getAttributeName(scenarioQuark).split("#"); //$NON-NLS-1$

                        PatternStatusEntry scenarioTypeEntry;
                        if (scenariosTypesMap.get(scenarioInfos[0]) == null) {
                            scenarioTypeEntry = new PatternStatusEntry(-1, trace, startTime, endTime, scenarioInfos[0], -1, null, Type.SCENARIO_TYPE);
                            scenariosTypesMap.put(scenarioInfos[0], scenarioTypeEntry);
                            traceEntry.addChild(scenarioTypeEntry);
                        } else {
                            scenarioTypeEntry = scenariosTypesMap.get(scenarioInfos[0]);
                        }

                        PatternStatusEntry entry = entryMap.get(scenarioQuark);
                        if (entry == null) {
                            entry = new PatternStatusEntry(scenarioQuark, trace, startTime, endTime, scenarioInfos[0], Integer.parseInt(scenarioInfos[1]), ssq, Type.STATUS);
                            entryMap.put(scenarioQuark, entry);

                            try {
                                int stateQuark = ssq.getQuarkRelative(scenarioQuark, TmfXmlReadWriteScenarioStatus.STATE);
                                if (stateQuark != rootQuark) {
                                    PatternStatusEntry stateEntry = entryMap.get(stateQuark);
                                    if (stateEntry == null) {
                                        stateEntry = new PatternStatusEntry(stateQuark, trace, startTime, endTime, TmfXmlReadWriteScenarioStatus.STATE, 0, ssq, Type.STATE);
                                        entryMap.put(stateQuark, stateEntry);
                                        entry.addChild(stateEntry);
                                    }
                                }
                            } catch (AttributeNotFoundException e) {
                            }

                            scenarioTypeEntry.addChild(entry);
                        }
                    }
                }

                if (parentTrace.equals(getTrace())) {
                    refresh();
                }
                long resolution = Math.max(1, (endTime - ssq.getStartTime()) / getDisplayWidth());
                for (ITimeGraphEntry child : traceEntry.getChildren()) {
                    if (monitor.isCanceled()) {
                        return;
                    }
                    if (child instanceof TimeGraphEntry) {
                        TimeGraphEntry entry = (TimeGraphEntry) child;
                        List<ITimeEvent> eventList = getEventList(entry, start, endTime, resolution, monitor);
                        if (eventList != null) {
                            for (ITimeEvent event : eventList) {
                                entry.addEvent(event);
                            }
                        }

                        for (ITimeGraphEntry subChild : entry.getChildren()) {
                            if (monitor.isCanceled()) {
                                return;
                            }
                            if (subChild instanceof TimeGraphEntry) {
                                TimeGraphEntry subEntry = (TimeGraphEntry) subChild;
                                List<ITimeEvent> subEventList = getEventList(subEntry, start, endTime, resolution, monitor);
                                if (subEventList != null) {
                                    for (ITimeEvent event : subEventList) {
                                        subEntry.addEvent(event);
                                    }
                                }
                            }

                            for (ITimeGraphEntry subSubChild : subChild.getChildren()) {
                                if (monitor.isCanceled()) {
                                    return;
                                }
                                if (subSubChild instanceof TimeGraphEntry) {
                                    TimeGraphEntry subSubEntry = (TimeGraphEntry) subSubChild;
                                    List<ITimeEvent> subSubEventList = getEventList(subSubEntry, start, endTime, resolution, monitor);
                                    if (subSubEventList != null) {
                                        for (ITimeEvent event : subSubEventList) {
                                            subSubEntry.addEvent(event);
                                        }
                                    }
                                }
                            }
                        }
                        redraw();
                    }
                }

                start = end;
            }
        }
    }

    @Override
    protected List<ITimeEvent> getEventList(TimeGraphEntry entry, long startTime, long endTime, long resolution, IProgressMonitor monitor) {
        PatternStatusEntry scenarioStatusEntry = (PatternStatusEntry) entry;
        ITmfStateSystem ssq = scenarioStatusEntry.getSs(); //TmfStateSystemAnalysisModule.getStateSystem(scenarioStatusEntry.getTrace(), "id"); //$NON-NLS-1$
        if (ssq == null) {
            return null;
        }
        final long realStart = Math.max(startTime, ssq.getStartTime());
        final long realEnd = Math.min(endTime, ssq.getCurrentEndTime() + 1);
        if (realEnd <= realStart) {
            return null;
        }
        List<ITimeEvent> eventList = null;
        int quark = scenarioStatusEntry.getQuark();

        try {
            if (scenarioStatusEntry.getType().equals(Type.STATUS)) {
                int statusQuark = ssq.getQuarkRelative(quark, TmfXmlReadWriteScenarioStatus.STATUS);
                List<ITmfStateInterval> statusIntervals = StateSystemUtils.queryHistoryRange(ssq, statusQuark, realStart, realEnd - 1, resolution, monitor);
                eventList = new ArrayList<>(statusIntervals.size());
                long lastEndTime = -1;
                for (ITmfStateInterval statusInterval : statusIntervals) {
                    if (monitor.isCanceled()) {
                        return null;
                    }
                    int status = -1;
                    String stateValue = statusInterval.getStateValue().unboxStr();
                    if (PatternStatusPresentationProvider.statusStates.get(stateValue) != null) {
                        status = PatternStatusPresentationProvider.statusStates.get(stateValue).value;
                    }
                    long time = statusInterval.getStartTime();
                    long duration = statusInterval.getEndTime() - time + 1;
                    if (!statusInterval.getStateValue().isNull()) {
                        if (lastEndTime != time && lastEndTime != -1) {
                            eventList.add(new TimeEvent(entry, lastEndTime, time - lastEndTime));
                        }
                        eventList.add(new TimeEvent(entry, time, duration, status));
                    } else if (lastEndTime == -1 || time + duration >= endTime) {
                        // add null event if it intersects the start or end time
                        eventList.add(new NullTimeEvent(entry, time, duration));
                    }
                    lastEndTime = time + duration;
                }
            }
            if (scenarioStatusEntry.getType().equals(Type.STATE)) {
                List<ITmfStateInterval> stateIntervals = StateSystemUtils.queryHistoryRange(ssq, quark, realStart, realEnd - 1, resolution, monitor);
                eventList = new ArrayList<>(stateIntervals.size());
                long lastEndTime = -1;
                for (ITmfStateInterval stateInterval : stateIntervals) {
                    if (monitor.isCanceled()) {
                        return null;
                    }
                    int status = -1;
                    String stateValue = stateInterval.getStateValue().unboxStr();
                    if (PatternStatusPresentationProvider.getStateIndex().get(stateValue) == null) {
                        PatternStatusPresentationProvider.addNewStateItem(stateValue);
                    }
                    status = PatternStatusPresentationProvider.getStateIndex().get(stateValue);
                    long time = stateInterval.getStartTime();
                    long duration = stateInterval.getEndTime() - time + 1;
                    if (!stateInterval.getStateValue().isNull()) {
                        if (lastEndTime != time && lastEndTime != -1) {
                            eventList.add(new TimeEvent(entry, lastEndTime, time - lastEndTime));
                        }
                        eventList.add(new TimeEvent(entry, time, duration, status));
                    } else if (lastEndTime == -1 || time + duration >= endTime) {
                        // add null event if it intersects the start or end time
                        eventList.add(new NullTimeEvent(entry, time, duration));
                    }
                    lastEndTime = time + duration;
                }
            }
            if (scenarioStatusEntry.getType().equals(Type.SCENARIO_TYPE)) {
                eventList = new ArrayList<>(1);
                eventList.add(new NullTimeEvent(entry, startTime, endTime - startTime + 1));
            }
        } catch (AttributeNotFoundException | TimeRangeException | StateValueTypeException e) {
            e.printStackTrace();
        } catch (StateSystemDisposedException e) {
            /* Ignored */
        }
        return eventList;
    }

//    @Override
//    protected void synchingToTime(long time) {
//        int selected = getSelectionValue(time);
//        if (selected > 0) {
//            for (Object element : getTimeGraphViewer().getExpandedElements()) {
//                if (element instanceof PatternStatusEntry) {
//                    PatternStatusEntry entry = (PatternStatusEntry) element;
//                    if (entry.getThreadId() == selected) {
//                        getTimeGraphCombo().setSelection(entry);
//                        break;
//                    }
//                }
//            }
//        }
//    }


//    private static ITimeGraphEntry checkEntry(ITimeGraphEntry entry, String scenarioName) {
//        int scenarioId = Integer.valueOf(scenarioName.split("#")[1]); //$NON-NLS-1$
//        PatternStatusEntry se = (PatternStatusEntry) entry;
//         if (se.getId() == -1) {
//            for (ITimeGraphEntry child : se.getChildren()) {
//                ITimeGraphEntry scenario = checkEntry(child, scenarioName);
//                if (scenario != null) {
//                    return scenario;
//                }
//            }
//        } else if (scenarioId == se.getId()) {
//            return entry;
//        }
//         return null;
//    }
}
