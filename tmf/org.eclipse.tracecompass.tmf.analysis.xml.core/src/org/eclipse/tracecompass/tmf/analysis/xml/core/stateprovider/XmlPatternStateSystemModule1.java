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
import org.eclipse.tracecompass.analysis.timing.core.segmentstore.AbstractSegmentStoreAnalysisModule;
import org.eclipse.tracecompass.analysis.timing.core.segmentstore.IAnalysisProgressListener;
import org.eclipse.tracecompass.common.core.NonNullUtils;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.Activator;
import org.eclipse.tracecompass.segmentstore.core.ISegment;
import org.eclipse.tracecompass.segmentstore.core.ISegmentStore;
import org.eclipse.tracecompass.segmentstore.core.treemap.TreeMapStore;
import org.eclipse.tracecompass.tmf.analysis.xml.core.module.IXmlStateSystemContainer;
import org.eclipse.tracecompass.tmf.analysis.xml.core.module.XmlUtils;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfTraceException;
import org.eclipse.tracecompass.tmf.core.request.ITmfEventRequest;
import org.eclipse.tracecompass.tmf.core.statesystem.ITmfStateProvider;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.w3c.dom.Element;

import com.google.common.collect.ImmutableList;

/**
 * @author Jean-Christian Kouame
 * @since 2.0
 *
 */
public class XmlPatternStateSystemModule1 extends AbstractSegmentStoreAnalysisModule {

    private static final String EXTENSION = ".ht"; //$NON-NLS-1$

    private @Nullable IPath fXmlFile;
    private @NonNull List<ITmfEvent> fSynEventsList = new ArrayList<>();
    private String fSynEventFilename;
    private ISegmentStore<@NonNull ISegment> fSegments = new TreeMapStore<>();

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

    private boolean exA(@Nullable final IProgressMonitor monitor) {
        ITmfTrace trace = checkNotNull(getTrace());

        /* See if the data file already exists on disk */
        String dir = TmfTraceManager.getSupplementaryFileDir(trace);
        final Path file = Paths.get(dir, getDataFileName());

        if (Files.exists(file)) {
            /* Attempt to read the existing file */
            try (ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(file))) {
                Object[] segmentArray = readObject(ois);
                final ISegmentStore<@NonNull ISegment> store = new TreeMapStore<>();
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

        ISegmentStore<ISegment> syscalls = new TreeMapStore<>();

        /* Cancel an ongoing request */
        ITmfEventRequest req = fOngoingRequest;
        if ((req != null) && (!req.isCompleted())) {
            req.cancel();
        }

        /* Create a new request */
        req = createAnalysisRequest(syscalls);
        fOngoingRequest = req;
        trace.sendRequest(req);

        try {
            req.waitForCompletion();
        } catch (InterruptedException e) {
        }

        /* Do not process the results if the request was cancelled */
        if (req.isCancelled() || req.isFailed()) {
            return false;
        }

        /* The request will fill 'syscalls' */
        fSegmentStore = syscalls;

        /* Serialize the collections to disk for future usage */
        try (ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(file))) {
            oos.writeObject(syscalls.toArray());
        } catch (IOException e) {
            /* Didn't work, oh well. We will just re-read the trace next time */
        }

        for (IAnalysisProgressListener listener : getListeners()) {
            listener.onComplete(this, syscalls);
        }

        return true;
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
                    fSegments.add(((SyntheticEvent) event).getSegment());
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
                        eventList.add(((XMLSegment) segment).createSyntheticEvent(NonNullUtils.checkNotNull((IXmlStateSystemContainer) provider)));
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

    @Override
    protected @NonNull String getDataFileName() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected @NonNull AbstractSegmentStoreAnalysisRequest createAnalysisRequest(@NonNull ISegmentStore<@NonNull ISegment> segmentStore) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected Object @NonNull [] readObject(@NonNull ObjectInputStream ois) throws ClassNotFoundException, IOException {
        // TODO Auto-generated method stub
        return null;
    }

    private String getSsFileName() {
        return getId() + EXTENSION;
    }
}
