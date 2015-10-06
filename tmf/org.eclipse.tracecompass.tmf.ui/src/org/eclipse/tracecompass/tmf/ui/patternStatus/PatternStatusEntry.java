package org.eclipse.tracecompass.tmf.ui.patternStatus;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.ITimeGraphEntry;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.TimeGraphEntry;

public class PatternStatusEntry extends TimeGraphEntry implements Comparable<ITimeGraphEntry> {

    /** Type of resource */
    public static enum Type {
        /** Null resources (filler rows, etc.) */
        NULL,
        /** Entries for STATUS */
        STATUS,
        /** Entries for STATE */
        STATE,
        /** Entries for SCENARIO TYPE */
        SCENARIO_TYPE

    }

    private static String ROOT_FILTER = "filter"; //$NON-NLS-1$
    String fScenarioName;
    private int fId;
    private final @NonNull ITmfTrace fTrace;
    private int fQuark;
    private String fFsm;
    private ITmfStateSystem fSs;
    private Type fType;
    /**
     * @param name
     * @param startTime
     * @param endTime
     */
    public PatternStatusEntry(int quark, @NonNull ITmfTrace trace, String name,
            long startTime, long endTime, String fsm, int id, ITmfStateSystem ss, Type type) {
        super(name, startTime, endTime);
        fFsm = fsm;
        fId = id;
        fTrace = trace;
        fQuark = quark;
        fSs = ss;
        fType = type;
        fType.toString();
    }

    /**
     * @param trace
     * @param name
     * @param startTime
     * @param endTime
     * @param id
     */
    public PatternStatusEntry(@NonNull ITmfTrace trace, String name,
            long startTime, long endTime, int id) {
        this(-1, trace, name, startTime, endTime, ROOT_FILTER, id, null, Type.NULL);
    }

    public PatternStatusEntry(Integer quark, @NonNull ITmfTrace trace, long startTime, long endTime, String fsm, int id, ITmfStateSystem ss, Type type) {
        this(quark, trace, fsm + " " + id, startTime, endTime, fsm, id, ss, type); //$NON-NLS-1$
    }

    @Override
    public int compareTo(ITimeGraphEntry other) {
        if (!(other instanceof PatternStatusEntry)) {
            /* Should not happen, but if it does, put those entries at the end */
            return -1;
        }
        PatternStatusEntry o = (PatternStatusEntry) other;

        /*
         * Resources entry names should all be of type "ABC 123"
         *
         * We want to filter on the Type first (the "ABC" part), then on the ID
         * ("123") in numerical order (so we get 1,2,10 and not 1,10,2).
         */
        int ret = this.getFsm().compareTo(o.getFsm());
        if (ret != 0) {
            return ret;
        }
        return Integer.compare(this.getId(), o.getId());
    }

    /**
     * @return
     */
    public ITmfTrace getTrace() {
        return fTrace;
    }

    public String getScenarioName() {
        return fScenarioName;
    }

    public int getId() {
        return fId;
    }

    public int getQuark() {
        return fQuark;
    }

    public String getFsm() {
        return fFsm;
    }

    public ITmfStateSystem getSs() {
        return fSs;
    }

    public Type getType() {
        return fType;
    }
}
