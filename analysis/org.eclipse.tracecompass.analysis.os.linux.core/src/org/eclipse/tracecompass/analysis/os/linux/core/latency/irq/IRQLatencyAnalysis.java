package org.eclipse.tracecompass.analysis.os.linux.core.latency.irq;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.tracecompass.analysis.os.linux.core.latency.irq.XmlIrqUtils.TYPE;
import org.eclipse.tracecompass.analysis.timing.core.segmentstore.AbstractSegmentStoreAnalysisModule;
import org.eclipse.tracecompass.analysis.timing.core.segmentstore.IAnalysisProgressListener;
import org.eclipse.tracecompass.common.core.NonNullUtils;
import org.eclipse.tracecompass.segmentstore.core.ISegment;
import org.eclipse.tracecompass.segmentstore.core.ISegmentStore;
import org.eclipse.tracecompass.segmentstore.core.treemap.TreeMapStore;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
import org.eclipse.tracecompass.tmf.analysis.xml.core.model.TmfXmlSyntheticEvent;
import org.eclipse.tracecompass.tmf.analysis.xml.core.stateprovider.XmlPatternStateSystemModule;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfAnalysisException;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;

/**
 * @author Jean-Christian Kouame
 * @since 2.0
 *
 */
public class IRQLatencyAnalysis extends AbstractSegmentStoreAnalysisModule {

    /**
     * The ID of this analysis
     */
    public static final String ID = "org.eclipse.tracecompass.analysis.os.linux.core.latency.irq.xml"; //$NON-NLS-1$

    private static final String DATA_FILENAME = "new-irq-latency-analysis.dat"; //$NON-NLS-1$

    @Override
    public String getId() {
        return ID;
    }

    @Override
    protected String getDataFileName() {
        return DATA_FILENAME;
    }

    @Override
    protected Object[] readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
        return checkNotNull((Object[]) ois.readObject());
    }

    @Override
    protected AbstractSegmentStoreAnalysisRequest createAnalysisRequest(ISegmentStore<ISegment> irqStore) {
        return new IRQLatencyAnalysisRequest(irqStore);
    }

    @Override
    protected boolean executeAnalysis(IProgressMonitor monitor) throws TmfAnalysisException {
        ITmfTrace trace = checkNotNull(getTrace());

        /* See if the data file already exists on disk */
        String dir = TmfTraceManager.getSupplementaryFileDir(trace);
        final Path file = Paths.get(dir, getDataFileName());

        if (Files.exists(file)) {
            /* Attempt to read the existing file */
            try (ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(file))) {
                Object[] segmentArray = readObject(ois);
                final ISegmentStore<ISegment> store = new TreeMapStore<>();
                for (Object element : segmentArray) {
                    if (element instanceof ISegment) {
                        ISegment segment = (ISegment) element;
                        store.add(segment);
                    }
                }
                fSegmentStore = store;
                for (IAnalysisProgressListener listener : getListeners()) {
                    listener.onComplete(this, store);
                }
                return true;
            } catch (IOException | ClassNotFoundException | ClassCastException e) {
                /*
                 * We did not manage to read the file successfully, we will just
                 * fall-through to rebuild a new one.
                 */
                try {
                    Files.delete(file);
                } catch (IOException e1) {
                }
            }
        }

        ISegmentStore<ISegment> irqs = new TreeMapStore<>();

        computeEntries(trace, irqs);

        /* The request will fill 'irqs' */
        fSegmentStore = irqs;

        /* Serialize the collections to disk for future usage */
        try (ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(file))) {
            oos.writeObject(irqs.toArray());
        } catch (IOException e) {
            /* Didn't work, oh well. We will just re-read the trace next time */
        }

        for (IAnalysisProgressListener listener : getListeners()) {
            listener.onComplete(this, irqs);
        }

        return true;
    }

    private static void computeEntries(ITmfTrace trace, ISegmentStore<ISegment> IRQs) {
        for (XmlPatternStateSystemModule module : TmfTraceUtils.getAnalysisModulesOfClass(NonNullUtils.checkNotNull(trace), XmlPatternStateSystemModule.class)) {
            module.waitForCompletion();
            for (ITmfStateSystem ssq : module.getStateSystems()) {
                if (ssq == null) {
                    return;
                }
            }
            for (ITmfEvent event : module.getSyntheticEvents()) {
                if (XmlIrqUtils.validateEvent(event, TmfTimestamp.BIG_BANG, TmfTimestamp.BIG_CRUNCH)) {
                    if (event.getName().startsWith(XmlIrqUtils.SOFT_IRQ_PREFIX)) {
                        IRQ irq = new IRQ((TmfXmlSyntheticEvent) event, TYPE.SOFTIRQ);
                        IRQs.add(irq);
                    } else {
                        IRQ irq = new IRQ((TmfXmlSyntheticEvent) event, TYPE.IRQ);
                        IRQs.add(irq);
                    }
                }
            }
        }
    }

    private static class IRQLatencyAnalysisRequest extends AbstractSegmentStoreAnalysisRequest {

        public IRQLatencyAnalysisRequest(ISegmentStore<ISegment> irqs) {
            super(irqs);
        }

        @Override
        public void handleData(final ITmfEvent event) {
        }

        @Override
        public void handleCompleted() {
        }
    }

}
