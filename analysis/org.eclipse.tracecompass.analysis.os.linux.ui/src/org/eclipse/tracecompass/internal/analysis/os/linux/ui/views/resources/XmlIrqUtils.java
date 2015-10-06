package org.eclipse.tracecompass.internal.analysis.os.linux.ui.views.resources;

import org.eclipse.tracecompass.common.core.NonNullUtils;
import org.eclipse.tracecompass.tmf.analysis.xml.core.model.TmfXmlSyntheticEvent;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.event.aspect.ITmfEventAspect;
import org.eclipse.tracecompass.tmf.core.event.aspect.TmfContentFieldAspect;
import org.eclipse.tracecompass.tmf.core.timestamp.ITmfTimestamp;

/**
 * @author Jean-Christian Kouame
 *
 */
public class XmlIrqUtils {

    public static final String SOFT_IRQ_PREFIX = "syn/soft"; //$NON-NLS-1$
    public static final String HARD_IRQ_PREFIX = "syn/irq"; //$NON-NLS-1$

    public enum STAT_TYPE {
        NO_DATA, DATA
    }

    public enum TYPE {
        ROOT, IRQ, SOFTIRQ
    }

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
