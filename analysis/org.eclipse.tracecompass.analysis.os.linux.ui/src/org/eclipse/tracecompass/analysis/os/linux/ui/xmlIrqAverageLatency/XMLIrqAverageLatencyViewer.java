/*******************************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Jean-christian Kouame - Initial API and implementation
 *******************************************************************************/
package org.eclipse.tracecompass.analysis.os.linux.ui.xmlIrqAverageLatency;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tracecompass.analysis.os.linux.ui.viewers.AbstractTmfLatencySegmentAnalysisViewer;
import org.eclipse.tracecompass.common.core.NonNullUtils;
import org.eclipse.tracecompass.internal.analysis.os.linux.ui.views.model.IRQStatInfos;
import org.eclipse.tracecompass.internal.analysis.os.linux.ui.views.resources.XmlIrqUtils;
import org.eclipse.tracecompass.internal.analysis.os.linux.ui.views.resources.XmlIrqUtils.STAT_TYPE;
import org.eclipse.tracecompass.internal.analysis.os.linux.ui.views.resources.XmlIrqUtils.TYPE;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
import org.eclipse.tracecompass.tmf.analysis.xml.core.model.TmfXmlSyntheticEvent;
import org.eclipse.tracecompass.tmf.analysis.xml.core.stateprovider.XmlPatternStateSystemModule;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.signal.TmfSelectionRangeUpdatedSignal;
import org.eclipse.tracecompass.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;

/**
 * This class displays the irq average latency analysis in a tree column table
 *
 */
public class XMLIrqAverageLatencyViewer extends AbstractTmfLatencySegmentAnalysisViewer {

    /**
     * @param parent
     *            The parent composite
     */
    public XMLIrqAverageLatencyViewer(Composite parent) {
        super(parent);
    }

    @Override
    protected void createColumns(TreeViewer viewer) {
        createColumn(viewer, Messages.XMLIrqAverageLatencyViewer_Interrupt, 200, new SegmentColumnLabelProvider() {
            @Override
            public String getTextForColumn(Object o) {
                IRQStatInfos irq = (IRQStatInfos) o;
                return irq.label;
            }
        });

        createColumn(viewer, Messages.XMLIrqAverageLatencyViewer_Count, 200, new SegmentColumnLabelProvider() {
            @Override
            public String getTextForColumn(Object o) {
                IRQStatInfos irq = (IRQStatInfos) o;
                return irq.type == STAT_TYPE.DATA ? String.valueOf(irq.count) : NonNullUtils.nullToEmptyString(null);
            }
        });

        createColumn(viewer, Messages.XMLIrqAverageLatencyViewer_Min, 200, new SegmentColumnLabelProvider() {
            @Override
            public String getTextForColumn(Object o) {
                IRQStatInfos irq = (IRQStatInfos) o;
                return irq.type == STAT_TYPE.DATA ? String.valueOf(irq.min) : NonNullUtils.nullToEmptyString(null);
            }
        });

        createColumn(viewer, Messages.XMLIrqAverageLatencyViewer_Max, 200, new SegmentColumnLabelProvider() {
            @Override
            public String getTextForColumn(Object o) {
                IRQStatInfos irq = (IRQStatInfos) o;
                return irq.type == STAT_TYPE.DATA ? String.valueOf(irq.max) : NonNullUtils.nullToEmptyString(null);
            }
        });

        createColumn(viewer, Messages.XMLIrqAverageLatencyViewer_Average, 300, new SegmentColumnLabelProvider() {
            @Override
            public String getTextForColumn(Object o) {
                IRQStatInfos irq = (IRQStatInfos) o;
                return irq.type == STAT_TYPE.DATA ? String.valueOf(irq.buffer / irq.count) : NonNullUtils.nullToEmptyString(null);
            }
        });
    }

