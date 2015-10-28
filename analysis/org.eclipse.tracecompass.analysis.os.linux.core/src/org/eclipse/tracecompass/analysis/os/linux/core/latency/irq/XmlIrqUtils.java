package org.eclipse.tracecompass.analysis.os.linux.core.latency.irq;

import org.eclipse.tracecompass.common.core.NonNullUtils;
import org.eclipse.tracecompass.tmf.analysis.xml.core.model.TmfXmlSyntheticEvent;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.event.aspect.ITmfEventAspect;
import org.eclipse.tracecompass.tmf.core.event.aspect.TmfContentFieldAspect;
import org.eclipse.tracecompass.tmf.core.timestamp.ITmfTimestamp;

/**
 * This class helps to get general information for interruptions
 * @since 2.0
 *
 */
public class XmlIrqUtils {

    /**
     * The string for the softirq prefix
     */
    public static final String SOFT_IRQ_PREFIX = "syn/soft"; //$NON-NLS-1$
    /**
     * The string for the irq prefix
     */
    public static final String HARD_IRQ_PREFIX = "syn/irq"; //$NON-NLS-1$

    /**
     * Enum for the type of data.
     *
     */
    public enum STAT_TYPE {
        /**
         * No statistic data
         */
        NO_DATA,
        /**
         * Contain statistic data
         */
        DATA
    }

    /**
     * Enum for the type of interruption
     *
     */
    public enum TYPE {
        /**
         * The root
         */
        ROOT,
        /**
         * IRQ
         */
        IRQ,
        /**
         * Softirq
         */
        SOFTIRQ
    }

    /**
     * Get the label of the irq
     *
     * @param event
     *            The synthetic event assiociated to this irq
     * @param type
     *            The type of the irq
     * @return The label of the irq
     */
    public static String getIRQLabel(TmfXmlSyntheticEvent event, TYPE type) {
        if (event == null) {
            return type.toString();
        }

        switch (type) {
        case IRQ:
            TmfContentFieldAspect nameAspect = new TmfContentFieldAspect("name", "name"); //$NON-NLS-1$ //$NON-NLS-2$
            return String.valueOf(nameAspect.resolve(event));
        case SOFTIRQ:
            TmfContentFieldAspect vecAspect = new TmfContentFieldAspect("vec", "vec"); //$NON-NLS-1$ //$NON-NLS-2$
            return SoftIrqLabelProvider.getSoftIrq(NonNullUtils.checkNotNull((Long) vecAspect.resolve(event)).intValue());
        case ROOT:
        default:
            return Messages.SoftIrqLabelProvider_Unknown;
        }
    }

    /**
     * @param event
     *            The current event
     * @param start
     *            The timestamp of the start of the selection
     * @param end
     *            The timestamp of the end of the selection
     * @return True if the event is valid, false otherwise
     */
    public static boolean validateEvent(ITmfEvent event, ITmfTimestamp start, ITmfTimestamp end) {
        if (event instanceof TmfXmlSyntheticEvent &&
                event.getTimestamp().compareTo(start) >= 0 &&
                ((TmfXmlSyntheticEvent) event).getTimestampEnd().compareTo(end) <= 0 &&
                (NonNullUtils.nullToEmptyString(ITmfEventAspect.BaseAspects.EVENT_TYPE.resolve(event)).startsWith(SOFT_IRQ_PREFIX) ||
                        NonNullUtils.nullToEmptyString(ITmfEventAspect.BaseAspects.EVENT_TYPE.resolve(event)).startsWith(HARD_IRQ_PREFIX))) {
            return true;
        }
        return false;
    }
}
