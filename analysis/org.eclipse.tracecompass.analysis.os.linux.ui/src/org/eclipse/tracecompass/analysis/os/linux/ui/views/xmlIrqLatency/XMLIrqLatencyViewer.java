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
package org.eclipse.tracecompass.analysis.os.linux.ui.views.xmlIrqLatency;

import java.util.Collections;
import java.util.Comparator;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tracecompass.analysis.os.linux.core.latency.irq.IRQ;
import org.eclipse.tracecompass.analysis.os.linux.core.latency.irq.XmlIrqUtils;
import org.eclipse.tracecompass.analysis.os.linux.core.latency.irq.XmlIrqUtils.TYPE;
import org.eclipse.tracecompass.analysis.os.linux.ui.viewers.AbstractTmfLatencySegmentTreeViewer;
import org.eclipse.tracecompass.common.core.NonNullUtils;
import org.eclipse.tracecompass.tmf.analysis.xml.core.model.TmfXmlSyntheticEvent;
import org.eclipse.tracecompass.tmf.analysis.xml.core.stateprovider.XmlPatternStateSystemModule;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;

/**
 * This class displays the irq latency analysis in a tree column table
 */
public class XMLIrqLatencyViewer extends AbstractTmfLatencySegmentTreeViewer {

    /**
     * Constructor
     *
     * @param parent
     *            The parent composite
     */
    public XMLIrqLatencyViewer(Composite parent) {
        super(parent);
    }

    @Override
    protected Comparator<Object> getComparator(final int index, final TreeViewer treeViewer) {
        Comparator<Object> comparator;
        final int isAscendingOrder = treeViewer.getTree().getSortDirection();
        switch (index) {
        case 0:
            comparator = getColumnInterruptComparator(isAscendingOrder);
            break;
        case 1:
            comparator = getColumnStartTimeComparator(isAscendingOrder);
            break;
        case 2:
            comparator = getColumnEndTimeComparator(isAscendingOrder);
            break;
        case 3:
            comparator = getColumnElapsedTimeComparator(isAscendingOrder);
            break;
        case 4:
            comparator = getColumnTimeToEntryComparator(isAscendingOrder);
            break;
        case 5:
            comparator = getColumnTimeToExitComparator(isAscendingOrder);
            break;
        default:
            comparator = null;
            break;
        }
        return comparator;
    }

    private static Comparator<Object> getColumnTimeToExitComparator(final int isAscendingOrder) {
        Comparator<Object> comparator;
        comparator = new Comparator<Object>() {
            @Override
            public int compare(Object o1, Object o2) {
                int toReturn = 0;
                if (o1 instanceof IRQ && o2 instanceof IRQ) {
                    IRQ irq1 = (IRQ) o1;
                    IRQ irq2 = (IRQ) o2;
                    if (irq1.type == TYPE.ROOT) {
                        toReturn = 1;
                    } else if (irq1.synIrq == null & irq2.synIrq == null) {
                        toReturn = 0;
                    } else if (irq1.type != irq2.type) {
                        toReturn = irq1.type.toString().compareTo(irq2.type.toString());
                    } else {
                        if (irq1.type == TYPE.IRQ) {
                            if (irq1.getLength() > irq2.getLength()) {
                                toReturn = 1;
                            } else if (irq1.getLength() < irq2.getLength()) {
                                toReturn = -1;
                            } else {
                                toReturn = 0;
                            }
                        } else if (irq1.type == TYPE.SOFTIRQ) {
                            long d1 = irq1.getD2();
                            long d2 = irq2.getD2();
                            if (d1 > d2) {
                                toReturn = 1;
                            } else if (d1 < d2) {
                                toReturn = -1;
                            } else {
                                toReturn = 0;
                            }
                        }
                    }
                }
                return (isAscendingOrder == SWT.DOWN ? toReturn : -toReturn);
            }
        };
        return comparator;
    }

    private static Comparator<Object> getColumnTimeToEntryComparator(final int isAscendingOrder) {
        Comparator<Object> comparator;
        comparator = new Comparator<Object>() {
            @Override
            public int compare(Object o1, Object o2) {
                int toReturn = 0;
                if (o1 instanceof IRQ && o2 instanceof IRQ) {
                    IRQ irq1 = (IRQ) o1;
                    IRQ irq2 = (IRQ) o2;
                    if (irq1.type == TYPE.ROOT) {
                        toReturn = 1;
                    } else if (irq1.synIrq == null & irq2.synIrq == null) {
                        toReturn = 0;
                    } else if (irq1.type != irq2.type) {
                        toReturn = irq1.type.toString().compareTo(irq2.type.toString());
                    } else {
                        if (irq1.type == TYPE.IRQ) {
                            return 0;
                        } else if (irq1.type == TYPE.SOFTIRQ) {
                            long d1 = irq1.getD1();
                            long d2 = irq2.getD1();
                            if (d1 > d2) {
                                toReturn = 1;
                            } else if (d1 < d2) {
                                toReturn = -1;
                            } else {
                                toReturn = 0;
                            }
                        }
                    }
                }
                return (isAscendingOrder == SWT.DOWN ? toReturn : -toReturn);
            }

        };
        return comparator;
    }

