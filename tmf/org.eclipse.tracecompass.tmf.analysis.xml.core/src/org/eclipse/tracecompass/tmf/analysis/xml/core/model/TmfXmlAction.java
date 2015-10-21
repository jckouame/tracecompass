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
import org.eclipse.tracecompass.tmf.analysis.xml.core.stateprovider.XmlPatternStateProvider;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.event.TmfSyntheticEvent;
import org.eclipse.tracecompass.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestamp;
import org.w3c.dom.Element;

/**
 * This Class implement an action tree in the XML-defined state system.
 * @since 2.0
 *
 */
public class TmfXmlAction implements ISingleAction {

    private IXmlStateSystemContainer fParent;
    private String fId;
    private final List<ISingleAction> fActionList = new ArrayList<>();

    /**
     * Constructor
     * @param modelFactory
     *            The factory used to create XML model elements
     * @param node
     *            The XML root of this action
     * @param container
     *            The state system container this action belongs to
     */
    public TmfXmlAction(ITmfXmlModelFactory modelFactory, Element node, IXmlStateSystemContainer container) {
        fParent = container;
        fId = NonNullUtils.checkNotNull(node.getAttribute(TmfXmlStrings.ID));
        List<@Nullable Element> childElements = XmlUtils.getChildElements(node);
        for (Element child : childElements) {
            final @NonNull Element nonNullChild = NonNullUtils.checkNotNull(child);
            switch (nonNullChild.getNodeName()) {
            case TmfXmlStrings.STATE_CHANGE:
                ISingleAction stateChange = new XmlStateChangeAction(modelFactory, nonNullChild, fParent);
                fActionList.add(stateChange);
                break;
            case TmfXmlStrings.UI:
                ISingleAction ui = new XmlUiAction(modelFactory, nonNullChild, fParent);
                fActionList.add(ui);
                break;
            case TmfXmlStrings.FSM_SCHEDULE_ACTION:
                ISingleAction scheduleFsm = new XmlScheduleFsm(modelFactory, nonNullChild, fParent);
                fActionList.add(scheduleFsm);
                break;
            case TmfXmlStrings.SYN_EVENT:
                ISingleAction synEvent = new XmlGenerateSyntheticEventAction(modelFactory, nonNullChild, fParent);
                fActionList.add(synEvent);
                break;
            case TmfXmlStrings.ACTION:
                ISingleAction action = new TmfXmlAction(modelFactory, nonNullChild, fParent);
                fActionList.add(action);
                break;
            default:
                break;
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
    public void execute(@NonNull ITmfEvent event, String... args) {
        for (ISingleAction action : fActionList) {
            action.execute(event, args);
        }
    }

    private class XmlStateChangeAction implements ISingleAction {

        private final TmfXmlStateChange fStateChange;

        public XmlStateChangeAction(ITmfXmlModelFactory modelFactory, Element node, IXmlStateSystemContainer parent) {
            fStateChange = modelFactory.createStateChange(node, parent);
        }

        @Override
        public void execute(@NonNull ITmfEvent event, String... args) {
            try {
                fStateChange.handleEvent(event, args);
            } catch (StateValueTypeException e) {
            } catch (TimeRangeException e) {
            } catch (AttributeNotFoundException e) {
            }
        }
    }

    private static class XmlUiAction implements ISingleAction {

        /**
         * Constructor
         *
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

    private static class XmlScheduleFsm implements ISingleAction {

        private List<String> fFsmToScheduleList = new ArrayList<>();
        private XmlPatternStateProvider fProvider;
        private static final String REGEX = "&"; //$NON-NLS-1$

        /**
         * Constructor
         *
         * @param modelFactory
         *            The factory used to create XML model elements
         * @param node
         *            The XML root of this action
         * @param container
         *            The state system container this action belongs to
         */
        public XmlScheduleFsm(ITmfXmlModelFactory modelFactory, Element node, IXmlStateSystemContainer container) {
            fProvider = (XmlPatternStateProvider) container;
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

    private static class XmlGenerateSyntheticEventAction implements ISingleAction {

        private TmfXmlSyntheticEventBuilder fSynEventBuilder;
        private XmlPatternStateProvider fProvider;

        public XmlGenerateSyntheticEventAction(ITmfXmlModelFactory modelFactory, Element node, IXmlStateSystemContainer parent) {
            fProvider = ((XmlPatternStateProvider) parent);
            fSynEventBuilder = modelFactory.createSyntheticEventBuilder(node, parent);
        }

        @Override
        public void execute(ITmfEvent event, String... args) {
            long ts = TmfXmlReadWriteScenarioStatus.getInstance().getScenarioPatternDiscoveryStartTime(event, fProvider, NonNullUtils.checkNotNull(args[0]), NonNullUtils.checkNotNull(args[1]));
            //TODO IS THE SCALE ALWAYS NANOSECOND?
            ITmfTimestamp start = new TmfTimestamp(ts, ITmfTimestamp.NANOSECOND_SCALE);
            ITmfTimestamp end;
            if (event instanceof TmfSyntheticEvent) {
                end = NonNullUtils.checkNotNull(((TmfSyntheticEvent) event).getTimestampEnd());
            } else {
                end = event.getTimestamp();
            }
            TmfSyntheticEvent synEvent = fSynEventBuilder.generateSyntheticEvent(event, start, end, args);
            fProvider.handleSyntheticEvent(synEvent);
        }
    }
}
