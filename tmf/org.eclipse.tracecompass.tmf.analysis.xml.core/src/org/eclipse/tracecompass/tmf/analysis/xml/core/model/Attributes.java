package org.eclipse.tracecompass.tmf.analysis.xml.core.model;

/**
 * @since 2.0
 */
@SuppressWarnings({"nls", "javadoc"})
public interface Attributes {

    /* First-level attributes */
    static final String FILTER = "filter";

    /* Sub-attributes of the filter nodes */
    static final String SCENARIOS = "scenarios";

    static final String STATUS = "status";

    /* Misc stuff */
    static final String UNKNOWN = "Unknown";
}
