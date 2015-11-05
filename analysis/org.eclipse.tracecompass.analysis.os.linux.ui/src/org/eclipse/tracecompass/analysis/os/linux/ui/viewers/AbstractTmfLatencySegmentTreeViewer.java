/*******************************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Jean-Christian Kouame - Initial API and implementation
 *******************************************************************************/
package org.eclipse.tracecompass.analysis.os.linux.ui.viewers;

import java.util.Comparator;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ILazyTreeContentProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.tracecompass.common.core.NonNullUtils;
import org.eclipse.tracecompass.segmentstore.core.ISegment;
import org.eclipse.tracecompass.tmf.analysis.xml.core.stateprovider.XmlPatternStateSystemModule;
import org.eclipse.tracecompass.tmf.core.signal.TmfSelectionRangeUpdatedSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalHandler;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceClosedSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceSelectedSignal;
import org.eclipse.tracecompass.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;
import org.eclipse.tracecompass.tmf.ui.TmfUiRefreshHandler;
import org.eclipse.tracecompass.tmf.ui.viewers.TmfViewer;

import com.google.common.collect.Iterables;

/**
 * Abstract class to display iSegment latency analysis in a tree column table
 *
 * @since 2.0
 *
 */
public abstract class AbstractTmfLatencySegmentTreeViewer extends TmfViewer {

    private TreeViewer fTreeViewer;
    private Thread fUpdateThread;
    private ITmfTrace fTrace;
    private MenuManager fTablePopupMenuManager;

    /**
     * Constructor
     *
     * @param parent
     *            The parent composite
     */
    public AbstractTmfLatencySegmentTreeViewer(Composite parent) {
        super(parent);
        fTreeViewer = new TreeViewer(parent, SWT.VIRTUAL);

        createColumns(fTreeViewer);

        final Tree tree = fTreeViewer.getTree();
        tree.setHeaderVisible(true);
        tree.setLinesVisible(true);
        TreeColumn[] columns = tree.getColumns();
        for (int i = 0; i < columns.length; i++) {
            addTreeColumnSelectionListener(columns[i], i, fTreeViewer);
        }

        fTreeViewer.setContentProvider(new LatencyContentProvider(this));
        fTreeViewer.setUseHashlookup(true);
        fTreeViewer.addSelectionChangedListener(new ISelectionChangedListener() {

            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                StructuredSelection selection = (StructuredSelection) event.getSelection();
                Object o = selection.getFirstElement();
                if (o instanceof ISegment) {
                    ISegment segment = (ISegment) o;
                    if (segment.getLength() != -1) {
                        broadcast(new TmfSelectionRangeUpdatedSignal(event.getSource(), new TmfTimestamp(segment.getStart(), ITmfTimestamp.NANOSECOND_SCALE), new TmfTimestamp(segment.getEnd(), ITmfTimestamp.NANOSECOND_SCALE)));
                    }
                }
            }
        });
        reloadContents();

        fTablePopupMenuManager = new MenuManager();
        fTablePopupMenuManager.setRemoveAllWhenShown(true);

        fTablePopupMenuManager.addMenuListener(new IMenuListener() {
            @Override
            public void menuAboutToShow(final @Nullable IMenuManager manager) {
                ISelection selection = fTreeViewer.getSelection();
                if (selection instanceof IStructuredSelection) {
                    IStructuredSelection sel = (IStructuredSelection) selection;
                    if (manager != null) {
                        appendToTablePopupMenu(manager, sel);
                    }
                }
            }
        });

