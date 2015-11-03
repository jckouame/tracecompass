package org.eclipse.tracecompass.tmf.analysis.xml.core.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.common.core.NonNullUtils;
import org.eclipse.tracecompass.statesystem.core.exceptions.AttributeNotFoundException;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateValueTypeException;
import org.eclipse.tracecompass.statesystem.core.exceptions.TimeRangeException;
import org.eclipse.tracecompass.tmf.analysis.xml.core.model.readwrite.TmfXmlReadWriteScenarioStatus;
import org.eclipse.tracecompass.tmf.analysis.xml.core.module.IXmlStateSystemContainer;
import org.eclipse.tracecompass.tmf.analysis.xml.core.module.XmlUtils;
import org.eclipse.tracecompass.tmf.analysis.xml.core.stateprovider.TmfXmlStrings;
import org.eclipse.tracecompass.tmf.analysis.xml.core.stateprovider.XmlFilterStateProvider;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.event.ITmfSynEvent;
import org.eclipse.tracecompass.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestamp;
import org.w3c.dom.Element;

/**
 * @author Jean-Christian Kouame
 * @since 2.0
 *
 */
public class TmfXmlAction implements ISingleAction {

    /**
     * @author bitray
     *
     */
    IXmlStateSystemContainer fParent;
    String fId;
    private final List<ISingleAction> fActionList = new ArrayList<>();

    /**
     * @param modelFactory
     *            The factory used to create XML model elements
     * @param node
     *            The XML root of this action
     * @param container
     *            The state system container this action belongs to
     */
    public TmfXmlAction(@Nullable ITmfXmlModelFactory modelFactory, Element node, IXmlStateSystemContainer container) {
        fId = node.getAttribute(TmfXmlStrings.ID);

        fParent = container;
        if (modelFactory != null) {
            fId = node.getAttribute(TmfXmlStrings.ID);
            List<@Nullable Element> childElements = XmlUtils.getChildElements(node);
            for (Element child : childElements) {
                if (child == null) {
                    continue;
                }
                switch (child.getNodeName()) {
                case TmfXmlStrings.STATE_CHANGE:
                    ISingleAction stateChange = new XmlStateChangeAction(modelFactory, child, container);
                    fActionList.add(stateChange);
                    break;
                case TmfXmlStrings.UI:
                    ISingleAction ui = new XmlUiAction(modelFactory, child, container);
                    fActionList.add(ui);
                    break;
                case TmfXmlStrings.FSM_SCHEDULE_ACTION:
                    ISingleAction scheduleFsm = new XmlScheduleFsm(modelFactory, child, container);
                    fActionList.add(scheduleFsm);
                    break;
                case TmfXmlStrings.SYN_EVENT:
                    ISingleAction synEvent = new XmlGenerateSyntheticEventAction(modelFactory, child, container);
                    fActionList.add(synEvent);
                    break;
                case TmfXmlStrings.ACTION:
                    ISingleAction action = new TmfXmlAction(modelFactory, child, container);
                    fActionList.add(action);
                    break;
                default:
                    break;
                }
            }
        }

    }

    /**
     * @return The id of this action
     */
    public String getId() {
        return fId;
    }

    @Override
    public void execute(@NonNull ITmfEvent event, String... arg) {
        for (ISingleAction action : fActionList) {
            action.execute(event, arg);
        }
    }

    private class XmlStateChangeAction implements ISingleAction {

        private final TmfXmlStateChange fStateChange;

        public XmlStateChangeAction(ITmfXmlModelFactory modelFactory, Element node, IXmlStateSystemContainer parent) {
            fStateChange = modelFactory.createStateChange(node, parent);
        }

        @Override
        public void execute(@NonNull ITmfEvent event, String... arg) {
            try {
                fStateChange.handleEvent(event, arg);
            } catch (StateValueTypeException e) {
            } catch (TimeRangeException e) {
            } catch (AttributeNotFoundException e) {
            }
        }
    }

    private class XmlUiAction implements ISingleAction {

        /**
         * @param modelFactory
         *            The factory used to create XML model elements
         * @param node
         *            The XML root of this XML action
         * @param container
         *            The state system container this XML action belongs to
         */
        public XmlUiAction(ITmfXmlModelFactory modelFactory, Element node, IXmlStateSystemContainer container) {
        }

        @Override
        public void execute(ITmfEvent event, String... args) {
            // TODO A UI action should be implemented here
        }
    }

    private class XmlScheduleFsm implements ISingleAction {

        private List<String> fFsmToScheduleList = new ArrayList<>();
        private XmlFilterStateProvider fProvider;
        private final String REGEX = "&"; //$NON-NLS-1$

        /**
         * @param modelFactory
         *            The factory used to create XML model elements
         * @param node
         *            The XML root of this action
         * @param container
         *            The state system container this action belongs to
         */
        public XmlScheduleFsm(ITmfXmlModelFactory modelFactory, Element node, IXmlStateSystemContainer container) {
            fProvider = (XmlFilterStateProvider) container;
            String[] fsmToScheduleList = node.getAttribute(TmfXmlStrings.ID).split(REGEX);
            final List<String> fsmList = Arrays.asList(fsmToScheduleList);
            fFsmToScheduleList = fsmList;
        }

        @Override
        public void execute(ITmfEvent event, String... args) {
            if (args.length < 4) {
                throw new IllegalArgumentException();
            }
            fProvider.startFsm(args[0], args[3], fFsmToScheduleList, event);
        }
    }

    private class XmlGenerateSyntheticEventAction implements ISingleAction {

        private TmfXmlSyntheticEvent fSynEvent;
        private XmlFilterStateProvider fProvider;

        public XmlGenerateSyntheticEventAction(ITmfXmlModelFactory modelFactory, Element node, IXmlStateSystemContainer parent) {
            fProvider = ((XmlFilterStateProvider) parent);
            fSynEvent = modelFactory.createSyntheticEvent(node, parent);
        }

        @Override
        public void execute(ITmfEvent event, String... arg) {
            TmfXmlSyntheticEvent synEvent = new TmfXmlSyntheticEvent(fSynEvent);
            synEvent.setLastEvent(event);
            synEvent.computeSynFields(event, arg);
            long ts = TmfXmlReadWriteScenarioStatus.getInstance().getScenarioPatternDiscoveryStartTime(event, fProvider, NonNullUtils.checkNotNull(arg[0]), NonNullUtils.checkNotNull(arg[1]));
            synEvent.setTimestamp(new TmfTimestamp(ts, ITmfTimestamp.NANOSECOND_SCALE));
            if (event instanceof ITmfSynEvent) {
                synEvent.setEndTimestamp(NonNullUtils.checkNotNull(((ITmfSynEvent) event).getTimestampEnd()));
            } else {
                synEvent.setEndTimestamp(event.getTimestamp());
            }
            fProvider.handleSyntheticEvent(synEvent);
        }
    }
}