    @Override
    protected IRQStatInfos computeEntries(ITmfTrace trace, ITmfTimestamp start, ITmfTimestamp end) {
        Map<String, IRQStatInfos> softIrqInfos = new HashMap<>();
        Map<String, IRQStatInfos> irqInfos = new HashMap<>();
        IRQStatInfos root = new IRQStatInfos("ROOT", STAT_TYPE.NO_DATA); //$NON-NLS-1$
        IRQStatInfos hard = new IRQStatInfos(Messages.XMLIrqAverageLatencyViewer_IRQ, STAT_TYPE.NO_DATA);
        IRQStatInfos soft = new IRQStatInfos(Messages.XMLIrqAverageLatencyViewer_SOFTIRQ, STAT_TYPE.NO_DATA);

        for (XmlPatternStateSystemModule module : TmfTraceUtils.getAnalysisModulesOfClass(NonNullUtils.checkNotNull(trace), XmlPatternStateSystemModule.class)) {
            module.waitForCompletion();
            for (ITmfStateSystem ssq : module.getStateSystems()) {
                if (ssq == null) {
                    return null;
                }
            }
            for (ITmfEvent event : module.getSyntheticEvents()) {
                if (XmlIrqUtils.validateEvent(event, start, end)) {
                    final TmfXmlSyntheticEvent synEvent = (TmfXmlSyntheticEvent) event;
                    if (event.getName().startsWith(XmlIrqUtils.SOFT_IRQ_PREFIX)) {
                        computeStatInfos(softIrqInfos, soft, TYPE.SOFTIRQ, synEvent);
                    } else {
                        computeStatInfos(irqInfos, hard, TYPE.IRQ, synEvent);
                    }
                }
            }
            Comparator<IRQStatInfos> comparator = new Comparator<IRQStatInfos>() {

                @Override
                public int compare(IRQStatInfos arg0, IRQStatInfos arg1) {
                    long avg0 = arg0.buffer / arg0.count;
                    long avg1 = arg1.buffer / arg1.count;
                    if (avg0 > avg1) {
                        return -1;
                    } else if (avg0 < avg1) {
                        return 1;
                    }
                    return 0;
                }
            };
            Collections.sort(hard.childs, comparator);
            Collections.sort(soft.childs, comparator);

            if (!hard.childs.isEmpty()) {
                root.childs.add(hard);
            }
            if (!soft.childs.isEmpty()) {
                root.childs.add(soft);
            }
        }
        return root;
    }

    private static void computeStatInfos(Map<String, IRQStatInfos> map, IRQStatInfos parent, TYPE type, final TmfXmlSyntheticEvent event) {
        String label = XmlIrqUtils.getIRQLabel(event, type);
        IRQStatInfos info = map.get(label);
        if (info == null) {
            info = new IRQStatInfos(label, STAT_TYPE.DATA);
            map.put(label, info);
            parent.childs.add(info);
        }
        updateInfos(event, info);
    }

    private static void updateInfos(final TmfXmlSyntheticEvent synEvent, IRQStatInfos info) {
        if (synEvent.getDuration() < info.min) {
            info.min = synEvent.getDuration();
            info.minEvent = synEvent;
        }
        if (synEvent.getDuration() > info.max) {
            info.max = synEvent.getDuration();
            info.maxEvent = synEvent;
        }
        info.buffer += synEvent.getDuration();
        info.count++;
    }

    @Override
    protected void updateElement(TreeViewer treeViewer, Object parent, int index) {
        treeViewer.replace(parent, index, ((IRQStatInfos) parent).childs.get(index));
        treeViewer.setChildCount(((IRQStatInfos) parent).childs.get(index), ((IRQStatInfos) parent).childs.get(index).childs.size());
    }

    @Override
    protected void updateChildCount(TreeViewer treeViewer, Object root, Object element) {
        int count = (root != null) ? ((IRQStatInfos) element).childs.size() : 0;
        treeViewer.setChildCount(element, count);
    }

    @Override
    protected void sortEntries(Comparator<Object> comparator, Object input) {
        IRQStatInfos irq = (IRQStatInfos) input;
        if (irq.childs.isEmpty()) {
            return;
        }
        for (IRQStatInfos child : irq.childs) {
            sortEntries(comparator, child);
        }
        Collections.sort(irq.childs, comparator);
    }

