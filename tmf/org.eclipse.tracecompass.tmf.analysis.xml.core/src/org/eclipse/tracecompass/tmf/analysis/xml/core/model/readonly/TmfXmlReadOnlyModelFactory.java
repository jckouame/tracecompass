/*******************************************************************************
 * Copyright (c) 2014 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.analysis.xml.core.model.readonly;

import java.util.List;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.tmf.analysis.xml.core.model.ITmfXmlModelFactory;
import org.eclipse.tracecompass.tmf.analysis.xml.core.model.ITmfXmlStateAttribute;
import org.eclipse.tracecompass.tmf.analysis.xml.core.model.ITmfXmlStateValue;
import org.eclipse.tracecompass.tmf.analysis.xml.core.model.TmfXmlAction;
import org.eclipse.tracecompass.tmf.analysis.xml.core.model.TmfXmlCondition;
import org.eclipse.tracecompass.tmf.analysis.xml.core.model.TmfXmlEventHandler;
import org.eclipse.tracecompass.tmf.analysis.xml.core.model.TmfXmlFilterEventHandler;
import org.eclipse.tracecompass.tmf.analysis.xml.core.model.TmfXmlFsm;
import org.eclipse.tracecompass.tmf.analysis.xml.core.model.TmfXmlLocation;
import org.eclipse.tracecompass.tmf.analysis.xml.core.model.TmfXmlStateChange;
import org.eclipse.tracecompass.tmf.analysis.xml.core.model.TmfXmlStateDefinition;
import org.eclipse.tracecompass.tmf.analysis.xml.core.model.TmfXmlStateTransition;
import org.eclipse.tracecompass.tmf.analysis.xml.core.model.TmfXmlSyntheticEvent;
import org.eclipse.tracecompass.tmf.analysis.xml.core.model.TmfXmlTimestampsCondition;
import org.eclipse.tracecompass.tmf.analysis.xml.core.model.TmfXmlTransitionInput;
import org.eclipse.tracecompass.tmf.analysis.xml.core.model.readwrite.TmfXmlReadWriteModelFactory;
import org.eclipse.tracecompass.tmf.analysis.xml.core.module.IXmlStateSystemContainer;
import org.w3c.dom.Element;

/**
 * Concrete factory for XML model elements in read only mode
 *
 * @author Geneviève Bastien
 */
public class TmfXmlReadOnlyModelFactory implements ITmfXmlModelFactory {

    private static @Nullable ITmfXmlModelFactory fInstance = null;

    /**
     * Get the instance of this model creator
     *
     * @return The {@link TmfXmlReadWriteModelFactory} instance
     */
    public static synchronized ITmfXmlModelFactory getInstance() {
        ITmfXmlModelFactory instance = fInstance;
        if (instance == null) {
            instance = new TmfXmlReadOnlyModelFactory();
            fInstance = instance;
        }
        return instance;
    }

    @Override
    public ITmfXmlStateAttribute createStateAttribute(Element attribute, IXmlStateSystemContainer container) {
        return new TmfXmlReadOnlyStateAttribute(this, attribute, container);
    }

    @Override
    public ITmfXmlStateValue createStateValue(Element node, IXmlStateSystemContainer container, List<ITmfXmlStateAttribute> attributes) {
        return new TmfXmlReadOnlyStateValue(this, node, container, attributes);
    }

    @Override
    public ITmfXmlStateValue createStateValue(Element node, IXmlStateSystemContainer container, String eventField) {
        return new TmfXmlReadOnlyStateValue(this, node, container, eventField);
    }

    @Override
    public TmfXmlCondition createCondition(Element node, IXmlStateSystemContainer container) {
        return new TmfXmlCondition(this, node, container);
    }

    @Override
    public TmfXmlEventHandler createEventHandler(Element node, IXmlStateSystemContainer container) {
        return new TmfXmlEventHandler(this, node, container);
    }

    @Override
    public TmfXmlStateChange createStateChange(Element node, IXmlStateSystemContainer container) {
        return new TmfXmlStateChange(this, node, container);
    }

    @Override
    public TmfXmlLocation createLocation(Element node, IXmlStateSystemContainer container) {
        return new TmfXmlLocation(this, node, container);
    }


    /**
     * @since 2.0
     */
    @Override
    public TmfXmlFilterEventHandler createFilterEventHandler(Element node, IXmlStateSystemContainer container) {
        return new TmfXmlFilterEventHandler(this, node, container);
    }

    /**
     * @since 2.0
     */
    @Override
    public TmfXmlTransitionInput createTransitionInput(Element node, IXmlStateSystemContainer container) {
        return new TmfXmlTransitionInput(this, node, container);
    }

    /**
     * @since 2.0
     */
    @Override
    public TmfXmlAction createAction(Element node, IXmlStateSystemContainer container) {
        return new TmfXmlAction(this, node, container);
    }

    /**
     * @since 2.0
     */
    @Override
    public TmfXmlFsm createFsm(Element node, IXmlStateSystemContainer container) {
        return new TmfXmlFsm(this, node, container);
    }

    /**
     * @since 2.0
     */
    @Override
    public TmfXmlStateDefinition createStateDefinition(Element node, IXmlStateSystemContainer container) {
        return new TmfXmlStateDefinition(this, node, container);
    }

    /**
     * @since 2.0
     */
    @Override
    public TmfXmlStateTransition createStateTransition(Element node, IXmlStateSystemContainer container) {
        return new TmfXmlStateTransition(this, node, container);
    }

    /**
     * @since 2.0
     */
    @Override
    public TmfXmlTimestampsCondition createTimestampsCondition(Element node, IXmlStateSystemContainer container) {
        return new TmfXmlTimestampsCondition(this, node, container);
    }

    /**
     * @since 2.0
     */
    @Override
    public TmfXmlSyntheticEvent createSyntheticEvent(Element node, IXmlStateSystemContainer container) {
        return new TmfXmlSyntheticEvent(this, node, container);
    }
}
