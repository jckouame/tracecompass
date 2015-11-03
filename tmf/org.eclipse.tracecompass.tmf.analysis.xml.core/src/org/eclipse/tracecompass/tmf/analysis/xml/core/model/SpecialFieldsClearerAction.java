package org.eclipse.tracecompass.tmf.analysis.xml.core.model;

import java.util.Map.Entry;

import org.eclipse.tracecompass.tmf.analysis.xml.core.model.readwrite.TmfXmlReadWriteScenarioStatus;
import org.eclipse.tracecompass.tmf.analysis.xml.core.module.IXmlStateSystemContainer;
import org.eclipse.tracecompass.tmf.analysis.xml.core.stateprovider.XmlFilterStateProvider;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;

/**
 * @author Jean-Christian Kouame
 * @since 2.0
 *
 */
public class SpecialFieldsClearerAction implements ISingleAction {

    IXmlStateSystemContainer fParent;

    /**
     * @param parent
     *            The state system container this action belongs to
     */
    public SpecialFieldsClearerAction(IXmlStateSystemContainer parent) {
        fParent = parent;
    }

    @Override
    public void execute(ITmfEvent event, String... args) {
        XmlFilterStateProvider provider = (XmlFilterStateProvider) fParent;
        for (Entry<String, String> entry : provider.getDefinedFields().entrySet()) {
            final String value = entry.getValue();
            TmfXmlReadWriteScenarioStatus.clearSpecialFields(event, fParent, value, args);
        }
    }
}
