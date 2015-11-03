package org.eclipse.tracecompass.tmf.analysis.xml.core.model;

import java.util.List;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.common.core.NonNullUtils;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
import org.eclipse.tracecompass.tmf.analysis.xml.core.model.readwrite.TmfXmlReadWriteScenarioStatus;
import org.eclipse.tracecompass.tmf.analysis.xml.core.module.IXmlStateSystemContainer;
import org.eclipse.tracecompass.tmf.analysis.xml.core.module.XmlUtils;
import org.eclipse.tracecompass.tmf.analysis.xml.core.stateprovider.TmfXmlStrings;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.timestamp.ITmfTimestamp;
import org.w3c.dom.Element;

/**
 * @author Jean-Christian Kouame
 * @since 2.0
 *
 */
public class TmfXmlTimestampsCondition {

    private enum TimeRangeOperator {
        in, out, none
    }

    private enum ElapsedTimeOperator {
        less, equal, more, none
    }

    IXmlTimestampsCondition fTimestampsCondition;
    IXmlStateSystemContainer fParent;

    /**
     * @param modelFactory
     *            The factory used to create XML model elements
     * @param node
     *            The XML root of this timestamp condition
     * @param container
     *            The state system container this timestamp condition belongs to
     */
    public TmfXmlTimestampsCondition(ITmfXmlModelFactory modelFactory, Element node, IXmlStateSystemContainer container) {
        fParent = container;
        List<@Nullable Element> childElements = XmlUtils.getChildElements(node);
        if (childElements.size() != 1) {
            throw new IllegalArgumentException("Invalid timestampsChecker declaration in XML : Only one timing condition is allowed"); //$NON-NLS-1$
        }
        Element element = childElements.get(0);
        String type = NonNullUtils.checkNotNull(element).getNodeName();
        switch (type) {
        case TmfXmlStrings.TIME_RANGE:
            fTimestampsCondition = new TmfXmlTimeRangeCondition(modelFactory, element, fParent);
            break;
        case TmfXmlStrings.ELAPSED_TIME:
            fTimestampsCondition = new TmfXmlElapsedTimeCondition(modelFactory, element, fParent);
            break;
        default:
            throw new IllegalArgumentException("Invalid timestampsChecker declaration in XML : Type should be timeRange or elapsedTime"); //$NON-NLS-1$
        }
    }

    /**
     * @param timestamp
     *            The timestamp value
     * @param unit
     *            The initial unit of the timestamp
     * @return The value of the timestamp in nanoseconds
     */
    public static long valueToNanoseconds(long timestamp, String unit) {
        switch (unit) {
        case TmfXmlStrings.NS:
            return timestamp;
        case TmfXmlStrings.MS:
            return timestamp * 1000l;
        case TmfXmlStrings.S:
            return timestamp * 1000000l;
        case TmfXmlStrings.MIN:
            return timestamp * (60l * 1000000l);
        default:
            throw new IllegalArgumentException("The time unit is not yet supporting."); //$NON-NLS-1$
        }
    }

    /**
     * @param event
     *            The current event
     * @param arg
     *            The arguments necessary for the test
     * @return True if the test succeed, false otherwise
     */
    public boolean testForEvent(ITmfEvent event, String... arg) {
        return fTimestampsCondition.testForEvent(event, arg);
    }

    private interface IXmlTimestampsCondition {
        boolean testForEvent(ITmfEvent event, String... arg);
    }

    private class TmfXmlTimeRangeCondition implements IXmlTimestampsCondition {

        TimeRangeOperator fType = TimeRangeOperator.none;
        String fUnit;
        String fBegin;
        String fEnd;
        IXmlStateSystemContainer fContainer;

        /**
         * @param modelFactory
         *            The factory used to create XML model elements
         * @param node
         *            The XML root of this time range condition transition
         * @param container
         *            The state system container this time range condition
         *            belongs to
         */
        public TmfXmlTimeRangeCondition(ITmfXmlModelFactory modelFactory, Element node, IXmlStateSystemContainer container) {
            fContainer = container;
            String unit = node.getAttribute(TmfXmlStrings.UNIT);
            if (unit == null) {
                throw new IllegalArgumentException();
            }
            fUnit = unit;
            List<@Nullable Element> childElements = XmlUtils.getChildElements(node);
            if (childElements.size() != 1) {
                throw new IllegalArgumentException("Invalid timestampsChecker declaration in XML : Only one timing condition is allowed"); //$NON-NLS-1$
            }
            final Element element = NonNullUtils.checkNotNull(childElements.get(0));
            String type = element.getNodeName();
            switch (type) {
            case TmfXmlStrings.IN:
                fType = TimeRangeOperator.in;
                break;
            case TmfXmlStrings.OUT:
                fType = TimeRangeOperator.out;
                break;
            default:
                break;
            }

            final String begin = element.getAttribute(TmfXmlStrings.BEGIN);
            final String end = element.getAttribute(TmfXmlStrings.END);
            if (begin == null || end == null) {
                throw new IllegalArgumentException();
            }
            fBegin = begin;
            fEnd = end;
        }

