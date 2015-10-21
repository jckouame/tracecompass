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

/**
 * The attribute names that are used in the XML file
 * @since 2.0
 */
public interface Attributes {

    /** First-level attributes */
    static final String FILTER = "filter"; //$NON-NLS-1$

    /** Sub-attributes of the filter nodes */
    static final String SCENARIOS = "scenarios"; //$NON-NLS-1$

    /** Sub-attributes of a scenario node */
    static final String STATUS = "status"; //$NON-NLS-1$

    /** Misc stuff */
    static final String UNKNOWN = "Unknown"; //$NON-NLS-1$
}
