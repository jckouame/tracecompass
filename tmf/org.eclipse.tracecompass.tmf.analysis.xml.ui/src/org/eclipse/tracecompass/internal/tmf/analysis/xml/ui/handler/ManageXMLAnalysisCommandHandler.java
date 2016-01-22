package org.eclipse.tracecompass.internal.tmf.analysis.xml.ui.handler;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.widgets.Display;

public class ManageXMLAnalysisCommandHandler extends AbstractHandler {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        ManageXMLAnalysisDialog dialog = new ManageXMLAnalysisDialog(Display.getDefault().getActiveShell());
        dialog.open();
        return null;
    }

}