        @Override
        public boolean testForEvent(ITmfEvent event, String... arg) {
            boolean success;
            ITmfStateSystem ss = fContainer.getStateSystem();

            long begin;
            begin = valueToNanoseconds(Long.parseLong(fBegin), fUnit);

            long end;
            end = valueToNanoseconds(Long.parseLong(fEnd), fUnit);

            // swap the value if begin > end
            if (begin > end) {
                begin = begin ^ end;
                end = begin ^ end;
                begin = begin ^ end;
            }

            begin = Math.max(ss.getStartTime(), begin);
            end = Math.min(ss.getCurrentEndTime(), end);
            begin = Math.min(begin, end);

            long ts = event.getTimestamp().normalize(0, ITmfTimestamp.NANOSECOND_SCALE).getValue();
            switch (fType) {
            case in:
                success = ts >= begin && ts <= end;
                break;
            case out:
                success = !(ts >= begin && ts <= end);
                break;
            case none:
            default:
                success = false;
                break;
            }
            return success;
        }

    }

    private class TmfXmlElapsedTimeCondition implements IXmlTimestampsCondition {

        IXmlStateSystemContainer fContainer;
        ElapsedTimeOperator fType = ElapsedTimeOperator.none;
        String fUnit;
        String fValue;
        String fReferenceState;

        /**
         * @param modelFactory
         *            The factory used to create XML model elements
         * @param node
         *            The XML root of this elapsed time condition
         * @param container
         *            The state system container this elapsed time condition
         *            belongs to
         */
        public TmfXmlElapsedTimeCondition(ITmfXmlModelFactory modelFactory, Element node, IXmlStateSystemContainer container) {
            fContainer = container;
            String unit = node.getAttribute(TmfXmlStrings.UNIT);
            if (unit == null) {
                throw new IllegalArgumentException();
            }
            fUnit = unit;
            List<@Nullable Element> childElements = XmlUtils.getChildElements(node);
            if (childElements.size() != 1) {
                throw new IllegalArgumentException("Invalid timestampsChecker declaration in XML : Only one timing condition is allowed"); //$NON-NLS-1$
            }
            final Element element = NonNullUtils.checkNotNull(childElements.get(0));
            String type = element.getNodeName();
            switch (type) {
            case TmfXmlStrings.LESS:
                fType = ElapsedTimeOperator.less;
                break;
            case TmfXmlStrings.EQUAL:
                fType = ElapsedTimeOperator.equal;
                break;
            case TmfXmlStrings.MORE:
                fType = ElapsedTimeOperator.more;
                break;
            default:
                break;
            }
            final String reference = element.getAttribute(TmfXmlStrings.SINCE);
            final String value = element.getAttribute(TmfXmlStrings.VALUE);
            if (reference == null || value == null) {
                throw new IllegalArgumentException();
            }
            fReferenceState = reference;
            fValue = value;
        }

        @Override
        public boolean testForEvent(ITmfEvent event, String... args) {
            if (args.length > 3 || args.length == 0) {
                throw new IllegalArgumentException("Arguments missing."); //$NON-NLS-1$
            }
            boolean success;
            String filterName = args[0];
            String scenarioName = args[1];
            if (filterName == null || scenarioName == null) {
                throw new IllegalArgumentException("Arguments missing."); //$NON-NLS-1$
            }
            long ts = event.getTimestamp().normalize(0, ITmfTimestamp.NANOSECOND_SCALE).getValue();
            long referenceTimestamps = TmfXmlReadWriteScenarioStatus.getInstance().getScenarioSpecificStateStartTime(event, fContainer, filterName, scenarioName, fReferenceState);
            if (ts < referenceTimestamps) {
                throw new IllegalArgumentException();
            }
            switch (fType) {
            case less:
                success = (ts - referenceTimestamps) < valueToNanoseconds(Long.parseLong(fValue), fUnit);
                break;
            case equal:
                success = (ts - referenceTimestamps) == valueToNanoseconds(Long.parseLong(fValue), fUnit);
                break;
            case more:
                success = (ts - referenceTimestamps) > valueToNanoseconds(Long.parseLong(fValue), fUnit);
                break;
            case none:
            default:
                success = false;
                break;
            }
            return success;
        }

    }
}