    @Override
    protected Comparator<Object> getComparator(int index, TreeViewer treeViewer) {
        Comparator<Object> comparator;
        final int isAscendingOrder = treeViewer.getTree().getSortDirection();
        switch (index) {
        case 0:
            comparator = getColumnInterruptComparator(isAscendingOrder);
            break;
        case 1:
            comparator = getColumnCountComparator(isAscendingOrder);
            break;
        case 2:
            comparator = getColumnMinComparator(isAscendingOrder);
            break;
        case 3:
            comparator = getColumnMaxComparator(isAscendingOrder);
            break;
        case 4:
            comparator = getColumnAverageComparator(isAscendingOrder);
            break;
        default:
            comparator = null;
            break;
        }
        return comparator;
    }

    private static Comparator<Object> getColumnAverageComparator(final int isAscendingOrder) {
        Comparator<Object> comparator = new Comparator<Object>() {
            @Override
            public int compare(Object o1, Object o2) {
                int toReturn = 0;
                if (o1 instanceof IRQStatInfos && o2 instanceof IRQStatInfos) {
                    IRQStatInfos irq1 = (IRQStatInfos) o1;
                    IRQStatInfos irq2 = (IRQStatInfos) o2;
                    if (irq1.type == STAT_TYPE.NO_DATA) {
                        toReturn = 0;
                    } else if (irq1.type != irq2.type) {
                        toReturn = irq1.type.toString().compareTo(irq2.type.toString());
                    } else {
                        long avg1 = irq1.buffer / irq1.count;
                        long avg2 = irq2.buffer / irq2.count;
                        if (avg1 > avg2) {
                            toReturn = 1;
                        } else if (avg1 < avg2) {
                            toReturn = -1;
                        } else {
                            toReturn = 0;
                        }
                    }
                }
                return (isAscendingOrder == SWT.DOWN ? toReturn : -toReturn);
            }
        };
        return comparator;
    }

    private static Comparator<Object> getColumnMaxComparator(final int isAscendingOrder) {
        Comparator<Object> comparator = new Comparator<Object>() {
            @Override
            public int compare(Object o1, Object o2) {
                int toReturn = 0;
                if (o1 instanceof IRQStatInfos && o2 instanceof IRQStatInfos) {
                    IRQStatInfos irq1 = (IRQStatInfos) o1;
                    IRQStatInfos irq2 = (IRQStatInfos) o2;
                    if (irq1.type == STAT_TYPE.NO_DATA) {
                        toReturn = 0;
                    } else if (irq1.type != irq2.type) {
                        toReturn = irq1.type.toString().compareTo(irq2.type.toString());
                    } else {
                        if (irq1.max > irq2.max) {
                            toReturn = 1;
                        } else if (irq1.max < irq2.max) {
                            toReturn = -1;
                        } else {
                            toReturn = 0;
                        }
                    }
                }
                return (isAscendingOrder == SWT.DOWN ? toReturn : -toReturn);
            }
        };
        return comparator;
    }

    private static Comparator<Object> getColumnMinComparator(final int isAscendingOrder) {
        Comparator<Object> comparator = new Comparator<Object>() {
            @Override
            public int compare(Object o1, Object o2) {
                int toReturn = 0;
                if (o1 instanceof IRQStatInfos && o2 instanceof IRQStatInfos) {
                    IRQStatInfos irq1 = (IRQStatInfos) o1;
                    IRQStatInfos irq2 = (IRQStatInfos) o2;
                    if (irq1.type == STAT_TYPE.NO_DATA) {
                        toReturn = 0;
                    } else if (irq1.type != irq2.type) {
                        toReturn = irq1.type.toString().compareTo(irq2.type.toString());
                    } else {
                        if (irq1.min > irq2.min) {
                            toReturn = 1;
                        } else if (irq1.min < irq2.min) {
                            toReturn = -1;
                        } else {
                            toReturn = 0;
                        }
                    }
                }
                return (isAscendingOrder == SWT.DOWN ? toReturn : -toReturn);
            }
        };
        return comparator;
    }