    private static Comparator<Object> getColumnElapsedTimeComparator(final int isAscendingOrder) {
        Comparator<Object> comparator;
        comparator = new Comparator<Object>() {
            @Override
            public int compare(Object o1, Object o2) {
                int toReturn = 0;
                if (o1 instanceof IRQ && o2 instanceof IRQ) {
                    IRQ irq1 = (IRQ) o1;
                    IRQ irq2 = (IRQ) o2;
                    if (irq1.type == TYPE.ROOT) {
                        toReturn = 1;
                    } else if (irq1.synIrq == null & irq2.synIrq == null) {
                        toReturn = 0;
                    } else if (irq1.type != irq2.type) {
                        toReturn = irq1.type.toString().compareTo(irq2.type.toString());
                    } else {
                        if (irq1.getLength() > irq2.getLength()) {
                            toReturn = 1;
                        } else if (irq1.getLength() < irq2.getLength()) {
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

    private static Comparator<Object> getColumnEndTimeComparator(final int isAscendingOrder) {
        Comparator<Object> comparator;
        comparator = new Comparator<Object>() {
            @Override
            public int compare(Object o1, Object o2) {
                int toReturn = 0;
                if (o1 instanceof IRQ && o2 instanceof IRQ) {
                    IRQ irq1 = (IRQ) o1;
                    IRQ irq2 = (IRQ) o2;
                    if (irq1.type == TYPE.ROOT) {
                        toReturn = 1;
                    } else if (irq1.synIrq == null & irq2.synIrq == null) {
                        toReturn = 0;
                    } else if (irq1.type != irq2.type) {
                        toReturn = irq1.type.toString().compareTo(irq2.type.toString());
                    } else {
                        if (irq1.getEnd() > irq2.getEnd()) {
                            toReturn = 1;
                        } else if (irq1.getEnd() < irq2.getEnd()) {
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

    private static Comparator<Object> getColumnStartTimeComparator(final int isAscendingOrder) {
        Comparator<Object> comparator;
        comparator = new Comparator<Object>() {
            @Override
            public int compare(Object o1, Object o2) {
                int toReturn = 0;
                if (o1 instanceof IRQ && o2 instanceof IRQ) {
                    IRQ irq1 = (IRQ) o1;
                    IRQ irq2 = (IRQ) o2;
                    if (irq1.type == TYPE.ROOT) {
                        toReturn = 1;
                    } else if (irq1.synIrq == null & irq2.synIrq == null) {
                        toReturn = 0;
                    } else if (irq1.type != irq2.type) {
                        toReturn = irq1.type.toString().compareTo(irq2.type.toString());
                    } else {
                        if (irq1.getStart() > irq2.getStart()) {
                            toReturn = 1;
                        } else if (irq1.getStart() < irq2.getStart()) {
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
        Comparator<Object> comparator;
        comparator = new Comparator<Object>() {
            @Override
            public int compare(Object o1, Object o2) {
                int toReturn = 0;
                if (o1 instanceof IRQ && o2 instanceof IRQ) {
                    IRQ irq1 = (IRQ) o1;
                    IRQ irq2 = (IRQ) o2;
                    if (irq1.type == TYPE.ROOT) {
                        toReturn = 1;
                    } else if (irq1.synIrq == null & irq2.synIrq == null) {
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
    protected void sortEntries(Comparator<Object> comparator, Object input) {
        IRQ irq = (IRQ) input;
        if (irq.childs.isEmpty()) {
            return;
        }
        for (IRQ child : irq.childs) {
            sortEntries(comparator, child);
        }
        Collections.sort(irq.childs, comparator);
    }

    @Override
    protected void createColumns(TreeViewer viewer) {
        createColumn(viewer, Messages.XMLIrqLatencyViewer_Level, 200, new SegmentColumnLabelProvider() {
            @Override
            public String getTextForColumn(Object o) {
                IRQ irq = (IRQ) o;
                return irq.label;
            }
        });

        createColumn(viewer, Messages.XMLIrqLatencyViewer_StartTime, 200, new SegmentColumnLabelProvider() {
            @Override
            public String getTextForColumn(Object o) {
                IRQ irq = (IRQ) o;
                return irq.synIrq != null ? irq.synIrq.getTimestamp().toString() : NonNullUtils.nullToEmptyString(null);
            }
        });

        createColumn(viewer, Messages.XMLIrqLatencyViewer_EndTime, 200, new SegmentColumnLabelProvider() {
            @Override
            public String getTextForColumn(Object o) {
                IRQ irq = (IRQ) o;
                return irq.synIrq != null ? irq.synIrq.getTimestampEnd().toString() : NonNullUtils.nullToEmptyString(null);
            }
        });

        createColumn(viewer, Messages.XMLIrqLatencyViewer_Duration, 300, new SegmentColumnLabelProvider() {
            @Override
            public String getTextForColumn(Object o) {
                IRQ irq = (IRQ) o;
                return irq.synIrq != null ? String.valueOf(irq.synIrq.getDuration()) : NonNullUtils.nullToEmptyString(null);
            }
        });

        createColumn(viewer, Messages.XMLIrqLatencyViewer_TimeToEentry, 300, new SegmentColumnLabelProvider() {
            @Override
            public String getTextForColumn(Object o) {
                IRQ irq = (IRQ) o;
                switch (irq.type) {
                case IRQ:
                    if (irq.synIrq != null) {
                        return Messages.XMLIrqLatencyViewer_NonApplicable;
                    }
                    return NonNullUtils.nullToEmptyString(null);
                case SOFTIRQ:
                    if (irq.synIrq != null) {
                        TmfXmlSyntheticEvent synEvent = irq.synIrq;
                        long delta = irq.getD1();
                        double percent = (delta / (double) synEvent.getDuration()) * 100.0;
                        StringBuilder label = new StringBuilder();
                        label.append(delta);
                        label.append("\t\t("); //$NON-NLS-1$
                        label.append(Math.round(percent));
                        label.append("%)"); //$NON-NLS-1$
                        return label.toString();
                    }
                    return NonNullUtils.nullToEmptyString(null);
                case ROOT:
                default:
                    return NonNullUtils.nullToEmptyString(null);
                }
            }
        });

        createColumn(viewer, Messages.XMLIrqLatencyViewer_TimeToExit, 300, new SegmentColumnLabelProvider() {
            @Override
            public String getTextForColumn(Object o) {
                IRQ irq = (IRQ) o;
                switch (irq.type) {
                case IRQ:
                    if (irq.synIrq != null) {
                        TmfXmlSyntheticEvent synEvent = irq.synIrq;
                        int percent = 100;
                        StringBuilder label = new StringBuilder();
                        label.append(synEvent.getDuration());
                        label.append("\t\t("); //$NON-NLS-1$
                        label.append(percent);
                        label.append("%)"); //$NON-NLS-1$
                        return label.toString();
                    }
                    return NonNullUtils.nullToEmptyString(null);
                case SOFTIRQ:
                    if (irq.synIrq != null) {
                        TmfXmlSyntheticEvent synEvent = irq.synIrq;
                        long delta = irq.getD2();
                        double percent = (delta / (double) synEvent.getDuration()) * 100.0;
                        StringBuilder label = new StringBuilder();
                        label.append(delta);
                        label.append("\t\t("); //$NON-NLS-1$
                        label.append(Math.round(percent));
                        label.append("%)"); //$NON-NLS-1$
                        return label.toString();
                    }
                    return NonNullUtils.nullToEmptyString(null);
                case ROOT:
                default:
                    return NonNullUtils.nullToEmptyString(null);
                }
            }
        });
    }

    @Override
    protected IRQ computeEntries(ITmfTrace trace, ITmfTimestamp start, ITmfTimestamp end) {
        IRQ root = new IRQ(null, TYPE.ROOT);
        IRQ hard = new IRQ(null, TYPE.IRQ);
        IRQ soft = new IRQ(null, TYPE.SOFTIRQ);
        for (XmlPatternStateSystemModule module : TmfTraceUtils.getAnalysisModulesOfClass(NonNullUtils.checkNotNull(trace), XmlPatternStateSystemModule.class)) {
            module.waitForCompletion();
            for (ITmfEvent event : module.getSyntheticEvents()) {
                if (XmlIrqUtils.validateEvent(event, start, end)) {
                    if (event.getName().startsWith(XmlIrqUtils.SOFT_IRQ_PREFIX)) {
                        IRQ irq = new IRQ((TmfXmlSyntheticEvent) event, TYPE.SOFTIRQ);
                        soft.childs.add(irq);
                    } else {
                        IRQ irq = new IRQ((TmfXmlSyntheticEvent) event, TYPE.IRQ);
                        hard.childs.add(irq);
                    }
                }
            }
        }
        if (!hard.childs.isEmpty()) {
            root.childs.add(hard);
        }
        if (!soft.childs.isEmpty()) {
            root.childs.add(soft);
        }
        return root;
    }

    @Override
    protected void updateElement(TreeViewer treeViewer, Object parent, int index) {
        treeViewer.replace(parent, index, ((IRQ) parent).childs.get(index));
        treeViewer.setChildCount(((IRQ) parent).childs.get(index), ((IRQ) parent).childs.get(index).childs.size());
    }

    @Override
    protected void updateChildCount(TreeViewer treeViewer, Object root, Object element) {
        int count = (root != null) ? ((IRQ) element).childs.size() : 0;
        treeViewer.setChildCount(element, count);
    }

    @Override
    protected void updateTreeInput(TreeViewer treeViewer, Object oldInput, Object newInput) {
        if (oldInput instanceof IRQ && newInput instanceof IRQ) {
            ((IRQ) oldInput).childs = ((IRQ) newInput).childs;
            treeViewer.refresh();
        }
    }

    @Override
    protected void appendToTablePopupMenu(IMenuManager manager, IStructuredSelection sel) {
        if (getTrace() != null) {
            IAction reset = new Action(Messages.XMLIrqLatencyViewer_Reset) {
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