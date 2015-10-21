package org.eclipse.tracecompass.tmf.core.event;

import org.eclipse.tracecompass.tmf.core.timestamp.ITmfTimestamp;

/**
 * @author Jean-Christian Kouame
 * @since 2.0
 *
 */
public interface ITmfSynEvent extends ITmfEvent {
    /**
     * @return the end time of a synthetic event
     */
    public ITmfTimestamp getTimestampEnd();
}
