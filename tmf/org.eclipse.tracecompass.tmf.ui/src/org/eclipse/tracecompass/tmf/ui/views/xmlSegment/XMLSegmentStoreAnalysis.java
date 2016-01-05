package org.eclipse.tracecompass.tmf.ui.views.xmlSegment;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.analysis.os.linux.core.kernelanalysis.KernelTidAspect;
import org.eclipse.tracecompass.analysis.os.linux.core.latency.SystemCall;
import org.eclipse.tracecompass.analysis.os.linux.core.trace.IKernelAnalysisEventLayout;
import org.eclipse.tracecompass.analysis.os.linux.core.trace.IKernelTrace;
import org.eclipse.tracecompass.analysis.timing.core.segmentstore.AbstractSegmentStoreAnalysisModule;
import org.eclipse.tracecompass.analysis.timing.core.segmentstore.AbstractSegmentStoreAnalysisModule.AbstractSegmentStoreAnalysisRequest;
import org.eclipse.tracecompass.segmentstore.core.ISegment;
import org.eclipse.tracecompass.segmentstore.core.ISegmentStore;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;

public class XMLSegmentStoreAnalysis extends AbstractSegmentStoreAnalysisModule {

    public static final @NonNull String ID = "org.eclipse.tracecompass.tmf.ui.views.xmlSegment.analysis"; //$NON-NLS-1$

    private static final String DATA_FILENAME = "xml-segment-analysis.dat"; //$NON-NLS-1$

    @Override
    protected @NonNull String getDataFileName() {
        // TODO Auto-generated method stub
        return DATA_FILENAME;
    }

    @Override
    protected @NonNull AbstractSegmentStoreAnalysisRequest createAnalysisRequest(@NonNull ISegmentStore<@NonNull ISegment> segmentStore) {
        return null;
    }

    @Override
    protected Object @NonNull [] readObject(@NonNull ObjectInputStream ois) throws ClassNotFoundException, IOException {
        return checkNotNull((Object[]) ois.readObject());
    }

    private static class XmlSegmentAnalysisRequest extends AbstractSegmentStoreAnalysisRequest {

        public XmlSegmentAnalysisRequest(ISegmentStore<@NonNull ISegment> syscalls) {
            super(syscalls);
        }

        @Override
        public void handleData(final ITmfEvent event) {
            super.handleData(event);
            IKernelAnalysisEventLayout layout = fLayout;
            if (layout == null) {
                IKernelTrace trace = checkNotNull((IKernelTrace) event.getTrace());
                layout = trace.getKernelEventLayout();
                fLayout = layout;
            }
            final String eventName = event.getType().getName();

            if (eventName.startsWith(layout.eventSyscallEntryPrefix()) ||
                    eventName.startsWith(layout.eventCompatSyscallEntryPrefix())) {
                /* This is a system call entry event */

                Integer tid = KernelTidAspect.INSTANCE.resolve(event);
                if (tid == null) {
                    // no information on this event/trace ?
                    return;
                }

                /* Record the event's data into the intial system call info */
                // String syscallName = fLayout.getSyscallNameFromEvent(event);
                long startTime = event.getTimestamp().getValue();
                String syscallName = eventName.substring(layout.eventSyscallEntryPrefix().length());

                Map<String, String> args = event.getContent().getFieldNames().stream()
                    .collect(Collectors.toMap(Function.identity(),
                            input -> checkNotNull(event.getContent().getField(input).getValue().toString())));

                SystemCall.InitialInfo newSysCall = new SystemCall.InitialInfo(startTime, checkNotNull(syscallName), checkNotNull(args));
                fOngoingSystemCalls.put(tid, newSysCall);

            } else if (eventName.startsWith(layout.eventSyscallExitPrefix())) {
                /* This is a system call exit event */

                Integer tid = KernelTidAspect.INSTANCE.resolve(event);
                if (tid == null) {
                    return;
                }

                SystemCall.InitialInfo info = fOngoingSystemCalls.remove(tid);
                if (info == null) {
                    /*
                     * We have not seen the entry event corresponding to this
                     * exit (lost event, or before start of trace).
                     */
                    return;
                }

                long endTime = event.getTimestamp().getValue();
                int ret = ((Long) event.getContent().getField("ret").getValue()).intValue(); //$NON-NLS-1$
                ISegment syscall = new SystemCall(info, endTime, ret);
                getSegmentStore().add(syscall);
            }
        }

        @Override
        public void handleCompleted() {
            fOngoingSystemCalls.clear();
        }
    }
}
