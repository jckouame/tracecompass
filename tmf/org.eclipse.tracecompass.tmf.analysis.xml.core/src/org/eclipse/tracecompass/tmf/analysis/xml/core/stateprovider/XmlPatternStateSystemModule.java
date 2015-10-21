/*******************************************************************************
 * Copyright (c) 2015 Ecole Polytechnique de Montreal, Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Jean-Christian Kouame - Initial API and implementation
 ******************************************************************************/
package org.eclipse.tracecompass.tmf.analysis.xml.core.stateprovider;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.common.core.NonNullUtils;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.Activator;
import org.eclipse.tracecompass.segmentstore.core.ISegment;
import org.eclipse.tracecompass.segmentstore.core.ISegmentStore;
import org.eclipse.tracecompass.segmentstore.core.treemap.TreeMapStore;
import org.eclipse.tracecompass.tmf.analysis.xml.core.module.XmlUtils;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.event.TmfSyntheticEvent;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfTraceException;
import org.eclipse.tracecompass.tmf.core.statesystem.ITmfStateProvider;
import org.eclipse.tracecompass.tmf.core.statesystem.TmfStateSystemAnalysisModule;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.w3c.dom.Element;

import com.google.common.collect.ImmutableList;

/**
 * Analysis module for the pattern data-driven state systems, defined in XML.
 *
 * @since 2.0
 *
 */
public class XmlPatternStateSystemModule extends TmfStateSystemAnalysisModule {

    private @Nullable IPath fXmlFile;
    private @NonNull List<ITmfEvent> fSynEventsList = new ArrayList<>();
    private String fSynEventFilename;
    private ISegmentStore<@NonNull ISegment> fSegments = new TreeMapStore<>();

    @Override
    protected StateSystemBackendType getBackendType() {
        return StateSystemBackendType.FULL;
    }

    @Override
    @NonNull
    protected ITmfStateProvider createStateProvider() {
        return new XmlPatternStateProvider(checkNotNull(getTrace()), getId(), fXmlFile);
    }

    @Override
    public String getName() {
        String name = getId();
        IPath xmlFile = fXmlFile;
        if (xmlFile == null) {
            return name;
        }
        Element doc = XmlUtils.getElementInFile(xmlFile.makeAbsolute().toString(), TmfXmlStrings.FILTER, getId());
        /* Label may be available in XML header */
        List<Element> head = XmlUtils.getChildElements(doc, TmfXmlStrings.HEAD);
        if (head.size() == 1) {
            List<Element> labels = XmlUtils.getChildElements(head.get(0), TmfXmlStrings.LABEL);
            if (!labels.isEmpty()) {
                name = labels.get(0).getAttribute(TmfXmlStrings.VALUE);
            }
        }
        return NonNullUtils.checkNotNull(name);
    }

    /**
     * Sets the file path of the XML file containing the state provider
     *
     * @param file
     *            The full path to the XML file
     */
    public void setXmlFile(IPath file) {
        fXmlFile = file;
    }

    /**
     * Get the path to the XML file containing this state provider definition.
     *
     * @return XML file path
     */
    public IPath getXmlFile() {
        return fXmlFile;
    }

    @Override
    protected boolean executeAnalysis(@Nullable final  IProgressMonitor monitor) {
        IProgressMonitor mon = (monitor == null ? new NullProgressMonitor() : monitor);
        final ITmfStateProvider provider = createStateProvider();

        String id = getId();
        boolean writeSynFile = true;

        /* FIXME: State systems should make use of the monitor, to be cancelled */
        try {
            /* Get the state system according to backend */
            StateSystemBackendType backend = getBackendType();
            String directory;
            File htFile = null;
            switch (backend) {
            case FULL:
                directory = TmfTraceManager.getSupplementaryFileDir(getTrace());
                htFile = new File(directory + getSsFileName());
                if (htFile.exists()) {
                    writeSynFile = false;
                }
                createFullHistory(id, provider, htFile);
                break;
            case PARTIAL:
                directory = TmfTraceManager.getSupplementaryFileDir(getTrace());
                htFile = new File(directory + getSsFileName());
                if (htFile.exists()) {
                    writeSynFile = false;
                }
                createPartialHistory(id, provider, htFile);
                break;
            case INMEM:
                createInMemoryHistory(id, provider);
                break;
            case NULL:
                createNullHistory(id, provider);
                break;
            default:
                break;
            }
        } catch (TmfTraceException e) {
            return false;
        }
        readWriteSyntheticEvents(writeSynFile, provider);
        return !mon.isCanceled();
    }

    private void readWriteSyntheticEvents(boolean writeFile, ITmfStateProvider provider) {
        ITmfTrace trace = getTrace();
        if (trace != null) {
            String dir = TmfTraceManager.getSupplementaryFileDir(trace);
            fSynEventFilename = getName() + ".synEvent.dat"; //$NON-NLS-1$
            final Path file = Paths.get(dir, fSynEventFilename);
            if (writeFile) {
                if (Files.exists(file)) {
                    try {
                        Files.delete(file);
                    } catch (IOException e1) {
                    }
                }
                final ImmutableList<ITmfEvent> synEvents = ImmutableList.copyOf(NonNullUtils.checkNotNull(((XmlPatternStateProvider) provider).getSyntheticEventList()));
                fSynEventsList = synEvents;
                for (ITmfEvent event : synEvents) {
                    fSegments.add(NonNullUtils.checkNotNull((TmfSyntheticEvent) event));
                }
                try (ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(file))) {
                    oos.writeObject(fSegments.toArray());
                } catch (IOException e) {
                    Activator.logError("Writing " + fSynEventFilename + " failed", e); //$NON-NLS-1$ //$NON-NLS-2$
                }
            } else if (Files.exists(file)) {

                try (ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(file))) {
                    Object[] segmentArray = checkNotNull((Object[]) ois.readObject());
                    final ISegmentStore<@NonNull ISegment> store = new TreeMapStore<>();
                    for (Object element : segmentArray) {
                        if (element instanceof ISegment) {
                            ISegment segment = (ISegment) element;
                            store.add(segment);
                        }
                    }
                    fSegments = store;
                    List<ITmfEvent> eventList = new ArrayList<>();
                    for (ISegment segment : fSegments) {
                        eventList.add((TmfSyntheticEvent) segment);
                    }
                    fSynEventsList = eventList;
                } catch (IOException | ClassNotFoundException | ClassCastException e) {
                    try {
                        Files.delete(file);
                        Activator.logError("Reading " + fSynEventFilename + " failed", e); //$NON-NLS-1$ //$NON-NLS-2$
                    } catch (IOException e1) {
                    }
                }
            }
        }
    }

    /**
     * @return A list with all the synthetic events
     */
    public @NonNull List<ITmfEvent> getSyntheticEvents() {
            return ImmutableList.copyOf(fSynEventsList);
    }

    /**
     * Get the segments generated by this module
     *
     * @return The segment store representing the generated segments
     */
    public ISegmentStore<@NonNull ISegment> getSegments() {
        return fSegments;
    }
}
