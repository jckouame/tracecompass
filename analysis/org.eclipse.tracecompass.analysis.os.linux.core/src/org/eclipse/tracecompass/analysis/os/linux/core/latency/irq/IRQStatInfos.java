package org.eclipse.tracecompass.analysis.os.linux.core.latency.irq;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.tracecompass.analysis.os.linux.core.latency.irq.XmlIrqUtils.STAT_TYPE;
import org.eclipse.tracecompass.tmf.analysis.xml.core.model.TmfXmlSyntheticEvent;

/**
 * This class compute a set of statistic data for an interruption type
 * @since 2.0
 *
 */
public class IRQStatInfos {

    /**
    * The list of child elements
    */
    public List<IRQStatInfos> childs = new ArrayList<>();
    /**
     * The minimum interrupt duration for this type
     */
    public long min = Long.MAX_VALUE;
    /**
     * The maximum interrupt duration for this type
     */
    public long max = 0;
    /**
     * The number of time this interruption happened
     */
    public long count = 0;
    /**
     * The sum of the duration of each interruption of this type
     */
    public long buffer = 0;
    /**
     * The label of this interruption
     */
    public String label;
    /**
     * The type of this interrupt
     */
    public STAT_TYPE type;
    /**
     * The synthetic event with the minimum duration
     */
    public TmfXmlSyntheticEvent minEvent;
    /**
     * The synthetic event with the maximum duration
     */
    public TmfXmlSyntheticEvent maxEvent;

    /**
     * Constructor
     *
     * @param label
     *            The label of this interruption
     * @param type
     *            The type of this interruption
     */
    public IRQStatInfos(String label, STAT_TYPE type) {
        this.label = label;
        this.type = type;
    }
}