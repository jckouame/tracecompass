/*******************************************************************************
 * Copyright (c) 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.tracecompass.tmf.analysis.xml.core.stateprovider;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.concurrent.CountDownLatch;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.analysis.timing.core.segmentstore.AbstractSegmentStoreAnalysisModule;
import org.eclipse.tracecompass.analysis.timing.core.segmentstore.ISegmentListener;
import org.eclipse.tracecompass.segmentstore.core.ISegment;
import org.eclipse.tracecompass.segmentstore.core.ISegmentStore;
import org.eclipse.tracecompass.segmentstore.core.treemap.TreeMapStore;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfAnalysisException;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;

/**
 * Segment store module for pattern analysis defined in XML. This module will
 * receive all the segments provided by an external source and will build a
 * segment store
 *
 * @author Jean-Christian Kouame
 * @since 2.0
 *
 */
public class XmlPatternSegmentStoreModule extends AbstractSegmentStoreAnalysisModule implements ISegmentListener {

    /**
     * Fake segment indicated that the last segment have been received
     */
    public static final @NonNull EndSegment END_SEGMENT = new EndSegment();
    private static final @NonNull String EXTENSION = ".dat"; //$NON-NLS-1$
    private final ISegmentStore<@NonNull ISegment> fSegments = new TreeMapStore<>();
    private final CountDownLatch fInitialized = new CountDownLatch(1);
    private boolean fInitializationSucceeded;

    @Override
    protected Object @NonNull [] readObject(@NonNull ObjectInputStream ois) throws ClassNotFoundException, IOException {
        return checkNotNull((Object[]) ois.readObject());
    }

    @Override
    protected boolean buildAnalysisSegments(@NonNull ISegmentStore<@NonNull ISegment> segments, @NonNull IProgressMonitor monitor) throws TmfAnalysisException {
        final @Nullable ITmfTrace trace = getTrace();
        if (trace == null) {
            /* This analysis was cancelled in the meantime */
            analysisReady(false);
            return false;
        }
        waitForInitialization();
        segments.addAll(getSegments());
        return true;
    }

    @Override
    protected void canceling() {
        super.cancel();
    }

    @Override
    protected @Nullable String getDataFileName() {
        return getId() + EXTENSION;
    }

    @Override
    public void onNewSegment(@NonNull ISegment segment) {
        // We can accept segments until the first END_SEGMENT arrives. Nothing
        // should be accept after it. This prevents to receive new segments if
        // the analysis that generates the segments is rescheduled
        if (!fInitializationSucceeded) {
            if (segment == END_SEGMENT) {
                analysisReady(true);
                return;
            }
            getSegments().add(segment);
        }
    }

    /**
     * Get the internal segment store of this module
     *
     * @return The segment store
     */
    private synchronized ISegmentStore<@NonNull ISegment> getSegments() {
        return fSegments;
    }

    /**
     * Wait until the module is ready. If all the segments have been received,
     * the initialization succeeded, otherwise it is not.
     *
     * @return True if the initialization succeeded, false otherwise
     */
    public boolean waitForInitialization() {
        try {
            fInitialized.await();
        } catch (InterruptedException e) {
            return false;
        }
        return fInitializationSucceeded;
    }

    /**
     * Make the module available and set whether the initialization succeeded or
     * not. If not, no segment store is available and
     * {@link #waitForInitialization()} should return false.
     *
     * @param success
     *            True if the initialization went well, false otherwise
     */
    private void analysisReady(boolean succeeded) {
        fInitializationSucceeded = succeeded;
        fInitialized.countDown();
    }

    /**
     * Fake segment indicating the build is over, and the segment store is fully
     * filled
     */
    public static class EndSegment implements ISegment {
        /**
         * The serial version UID
         */
        private static final long serialVersionUID = 7834984029618274707L;

        @Override
        public long getStart() {
            return Long.MIN_VALUE;
        }

        @Override
        public long getEnd() {
            return Long.MIN_VALUE;
        }
    }
}
