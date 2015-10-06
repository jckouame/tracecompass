package org.eclipse.tracecompass.internal.analysis.os.linux.ui.views.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.tracecompass.internal.analysis.os.linux.ui.views.resources.XmlIrqUtils.STAT_TYPE;
import org.eclipse.tracecompass.tmf.analysis.xml.core.model.TmfXmlSyntheticEvent;

/**
 * @author Jean-Christian Kouame
 * @since 2.0
 *
 */
public class IRQStatInfos {

   public List<IRQStatInfos> childs = new ArrayList<>();
   public long min = Long.MAX_VALUE;
   public long max = 0;
   public long count = 0;
   public long buffer = 0;
   public String label;
   public STAT_TYPE type;
   public TmfXmlSyntheticEvent minEvent;
   public TmfXmlSyntheticEvent maxEvent;

   public IRQStatInfos (String label, STAT_TYPE type) {
       this.label = label;
       this.type = type;
   }
}