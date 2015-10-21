package org.eclipse.tracecompass.tmf.analysis.xml.core.model;

import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;

/**
 * @author Jean-Christian Kouame
 * @since 2.0
 *
 */
public interface ISingleAction {

    /**
    *
    */
   public final static String ACTION_SEPARATOR = ":"; //$NON-NLS-1$
   /**
    *
    */
   public final static String SPECIAL_ACTION_PREFIX = "#"; //$NON-NLS-1$
   /**
   *
   */
  public final static String SAVE_SPECIAL_FIELD_STRING = "saveSpecialField"; //$NON-NLS-1$
  /**
  *
  */
 public final static String CLEAR_SPECIAL_FIELD_STRING = "clearSpecialField"; //$NON-NLS-1$

    /**
     * execute the action
     * @param event The current event to process
     * @param arg The arguments used to execute the actions
     */
    void execute(ITmfEvent event, String... arg);
}
