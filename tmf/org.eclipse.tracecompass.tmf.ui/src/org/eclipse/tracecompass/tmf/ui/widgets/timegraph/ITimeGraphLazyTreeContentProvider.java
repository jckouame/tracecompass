package org.eclipse.tracecompass.tmf.ui.widgets.timegraph;

import org.eclipse.jface.viewers.ILazyTreeContentProvider;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.ITimeGraphEntry;

/**
 * @author Jean-Christian Kouame
 * @since 3.0
 *
 */
public interface ITimeGraphLazyTreeContentProvider extends ILazyTreeContentProvider {

    @Override
    public void updateElement(Object parent, int index);

    @Override
    public void updateChildCount(Object element, int currentChildCount);

    @Override
    public Object getParent(Object element);

    /**
     * @param inputElement
     * @return
     */
    public ITimeGraphEntry[] getElements(Object inputElement);
}
