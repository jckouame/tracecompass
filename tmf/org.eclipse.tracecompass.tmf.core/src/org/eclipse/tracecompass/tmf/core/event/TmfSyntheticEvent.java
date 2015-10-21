/*******************************************************************************
 * Copyright (c) 2014, 2015 Ecole Polytechnique de Montreal, Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Jean-Christian Kouame - Initial API and implementation
 ******************************************************************************/

package org.eclipse.tracecompass.tmf.core.event;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.common.core.NonNullUtils;
import org.eclipse.tracecompass.segmentstore.core.ISegment;
import org.eclipse.tracecompass.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;

/**
 * This Class implements a Synthetic Event. This type of event has the same
 * behavior as an ITmfEvent and has also a duration and could be used to feed
 * the timing analysis view
 *
 * @since 2.0
 *
 */

public class TmfSyntheticEvent implements ITmfEvent, ISegment {

    /**
     * The serial version UID
     */
    private static final long serialVersionUID = 6525886536169235192L;

    private final TmfXmlEventType fEventType;
    private int fScale;
    private long fStart;
    private long fEnd;

    /**
     * @param start
     *            Start time of the synthetic event
     * @param end
     *            End time of the synthetic event
     * @param scale
     *            Scale of the synthetic event
     * @param eventName
     *            Name of the synthetic event
     * @param fields
     *            Fields of the synthetic event
     */
    public TmfSyntheticEvent(long start, long end, int scale, String eventName, Map<@NonNull String, @NonNull Object> fields) {
        fStart = start;
        fEnd = end;
        fScale = scale;
        fEventType = new TmfXmlEventType(eventName, fields);
    }

    @Override
    public <T> @Nullable T getAdapter(@Nullable Class<T> adapter) {
        return null;
    }

    @Override
    public @NonNull ITmfTrace getTrace() {
        return NonNullUtils.checkNotNull(TmfTraceManager.getInstance().getActiveTrace());
    }

    @Override
    public long getRank() {
        return 0;
    }

    @Override
    public @NonNull ITmfTimestamp getTimestamp() {
        return new TmfTimestamp(fStart, fScale);
    }

    /**
     * Get the end timestamp of this event
     *
     * @return The end timestamp
     */
    public ITmfTimestamp getTimestampEnd() {
        return new TmfTimestamp(fEnd, fScale);
    }

    @Override
    public ITmfEventType getType() {
        return fEventType;
    }

    @Override
    public ITmfEventField getContent() {
        return fEventType.getRootField();
    }

    @Override
    public String getName() {
        return fEventType.getName();
    }

    @Override
    public int compareTo(@Nullable ISegment o) {
        if (o == null) {
            return 1;
        }
        return (int) (getLength() - o.getLength());
    }

    @Override
    public long getStart() {
        return fStart;
    }

    @Override
    public long getEnd() {
        return fEnd;
    }

    private class TmfXmlEventType implements ITmfEventType, Serializable {
        /**
         * serial version UID
         */
        private static final long serialVersionUID = 5822017552601554562L;

        private String fEventName;
        private Map<String, Object> fFields;

        public TmfXmlEventType(String eventName, Map<String, Object> fields) {
            fEventName = eventName;
            fFields = fields;
        }

        @Override
        public String getName() {
            return fEventName;
        }

        @Override
        public ITmfEventField getRootField() {
            final List<ITmfEventField> fieldList = new ArrayList<>();
            for (Map.Entry<@NonNull String, Object> entry : fFields.entrySet()) {
                if (entry.getValue() instanceof Integer) {
                    fieldList.add(new TmfEventField(entry.getKey(), ((Integer) entry.getValue()).intValue(), null));
                } else if (entry.getValue() instanceof Long) {
                    fieldList.add(new TmfEventField(entry.getKey(), ((Long) entry.getValue()).longValue(), null));
                } else if (entry.getValue() instanceof Double) {
                    fieldList.add(new TmfEventField(entry.getKey(), ((Double) entry.getValue()).doubleValue(), null));
                } else if (entry.getValue() instanceof String) {
                    fieldList.add(new TmfEventField(entry.getKey(), entry.getValue(), null));
                }
            }
            ITmfEventField[] fieldTab = new ITmfEventField[fieldList.size()];
            fieldList.toArray(fieldTab);
            return new TmfEventField(ITmfEventField.ROOT_FIELD_ID, null, fieldTab);
        }

        @NonNullByDefault({})
        @Override
        public Collection<String> getFieldNames() {
            return fFields.keySet();
        }
    }
}
