package org.eclipse.tracecompass.tmf.analysis.xml.core.model;

import java.util.Map.Entry;

import org.eclipse.tracecompass.statesystem.core.statevalue.ITmfStateValue;
import org.eclipse.tracecompass.statesystem.core.statevalue.TmfStateValue;
import org.eclipse.tracecompass.tmf.analysis.xml.core.model.readwrite.TmfXmlReadWriteScenarioStatus;
import org.eclipse.tracecompass.tmf.analysis.xml.core.module.IXmlStateSystemContainer;
import org.eclipse.tracecompass.tmf.analysis.xml.core.stateprovider.XmlFilterStateProvider;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.event.ITmfEventField;

/**
 * @author Jean-Christian Kouame
 * @since 2.0
 *
 */
public class SpecialFieldsSaverAction implements ISingleAction {

    IXmlStateSystemContainer fParent;

    /**
     * @param parent
     *            The state system container this action belongs to
     */
    public SpecialFieldsSaverAction(IXmlStateSystemContainer parent) {
        fParent = parent;
    }

    @Override
    public void execute(ITmfEvent event, String... arg) {
        XmlFilterStateProvider provider = (XmlFilterStateProvider) fParent;
        for (Entry<String, String> entry : provider.getDefinedFields().entrySet()) {
            ITmfEventField eventField = event.getContent().getField(entry.getKey());
            ITmfStateValue stateValue = null;
            if (eventField != null) {
                Object field = eventField.getValue();
                if (field != null) {
                    if (field instanceof String) {
                        stateValue = TmfStateValue.newValueString((String) field);
                    } else if (field instanceof Long) {
                        stateValue = TmfStateValue.newValueLong(((Long) field).longValue());
                    } else if (field instanceof Integer) {
                        stateValue = TmfStateValue.newValueInt(((Integer) field).intValue());
                    } else if (field instanceof Double) {
                        stateValue = TmfStateValue.newValueDouble(((Double) field).doubleValue());
                    }
                    final String name = entry.getValue();
                    if (stateValue == null) {
                        throw new IllegalArgumentException();
                    }
                    TmfXmlReadWriteScenarioStatus.saveSpecialFields(event, fParent, name, stateValue, arg);
                }
            }
        }
    }
}
