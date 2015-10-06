package org.eclipse.tracecompass.internal.analysis.os.linux.ui.views.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.common.core.NonNullUtils;
import org.eclipse.tracecompass.internal.analysis.os.linux.ui.views.resources.XmlIrqUtils;
import org.eclipse.tracecompass.internal.analysis.os.linux.ui.views.resources.XmlIrqUtils.TYPE;
import org.eclipse.tracecompass.segmentstore.core.ISegment;
import org.eclipse.tracecompass.tmf.analysis.xml.core.model.TmfXmlSyntheticEvent;
import org.eclipse.tracecompass.tmf.core.event.aspect.TmfContentFieldAspect;

/**
 * @author Jean-Christian Kouame
 * @since 2.0
 *
 */
public class IRQ implements ISegment {
    /**
     *
     */
    private static final long serialVersionUID = 2111306681847732952L;

    public @Nullable TmfXmlSyntheticEvent synIrq;

    public List<IRQ> childs;
    public String label;
    public TYPE type;
    private TmfContentFieldAspect entryAspect = new TmfContentFieldAspect("entry", "entry");
    private TmfContentFieldAspect softExitAspect = new TmfContentFieldAspect("exit", "exit");

    public IRQ(@Nullable TmfXmlSyntheticEvent synEvent, TYPE type) {
        this.type = type;
        synIrq = synEvent;
        childs = new ArrayList<>();
        label = XmlIrqUtils.getIRQLabel(synIrq, this.type);
    }

    @Override
    public int compareTo(ISegment arg0) {
        return 0;
    }

    @Override
    public long getStart() {
        if (synIrq != null) {
            return synIrq.getTimestamp().getValue();
        }
        return -1;
    }

    @Override
    public long getEnd() {
        if (synIrq != null) {
            return synIrq.getTimestampEnd().getValue();
        }
        return -1;
    }

    @Override
    public long getLength() {
        if (synIrq != null) {
            return synIrq.getDuration();
        }
        return -1;
    }

    public long getD1() {
        if (synIrq != null && type == TYPE.SOFTIRQ) {
            return Long.parseLong(NonNullUtils.nullToEmptyString(entryAspect.resolve(synIrq))) - NonNullUtils.checkNotNull(synIrq).getTimestamp().getValue();
        }
        return -1;
    }

    public long getD2() {
        if (synIrq != null) {
            if (type == TYPE.IRQ) {
                return synIrq.getDuration();
            }
            return Long.parseLong(NonNullUtils.nullToEmptyString(softExitAspect.resolve(NonNullUtils.checkNotNull(synIrq))))
                    - Long.parseLong(NonNullUtils.nullToEmptyString(entryAspect.resolve(NonNullUtils.checkNotNull(synIrq))));
        }
        return -1;
    }
}