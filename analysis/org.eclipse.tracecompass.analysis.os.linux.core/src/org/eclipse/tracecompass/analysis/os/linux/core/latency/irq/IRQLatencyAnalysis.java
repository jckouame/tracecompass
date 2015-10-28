/******************************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Jean-Christian Kouame - Initial API and implementation
 *******************************************************************************/
package org.eclipse.tracecompass.analysis.os.linux.core.latency.irq;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.analysis.os.linux.core.latency.irq.XmlIrqUtils.TYPE;
import org.eclipse.tracecompass.analysis.os.linux.core.model.ILatencyAnalysis;
import org.eclipse.tracecompass.analysis.os.linux.core.model.LatencyAnalysisListener;
import org.eclipse.tracecompass.analysis.os.linux.core.trace.IKernelTrace;
import org.eclipse.tracecompass.common.core.NonNullUtils;
import org.eclipse.tracecompass.segmentstore.core.ISegment;
import org.eclipse.tracecompass.segmentstore.core.ISegmentStore;
import org.eclipse.tracecompass.segmentstore.core.treemap.TreeMapStore;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
import org.eclipse.tracecompass.tmf.analysis.xml.core.model.TmfXmlSyntheticEvent;
import org.eclipse.tracecompass.tmf.analysis.xml.core.stateprovider.XmlPatternStateSystemModule;
import org.eclipse.tracecompass.tmf.core.analysis.TmfAbstractAnalysisModule;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfAnalysisException;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;

/**
 * This class execute the IRQ latency analysis
 *
 * @since 2.0
 *
 */
public class IRQLatencyAnalysis extends TmfAbstractAnalysisModule implements ILatencyAnalysis {

    /**
     * The ID of this analysis
     */
    public static final String ID = "org.eclipse.tracecompass.analysis.os.linux.xmlIrqLatency"; //$NON-NLS-1$

    private static final String DATA_FILENAME = "irq-latency-analysis.dat"; //$NON-NLS-1$

    private @Nullable ISegmentStore<ISegment> fIRQs;

    private final Set<LatencyAnalysisListener> fListeners = new HashSet<>();

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public void addListener(LatencyAnalysisListener listener) {
        fListeners.add(listener);
    }

    @Override
    protected boolean executeAnalysis(IProgressMonitor monitor) throws TmfAnalysisException {
        IKernelTrace trace = checkNotNull((IKernelTrace) getTrace());

        /* See if the data file already exists on disk */
        String dir = TmfTraceManager.getSupplementaryFileDir(trace);
        final Path file = Paths.get(dir, DATA_FILENAME);

        if (Files.exists(file)) {
            /* Attempt to read the existing file */
            try (ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(file))) {
                Object[] IRQsArray = (Object[]) ois.readObject();
                final ISegmentStore<ISegment> IRQs = new TreeMapStore<>();
                for (Object element : IRQsArray) {
                    if (element instanceof ISegment) {
                        ISegment segment = (ISegment) element;
                        IRQs.add(segment);
                    }
                }
                fIRQs = IRQs;
                for (LatencyAnalysisListener listener : fListeners) {
                    listener.onComplete(this, IRQs);
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

        ISegmentStore<ISegment> IRQs = new TreeMapStore<>();
        computeEntries(trace, IRQs);
        /* The analysis will fill 'IRQs' */
        fIRQs = IRQs;

        /* Serialize the collections to disk for future usage */
        try (ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(file))) {
            oos.writeObject(fIRQs.toArray());
        } catch (IOException e) {
            /* Didn't work, oh well. We will just re-read the trace next time */
        }

        for (LatencyAnalysisListener listener : fListeners) {
            listener.onComplete(this, IRQs);
        }

        return true;
    }

    @Override
    protected void canceling() {
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

    /**
     * Get the IRQs in a ISegmentStore
     * @return Results from the analysis in a ISegmentStore
     */
    @Override
    public @Nullable ISegmentStore<ISegment> getResults() {
        return fIRQs;
    }
}
