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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.common.core.NonNullUtils;
import org.eclipse.tracecompass.tmf.analysis.xml.core.module.XmlUtils;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfTraceException;
import org.eclipse.tracecompass.tmf.core.statesystem.ITmfStateProvider;
import org.eclipse.tracecompass.tmf.core.statesystem.TmfStateSystemAnalysisModule;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.w3c.dom.Element;

import com.google.common.collect.ImmutableList;

/**
 * @author Jean-Christian Kouame
 * @since 2.0
 *
 */
public class XmlPatternStateSystemModule extends TmfStateSystemAnalysisModule {

    private @Nullable IPath fXmlFile;
    private final static Map<String, List<ITmfEvent>> fSynEventMap = new HashMap<>();
    private @Nullable ImmutableList<ITmfEvent> fSynEventsList;
    private String fSynEventFilename;

    @Override
    protected StateSystemBackendType getBackendType() {
        return StateSystemBackendType.INMEM;
    }

    @Override
    @NonNull
    protected ITmfStateProvider createStateProvider() {
        return new XmlFilterStateProvider(checkNotNull(getTrace()), getId(), fXmlFile);
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



    /**
     * @return The list of synthetic events
     */
    public List<ITmfEvent> getSynEventList() {
        List<ITmfEvent> synEventsList = new ArrayList<>();
        for (List<ITmfEvent> list :fSynEventMap.values()) {
            synEventsList.addAll(list);
        }
        return synEventsList;
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
                if (!fSynEventMap.containsKey(htFile.getName())) {
                    fSynEventMap.put(htFile.getName(), ((XmlFilterStateProvider) provider).getSyntheticEventList());
                }
                break;
            case PARTIAL:
                directory = TmfTraceManager.getSupplementaryFileDir(getTrace());
                htFile = new File(directory + getSsFileName());
                if (htFile.exists()) {
                    writeSynFile = false;
                }
                createPartialHistory(id, provider, htFile);
                if (!fSynEventMap.containsKey(htFile)) {
                    fSynEventMap.put(htFile.getName(), ((XmlFilterStateProvider) provider).getSyntheticEventList());
                }
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
        loadSynEvent(writeSynFile, provider);
        return !mon.isCanceled();
    }

    private void loadSynEvent(boolean writeFile, ITmfStateProvider provider) {
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
                final ImmutableList<ITmfEvent> synEvents = ImmutableList.copyOf(((XmlFilterStateProvider) provider).getSyntheticEventList());
                fSynEventsList = synEvents;
                try (ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(file))) {
                    System.out.println("writing new file!"); //$NON-NLS-1$
                    oos.writeObject(synEvents);
                } catch (IOException e) {
                    System.err.println("WRITE FAILED : " + e.getMessage()); //$NON-NLS-1$
                }
            } else if (Files.exists(file)) {
                try (ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(file))) {
                    @SuppressWarnings("unchecked")
                    ImmutableList<ITmfEvent> synEvents = (ImmutableList<ITmfEvent>) ois.readObject();
                    fSynEventsList = synEvents;
                } catch (IOException | ClassNotFoundException | ClassCastException e) {
                    try {
                        Files.delete(file);
                        System.err.println("READ FAILED : " + e.getMessage()); //$NON-NLS-1$
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
        if (fSynEventsList != null) {
            return fSynEventsList;
        }
        return new ArrayList<>();

    }
}
