package org.eclipse.tracecompass.tmf.ui.widgets.timegraph;

import java.util.List;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.ITimeGraphEntry;

/**
 * @author Jean-Christian Kouame
 * @since 3.0
 *
 */
public class TimeGraphLazyContentProvider implements ITimeGraphLazyTreeContentProvider {

    TreeViewer fViewer;
    ITimeGraphEntry[] fInput;

    /**
     * Set the current viewer
     * @param viewer The viewer
     */
    public void setViewer(TreeViewer viewer) {
        fViewer = viewer;
    }
    @Override
    public void updateElement(Object parent, int index) {
        if (parent instanceof ITimeGraphEntry) {
            fViewer.replace(parent, index, ((ITimeGraphEntry) parent).getChildren().get(index));
            fViewer.setChildCount(((ITimeGraphEntry) parent).getChildren().get(index), ((ITimeGraphEntry) parent).getChildren().get(index).getChildren().size());
        } else if (parent instanceof ITimeGraphEntry[]) {
            try {
                fViewer.replace(null, index, ((ITimeGraphEntry[]) parent)[index]);
                fViewer.setChildCount(null, ((ITimeGraphEntry[]) parent).length);
            } catch (ClassCastException e) {
            }
        } else if ((parent instanceof List)) {
            try {
                fViewer.replace(parent, index, fInput[index]);
                fViewer.setChildCount(fInput[index], fInput[index].getChildren().size());
            } catch (ClassCastException e) {
            }
        }
//        fViewer.replace(parent, index, ((IRQ) parent).childs.get(index));
//        treeViewer.setChildCount(((IRQ) parent).childs.get(index), ((IRQ) parent).childs.get(index).childs.size());
    }

    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {

        if (newInput instanceof ITimeGraphEntry[]) {
            fInput = (ITimeGraphEntry[]) newInput;
            return;
        } else if (newInput instanceof List) {
            try {
                fInput = ((List<?>) newInput).toArray(new ITimeGraphEntry[0]);
                return;
            } catch (ClassCastException e) {
            }
        }
        fInput = new ITimeGraphEntry[0];
    }

    @Override
    public void updateChildCount(Object element, int currentChildCount) {
        if (element instanceof ITimeGraphEntry) {
            fViewer.setChildCount(element, ((ITimeGraphEntry) element).getChildren().size());
        } else if (element instanceof List) {
            try {
                    fViewer.setChildCount(element, ((List<?>) element).size());
            } catch (ClassCastException e) {
            }
        }
    }

    @Override
    public Object getParent(Object element) {
        return null;
    }

    /**
     * @param inputElement
     * @return
     */
    @Override
    public ITimeGraphEntry[] getElements(Object inputElement) {
        return fInput;
    }
}