        Menu tablePopup = fTablePopupMenuManager.createContextMenu(fTreeViewer.getTree());
        fTreeViewer.getTree().setMenu(tablePopup);
    }

    @Override
    public Control getControl() {
        return fTreeViewer.getControl();
    }

    @Override
    public void refresh() {
        fTreeViewer.refresh();
    }

    /**
     * @return The tree viewer
     */
    public TreeViewer getTreeViewer() {
        return fTreeViewer;
    }

    /**
     * @return The current trace
     */
    public ITmfTrace getTrace() {
        return fTrace;
    }

    /**
     * @param column
     *            The current column
     * @param index
     *            The index of the column in the tree viewer
     * @param treeViewer
     *            The tree viewer that owns this column
     */
    private void addTreeColumnSelectionListener(final TreeColumn column, final int index, final TreeViewer treeViewer) {
        column.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                treeViewer.getTree().setSortColumn(column);
                Comparator<Object> comparator = getComparator(index, treeViewer);
                final Object root = treeViewer.getInput();
                sortEntries(comparator, root);
                treeViewer.refresh();
                switchSortDirection(treeViewer);
            }

            private void switchSortDirection(final TreeViewer tv) {
                if (tv.getTree().getSortDirection() == SWT.UP) {
                    tv.getTree().setSortDirection(SWT.DOWN);
                } else {
                    tv.getTree().setSortDirection(SWT.UP);
                }
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                /** Do nothing */
            }
        });
    }

    /**
     * @param treeViewer
     *            The tree viewer
     * @param name
     *            The name of the column
     * @param width
     *            The width of the column
     * @param provider
     *            The column label provider
     */
    protected static void createColumn(TreeViewer treeViewer, String name, int width, ColumnLabelProvider provider) {
        TreeViewerColumn col = new TreeViewerColumn(treeViewer, SWT.NONE);
        col.getColumn().setText(name);
        col.getColumn().setWidth(width);
        col.setLabelProvider(provider);
    }

    private void reloadContents() {
        ITmfTrace activeTrace = TmfTraceManager.getInstance().getActiveTrace();
        if (activeTrace == null) {
            /* No active trace, clear the view */
            clearAll();
            return;
        }
        reloadContents(activeTrace, activeTrace.getStartTime(), activeTrace.getEndTime());
    }

    private void clearAll() {
        fTreeViewer.setInput(null);
        refresh();
    }

    private void reloadContents(@NonNull final ITmfTrace trace, final ITmfTimestamp start, final ITmfTimestamp end) {
        if (fTrace != trace) {
            fTrace = trace;
        }
        if (Iterables.size(TmfTraceUtils.getAnalysisModulesOfClass(trace, XmlPatternStateSystemModule.class)) == 0) {
            clearAll();
            return;
        }
        if (fUpdateThread != null && fUpdateThread.isAlive()) {
            fUpdateThread.interrupt();
        }
        fUpdateThread = new Thread(new Runnable() {

            @Override
            public void run() {
                final Object root = computeEntries(fTrace, start, end);
                TmfUiRefreshHandler.getInstance().queueUpdate(this, new Runnable() {
                    @Override
                    public void run() {
                        fTreeViewer.setInput(root);
                        refresh();
                    }
                });
            }
        });
        fUpdateThread.start();
    }

    /**
     * @param signal
     *            The incoming signal
     */
    @TmfSignalHandler
    public void traceSelected(TmfTraceSelectedSignal signal) {
        ITmfTrace trace = signal.getTrace();
        if (trace != null) {
            reloadContents(trace, trace.getStartTime(), trace.getEndTime());
        }
    }

    /**
     * @param signal
     *            The incoming signal
     */
    @TmfSignalHandler
    public void traceClosed(TmfTraceClosedSignal signal) {
        /* The last opened trace was closed */
        clearAll();
    }

    /**
     * @param signal
     *            The incoming signal
     */
    @TmfSignalHandler
    public void selectionRangeUpdated(final TmfSelectionRangeUpdatedSignal signal) {
        if ((signal.getSource() != fTreeViewer) && (fTrace != null) && signal.getBeginTime().compareTo(signal.getEndTime()) != 0) {
            updateContents(NonNullUtils.checkNotNull(fTrace), signal.getBeginTime(), signal.getEndTime());
        }
    }

    /**
     * @param trace
     *            The current trace
     * @param beginTime
     *            The begin timestamp
     * @param endTime
     *            The end timestamp
     */
    protected void updateContents(ITmfTrace trace, ITmfTimestamp beginTime, ITmfTimestamp endTime) {
        if (fTrace != trace) {
            fTrace = trace;
        }
        final Object newInput = computeEntries(fTrace, beginTime, endTime);
        Object oldInput = fTreeViewer.getInput();
        updateTreeInput(fTreeViewer, oldInput, newInput);
    }

    /**
     * @param viewer
     *            The tree viewer of the view
     */
    protected abstract void createColumns(TreeViewer viewer);

    /**
     * @param trace
     *            The current trace
     * @param start
     *            the start timestamp
     * @param end
     *            The end timestamp
     * @return The entries used to populate the view
     */
    protected abstract Object computeEntries(ITmfTrace trace, ITmfTimestamp start, ITmfTimestamp end);

    /**
     * @param treeViewer
     *            The tree viewer of this view
     * @param parent
     *            The parent of the element to update
     * @param index
     *            The child index of the element to update
     */
    protected abstract void updateElement(TreeViewer treeViewer, Object parent, int index);

    /**
     * @param treeViewer
     *            The tree viewer of the view
     * @param root
     *            The root element of the tree viewer
     * @param element
     *            The element to update
     */
    protected abstract void updateChildCount(TreeViewer treeViewer, Object root, Object element);

    /**
     * @param comparator
     *            The comparator used to sort the entries
     * @param root
     *            The root element of the tree
     */
    protected abstract void sortEntries(Comparator<Object> comparator, Object root);

    /**
     * @param index
     *            The index of the column which we want the comparator
     * @param treeViewer
     *            The tree viewer that owns the column
     * @return The comparator to use to sort the entries
     */
    protected abstract Comparator<Object> getComparator(int index, TreeViewer treeViewer);

    /**
     * @param treeViewer
     *            The tree viewer that owns the column
     * @param oldInput
     *            The old tree input
     * @param newInput
     *            The new tree input
     */
    protected abstract void updateTreeInput(TreeViewer treeViewer, Object oldInput, Object newInput);

    /**
     * Abstract class to display the label of the columns
     *
     */
    protected static abstract class SegmentColumnLabelProvider extends ColumnLabelProvider {

        @Override
        public String getText(@Nullable Object input) {
            return getTextForColumn(input);
        }

        /**
         * @param o
         *            The entry
         * @return The label for this entry
         */
        public abstract String getTextForColumn(Object o);
    }

    /**
     * Method to add commands to the context sensitive menu.
     *
     * @param manager
     *            the menu manager
     * @param sel
     *            the current selection
     */
    protected void appendToTablePopupMenu(IMenuManager manager, IStructuredSelection sel) {
    }

    private static class LatencyContentProvider implements ILazyTreeContentProvider {

        private Object fInput;
        final TreeViewer fTreeViewer;
        final AbstractTmfLatencySegmentTreeViewer fViewer;

        public LatencyContentProvider(AbstractTmfLatencySegmentTreeViewer viewer) {
            this.fViewer = viewer;
            this.fTreeViewer = viewer.fTreeViewer;
        }

        @Override
        public void dispose() {
        }

        @Override
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
            if (newInput != oldInput) {
                setInput(newInput);
            }
        }

        @Override
        public void updateElement(Object parent, int index) {
            fViewer.updateElement(fTreeViewer, parent, index);
        }

        @Override
        public void updateChildCount(Object element, int currentChildCount) {
            fViewer.updateChildCount(fTreeViewer, fInput, element);
        }

        @Override
        public Object getParent(Object element) {
            return null;
        }

        public void setInput(Object input) {
            fInput = input;
        }
    }
}