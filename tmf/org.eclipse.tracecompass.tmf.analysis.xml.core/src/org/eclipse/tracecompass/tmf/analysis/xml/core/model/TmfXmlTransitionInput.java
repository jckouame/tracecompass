package org.eclipse.tracecompass.tmf.analysis.xml.core.model;

import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.common.core.NonNullUtils;
import org.eclipse.tracecompass.statesystem.core.exceptions.AttributeNotFoundException;
import org.eclipse.tracecompass.tmf.analysis.xml.core.module.IXmlStateSystemContainer;
import org.eclipse.tracecompass.tmf.analysis.xml.core.module.XmlUtils;
import org.eclipse.tracecompass.tmf.analysis.xml.core.stateprovider.TmfXmlStrings;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * @author Jean-Christian Kouame
 * @since 2.0
 *
 */
public class TmfXmlTransitionInput {

    IXmlStateSystemContainer fParent;
    private final String fId;
    private final IXmlTransitionCondition fTransitionCondition;

    /**
     * Constructor
     *
     * @param modelFactory
     *            The factory used to create XML model elements
     * @param node
     *            The XML root of this transition input
     * @param parent
     *            The state system container this transition input belongs to
     */
    public TmfXmlTransitionInput(ITmfXmlModelFactory modelFactory, Element node, IXmlStateSystemContainer parent) {
        fParent = parent;
        final String id = node.getAttribute(TmfXmlStrings.ID);
        if (id == null) {
            throw new IllegalArgumentException();
        }
        fId = id;

        List<@Nullable Element> childElements = XmlUtils.getChildElements(node);
        Node child = childElements.get(0);
        String childName = NonNullUtils.checkNotNull(child).getNodeName();
        switch (childName) {
        case TmfXmlStrings.EVENT:
            fTransitionCondition = new EventTransition(modelFactory, (Element) child, fParent);
            break;
        case TmfXmlStrings.TIME:
            fTransitionCondition = new TimeTransition(modelFactory, (Element) child, fParent);
            break;
        default:
            throw new IllegalArgumentException("invalid xml transition input type"); //$NON-NLS-1$
        }
    }

    /**
     * @return The id of this transition input
     */
    public String getId() {
        return fId;
    }

    /**
     * @param event
     *            The input event
     * @param args
     *            The arguments required to validate this event
     * @return true if the transition is validate false if not
     */
    public boolean validate(ITmfEvent event, String... args) {
        return fTransitionCondition.validate(event, args);
    }

    private interface IXmlTransitionCondition {
        boolean validate(ITmfEvent event, String... args);
    }

    private class EventTransition implements IXmlTransitionCondition {

        private final String REGEX = "\\|"; //$NON-NLS-1$
        private String fAcceptedEvents[];
        private final @Nullable TmfXmlCondition fCondition;

        public EventTransition(ITmfXmlModelFactory modelFactory, Element element, IXmlStateSystemContainer container) {
            final String[] eventNames = (element.getAttribute(TmfXmlStrings.EVENT_NAME)).split(REGEX);
            if (eventNames == null) {
                throw new IllegalArgumentException();
            }
            fAcceptedEvents = eventNames;
            Node ifNode = element.getElementsByTagName(TmfXmlStrings.IF).item(0);
            fCondition = ifNode != null ? modelFactory.createCondition((Element) ifNode, container) : null;
        }

        @Override
        public boolean validate(@NonNull ITmfEvent event, String... arg) {
            boolean isValide = false;
            try {
                isValide |= validateEvent(event);
                isValide &= (isValide && fCondition != null) ? fCondition.testForEvent(event, arg) : true;
            } catch (AttributeNotFoundException e) {
            }

            return isValide;
        }

        private boolean validateEvent(ITmfEvent event) {
            String eventName = event.getName();

            for (String name : fAcceptedEvents) {

                if ((name.startsWith(TmfXmlStrings.CONSTANT_PREFIX)) && name.substring(1).equals(TmfXmlStrings.ANY)) {
                    return true;
                }

                /* test for full name */
                if (eventName.equals(name)) {
                    return true;
                }

                /* test for the wildcard at the end */
                if (name.endsWith(TmfXmlStrings.WILDCARD) && name.startsWith(TmfXmlStrings.WILDCARD) && eventName.contains(name.replace(TmfXmlStrings.WILDCARD, TmfXmlStrings.NULL))) {
                    return true;
                }

                /* test for the wildcard at the end */
                if ((name.endsWith(TmfXmlStrings.WILDCARD) && eventName.startsWith(name.replace(TmfXmlStrings.WILDCARD, TmfXmlStrings.NULL)))) {
                    return true;
                }
            }
            return false;
        }
    }

    private class TimeTransition implements IXmlTransitionCondition {

        private final TmfXmlCondition fCondition;

        public TimeTransition(ITmfXmlModelFactory modelFactory, Element element, IXmlStateSystemContainer container) {
            fCondition = modelFactory.createCondition(element, container);
        }

        @Override
        public boolean validate(@NonNull ITmfEvent event, String... arg) {
            boolean isValide = false;
            try {
                isValide = fCondition.testForEvent(event, arg);
            } catch (AttributeNotFoundException e) {
            }
            return isValide;
        }

    }
}
