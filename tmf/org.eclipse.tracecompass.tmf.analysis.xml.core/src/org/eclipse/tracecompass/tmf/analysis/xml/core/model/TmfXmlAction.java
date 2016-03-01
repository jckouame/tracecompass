/*******************************************************************************
 * Copyright (c) 2016 Ecole Polytechnique de Montreal, Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.tracecompass.tmf.analysis.xml.core.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.common.core.NonNullUtils;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.Activator;
import org.eclipse.tracecompass.statesystem.core.exceptions.AttributeNotFoundException;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateValueTypeException;
import org.eclipse.tracecompass.statesystem.core.exceptions.TimeRangeException;
import org.eclipse.tracecompass.tmf.analysis.xml.core.module.IXmlStateSystemContainer;
import org.eclipse.tracecompass.tmf.analysis.xml.core.module.XmlUtils;
import org.eclipse.tracecompass.tmf.analysis.xml.core.stateprovider.TmfXmlStrings;
import org.eclipse.tracecompass.tmf.analysis.xml.core.stateprovider.XmlPatternStateProvider;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestamp;
import org.w3c.dom.Element;

/**
 * This Class implements an action tree in the XML-defined state system.
 *
 * @author Jean-Christian Kouame
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
            case TmfXmlStrings.FSM_SCHEDULE_ACTION:
                ISingleAction scheduleFsm = new XmlScheduleFsm(modelFactory, nonNullChild, fParent);
                fActionList.add(scheduleFsm);
                break;
            case TmfXmlStrings.SEGMENT:
                ISingleAction patternSegment = new XmlGeneratePatternSegmentAction(modelFactory, nonNullChild, fParent);
                fActionList.add(patternSegment);
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
    public void execute(@NonNull ITmfEvent event, String scenarioName, String activeState) {
        for (ISingleAction action : fActionList) {
            action.execute(event, scenarioName, activeState);
        }
    }

    private class XmlStateChangeAction implements ISingleAction {

        private final TmfXmlStateChange fStateChange;

        public XmlStateChangeAction(ITmfXmlModelFactory modelFactory, Element node, IXmlStateSystemContainer parent) {
            fStateChange = modelFactory.createStateChange(node, parent);
        }

        @Override
        public void execute(@NonNull ITmfEvent event, String scenarioName, String activeState) {
            try {
                fStateChange.handleEvent(event, scenarioName, activeState);
            } catch (AttributeNotFoundException ae) {
            /*
             * This would indicate a problem with the logic of the manager
             * here, so it shouldn't happen.
             */
            Activator.logError("Attribute not found", ae); //$NON-NLS-1$
        } catch (TimeRangeException tre) {
            /*
             * This would happen if the events in the trace aren't ordered
             * chronologically, which should never be the case ...
             */
            Activator.logError("TimeRangeException caught in the state system's event manager.  Are the events in the trace correctly ordered?", tre); //$NON-NLS-1$
        } catch (StateValueTypeException sve) {
            /*
             * This would happen if we were trying to push/pop attributes
             * not of type integer. Which, once again, should never happen.
             */
            Activator.logError("State value type error", sve); //$NON-NLS-1$
        }
        }
    }

    private static class XmlScheduleFsm implements ISingleAction {

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
        }

        @Override
        public void execute(ITmfEvent event, String scenarioName, String activeState) {
            //TODO This action needs to be implemented
            Activator.logWarning("Schedule an FSM is not already supported"); //$NON-NLS-1$
        }
    }

    private static class XmlGeneratePatternSegmentAction implements ISingleAction {

        private TmfXmlPatternSegmentBuilder fSegmentBuilder;
        private XmlPatternStateProvider fProvider;

        public XmlGeneratePatternSegmentAction(ITmfXmlModelFactory modelFactory, Element node, IXmlStateSystemContainer parent) {
            fProvider = ((XmlPatternStateProvider) parent);
            fSegmentBuilder = modelFactory.createPatternSegmentBuilder(node, parent);
        }

        @Override
        public void execute(ITmfEvent event, String scenarioName, String activeState) {
            long ts = TmfXmlReadWriteScenarioStatus.getInstance().getScenarioPatternDiscoveryStartTime(event, fProvider, scenarioName);
            //TODO IS THE SCALE ALWAYS NANOSECOND?
            ITmfTimestamp start = new TmfTimestamp(ts, ITmfTimestamp.NANOSECOND_SCALE);
            ITmfTimestamp end;
            end = event.getTimestamp();
            fSegmentBuilder.generatePatternSegment(event, start, end, scenarioName, activeState);
        }
    }
}
