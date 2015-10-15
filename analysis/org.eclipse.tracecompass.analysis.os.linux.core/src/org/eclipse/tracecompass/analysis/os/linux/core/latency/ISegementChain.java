/*******************************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.os.linux.core.latency;

import java.util.List;

import org.eclipse.tracecompass.segmentstore.core.ISegment;

/**
 *
 * @author Bernd Hufmann
 * @since 2.0
 */
public interface ISegementChain extends ISegment {
    /**
     * Returns a ordered list of sub-segments
     *
     * @return list of sub-segments.
     */
    List<ISegment> getSubSegments();
}