    private static Comparator<Object> getColumnCountComparator(final int isAscendingOrder) {
        Comparator<Object> comparator = new Comparator<Object>() {
            @Override
            public int compare(Object o1, Object o2) {
                int toReturn = 0;
                if (o1 instanceof IRQStatInfos && o2 instanceof IRQStatInfos) {
                    IRQStatInfos irq1 = (IRQStatInfos) o1;
                    IRQStatInfos irq2 = (IRQStatInfos) o2;
                    if (irq1.type == STAT_TYPE.NO_DATA) {
                        toReturn = 0;
                    } else if (irq1.type != irq2.type) {
                        toReturn = irq1.type.toString().compareTo(irq2.type.toString());
                    } else {
                        if (irq1.count > irq2.count) {
                            toReturn = 1;
                        } else if (irq1.count < irq2.count) {
                            toReturn = -1;
                        } else {
                            toReturn = 0;
                        }
                    }
                }
                return (isAscendingOrder == SWT.DOWN ? toReturn : -toReturn);
            }
        };
        return comparator;
    }

    private static Comparator<Object> getColumnInterruptComparator(final int isAscendingOrder) {
        Comparator<Object> comparator = new Comparator<Object>() {
            @Override
            public int compare(Object o1, Object o2) {
                int toReturn = 0;
                if (o1 instanceof IRQStatInfos && o2 instanceof IRQStatInfos) {
                    IRQStatInfos irq1 = (IRQStatInfos) o1;
                    IRQStatInfos irq2 = (IRQStatInfos) o2;
                    if (irq1.type == STAT_TYPE.NO_DATA) {
                        toReturn = 0;
                    } else if (irq1.type != irq2.type) {
                        toReturn = irq1.type.toString().compareTo(irq2.type.toString());
                    } else {
                        toReturn = irq1.label.compareTo(irq2.label);
                    }
                }
                return (isAscendingOrder == SWT.DOWN ? toReturn : -toReturn);
            }
        };
        return comparator;
    }

    @Override
    protected void updateTreeInput(TreeViewer treeViewer, Object oldInput, Object newInput) {
        if (oldInput instanceof IRQStatInfos && newInput instanceof IRQStatInfos) {
            ((IRQStatInfos) oldInput).childs = ((IRQStatInfos) newInput).childs;
            treeViewer.refresh();
        }
    }

    @Override
    protected void appendToTablePopupMenu(IMenuManager manager, IStructuredSelection sel) {
        final IRQStatInfos info = (IRQStatInfos) sel.getFirstElement();

        if (getTrace() != null && info != null && info.count > 0) {
            IAction gotoStartTime = new Action(Messages.XMLIrqAverageLatencyViewer_GoToMinIRQ) {
                @Override
                public void run() {
                    broadcast(new TmfSelectionRangeUpdatedSignal(getTreeViewer(), info.minEvent.getTimestamp(), info.minEvent.getTimestampEnd()));
                }
            };

            IAction gotoEndTime = new Action(Messages.XMLIrqAverageLatencyViewer_GoToMaxIRQ) {
                @Override
                public void run() {
                    broadcast(new TmfSelectionRangeUpdatedSignal(getTreeViewer(), info.maxEvent.getTimestamp(), info.maxEvent.getTimestampEnd()));
                }
            };

            manager.add(gotoStartTime);
            manager.add(gotoEndTime);
        }
        if (getTrace() != null) {
            IAction reset = new Action(Messages.XMLIrqAverageLatencyViewer_Reset) {
                @Override
                public void run() {
                    getTreeViewer().getTree().setSortDirection(SWT.NONE);
                    updateContents(getTrace(), getTrace().getStartTime(), getTrace().getEndTime());
                }
            };

            manager.add(reset);
        }
    }
}
