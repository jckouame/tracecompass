package org.eclipse.tracecompass.tmf.analysis.xml.core.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.common.core.NonNullUtils;
import org.eclipse.tracecompass.tmf.analysis.xml.core.module.IXmlStateSystemContainer;
import org.eclipse.tracecompass.tmf.analysis.xml.core.stateprovider.TmfXmlStrings;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * @author Jean-Christian Kouame
 * @since 2.0
 *
 */
public class TmfXmlState {
    private final String fId;
    private final boolean fAutomatic;
    private final IXmlStateSystemContainer fContainer;
    private final List<TmfXmlStateTransition> fTransitionList = new ArrayList<>();
    private @Nullable TmfXmlState fparent;
    private String[] fOnentryActions;
    private String[] fOnexitActions;
    private Map<String, TmfXmlState> fChildren = new HashMap<>();
    private @Nullable TmfXmlStateTransition fInitialTransition;
    private @Nullable String fInitialStateId;
    private @Nullable String fFinalStateId;
    private Type fType;

    public enum Type {
        FINAL,
        INITIAL,
        ABANDON,
        DEFAULT
    }
    /**
     * Constructor
     *
     * @param modelFactory
     *            The factory used to create XML model elements
     * @param node
     *            The XML root of this state definition
     * @param container
     *            The state system container this state definition belongs to
     * @param parent
     *            The parent state of this state
     */
    public TmfXmlState(ITmfXmlModelFactory modelFactory, Element node, IXmlStateSystemContainer container, @Nullable TmfXmlState parent) {
        fContainer = container;
        fparent = parent;

        switch (node.getNodeName()) {
        case "final":
            fType = Type.FINAL;
            break;
        case "initial":
            fType = Type.INITIAL;
            break;
        case "abandon":
            fType = Type.ABANDON;
            break;
        case "state":
        default:
            fType = Type.DEFAULT;
            break;
        }

        fId = node.getAttribute(TmfXmlStrings.ID);
        fAutomatic = Boolean.parseBoolean(node.getAttribute(TmfXmlStrings.AUTOMATIC));

        NodeList nodesTransition = node.getElementsByTagName(TmfXmlStrings.TRANSITION);
        for (int i = 0; i < nodesTransition.getLength(); i++) {
            final Element element = (Element) nodesTransition.item(i);
            if (element == null) {
                throw new IllegalArgumentException();
            }
            TmfXmlStateTransition transition = modelFactory.createStateTransition(element, fContainer);
            fTransitionList.add(transition);
        }

        NodeList nodesOnentry = node.getElementsByTagName(TmfXmlStrings.ONENTRY);
        fOnentryActions = nodesOnentry.getLength() > 0 ? ((Element) nodesOnentry.item(0)).getAttribute(TmfXmlStrings.ACTION).split(TmfXmlStrings.AND_SEPARATOR) : new String[] {};

        NodeList nodesOnexit = node.getElementsByTagName(TmfXmlStrings.ONEXIT);
        fOnexitActions = nodesOnexit.getLength() > 0 ? ((Element) nodesOnexit.item(0)).getAttribute(TmfXmlStrings.ACTION).split(TmfXmlStrings.AND_SEPARATOR) : new String[] {};

        String initial = node.getAttribute(TmfXmlStrings.INITIAL);
        if (initial.isEmpty()) {
            NodeList nodesInitial = node.getElementsByTagName(TmfXmlStrings.INITIAL);
            if (nodesInitial.getLength() > 0) {
                Element transitionElement = (Element) ((Element) nodesInitial.item(0)).getElementsByTagName(TmfXmlStrings.TRANSITION).item(0);
                fInitialTransition = modelFactory.createStateTransition(transitionElement, fContainer);
                initial = fInitialTransition.getTarget();
            }
        }

        NodeList nodesState = node.getElementsByTagName(TmfXmlStrings.STATE);
        for (int i = 0; i < nodesState.getLength(); i++) {
            TmfXmlState child = modelFactory.createState(NonNullUtils.checkNotNull((Element) nodesState.item(i)), container, this);
            fChildren.put(child.getId(), child);

            if (i == 0 && initial.isEmpty()) {
                initial = child.getId();
            }
        }
        fInitialStateId = initial.isEmpty() ? null : initial;

        NodeList nodesFinal = node.getElementsByTagName(TmfXmlStrings.FINAL);
        if (nodesFinal.getLength() > 0) {
            final Element finalElement = NonNullUtils.checkNotNull((Element) nodesFinal.item(0));
            fFinalStateId = nodesFinal.getLength() > 0 ? finalElement.getAttribute(TmfXmlStrings.ID) : null;
            TmfXmlState finalState = modelFactory.createState(finalElement, container, this);
            fChildren.put(finalState.getId(), finalState);
        }
    }

    /**
     * Get the state id
     *
     * @return The state id
     */
    public String getId() {
        return fId;
    }

    public boolean isAutomatic() {
        return fAutomatic;
    }

    public IXmlStateSystemContainer getContainer() {
        return fContainer;
    }

    public List<TmfXmlStateTransition> getTransitionList() {
        return fTransitionList;
    }

    public String[] getOnentryActions() {
        return fOnentryActions;
    }

    public String[] getOnexitActions() {
        return fOnexitActions;
    }

    public Map<String, TmfXmlState> getChildren() {
        return fChildren;
    }

    public @Nullable TmfXmlStateTransition getInitialTransition() {
        return fInitialTransition;
    }

    public @Nullable String getInitialStateId() {
        return fInitialStateId;
    }

    public @Nullable String getFinalStateId() {
        return fFinalStateId;
    }

    public @Nullable TmfXmlState getFparent() {
        return fparent;
    }

    public Type getType() {
        return fType;
    }
}
