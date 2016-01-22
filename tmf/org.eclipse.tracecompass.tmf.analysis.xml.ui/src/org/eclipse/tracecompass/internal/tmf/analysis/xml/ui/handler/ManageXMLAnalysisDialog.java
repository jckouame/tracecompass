package org.eclipse.tracecompass.internal.tmf.analysis.xml.ui.handler;

import java.io.File;
import java.util.ArrayList;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.tracecompass.tmf.analysis.xml.core.module.XmlUtils;
import org.eclipse.tracecompass.tmf.analysis.xml.ui.module.Messages;
import org.eclipse.tracecompass.tmf.analysis.xml.ui.module.XmlAnalysisModuleSource;
import org.eclipse.tracecompass.tmf.core.parsers.custom.CustomTraceDefinition;
import org.eclipse.tracecompass.tmf.core.parsers.custom.CustomXmlTraceDefinition;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfProjectElement;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfProjectModelElement;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfProjectRegistry;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfTraceElement;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfTraceFolder;
import org.eclipse.tracecompass.tmf.ui.project.model.TraceUtils;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

/**
 * @author Jean-Christian Kouame
 *
 */
public class ManageXMLAnalysisDialog extends Dialog{

    private static final String SEP = " : "; //$NON-NLS-1$
    private static final int SEP_LEN = SEP.length();

//    private static final Image image = Activator.getDefault().getImageFromPath("/icons/etool16/customparser_wizard.gif"); //$NON-NLS-1$

    List analysisList;
    Button deleteButton;
    Button importButton;
    Button exportButton;

    /**
     * Constructor
     *
     * @param parent
     *            Parent shell of this dialog
     */
    public ManageXMLAnalysisDialog(Shell parent) {
        super(parent);
        setShellStyle(SWT.RESIZE | SWT.MAX | getShellStyle());
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        getShell().setText("Manage XML analyses files"); //$NON-NLS-1$
//        getShell().setImage(image);

        Composite composite = (Composite) super.createDialogArea(parent);
        composite.setLayout(new GridLayout(2, false));

        Composite listContainer = new Composite(composite, SWT.NONE);
        listContainer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        GridLayout lcgl = new GridLayout();
        lcgl.marginHeight = 0;
        lcgl.marginWidth = 0;
        listContainer.setLayout(lcgl);

        analysisList = new List(listContainer, SWT.SINGLE | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
        analysisList.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        analysisList.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetDefaultSelected(SelectionEvent e) {}

            @Override
            public void widgetSelected(SelectionEvent e) {
                if (analysisList.getSelectionCount() == 0) {
                    deleteButton.setEnabled(false);
                    exportButton.setEnabled(false);
                } else {
                    deleteButton.setEnabled(true);
                    exportButton.setEnabled(true);
                }
            }
        });

        Composite buttonContainer = new Composite(composite, SWT.NULL);
        buttonContainer.setLayout(new GridLayout());
        buttonContainer.setLayoutData(new GridData(SWT.CENTER, SWT.TOP, false, false));

        new Label(buttonContainer, SWT.NONE); // filler

        importButton = new Button(buttonContainer, SWT.PUSH);
        importButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
        importButton.setText("Import");
        importButton.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetDefaultSelected(SelectionEvent e) {}

            @Override
            public void widgetSelected(SelectionEvent e) {
                FileDialog dialog = new FileDialog(Display.getCurrent().getActiveShell(), SWT.OPEN);
                dialog.setText("Select XML file to import");
                dialog.setFilterNames(new String[] { Messages.ImportXmlHandler_ImportXmlFile + " (*.xml)" }); //$NON-NLS-1$
                dialog.setFilterExtensions(new String[] { "*.xml" }); //$NON-NLS-1$
                String path = dialog.open();
                if (path != null) {
                    File file = new File(path);
                    IStatus status = XmlUtils.xmlValidate(file);
                    if (status.isOK()) {
                        status = XmlUtils.addXmlFile(file);
                        if (status.isOK()) {
                            fillParserList();
                            XmlAnalysisModuleSource.notifyModuleChange();
                            /*
                             * FIXME: It refreshes the list of analysis under a trace,
                             * but since modules are instantiated when the trace opens,
                             * the changes won't apply to an opened trace, it needs to
                             * be closed then reopened
                             */
                            refreshProject();
                        } else {
                            TraceUtils.displayErrorMsg(Messages.ImportXmlHandler_ImportXmlFile, status.getMessage());
                        }
                    } else {
                        TraceUtils.displayErrorMsg(Messages.ImportXmlHandler_ImportXmlFile, status.getMessage());
                    }
                }
            }
        });

        exportButton = new Button(buttonContainer, SWT.PUSH);
        exportButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
        exportButton.setText("Export");
        exportButton.setEnabled(false);
        exportButton.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetDefaultSelected(SelectionEvent e) {}

            @Override
            public void widgetSelected(SelectionEvent e) {
                FileDialog dialog = new FileDialog(Display.getCurrent().getActiveShell(), SWT.SAVE);
                dialog.setText(NLS.bind("select file ti export {0}", analysisList.getSelection()[0]));
                dialog.setFilterExtensions(new String[] { "*.xml", "*" }); //$NON-NLS-1$ //$NON-NLS-2$
                String path = dialog.open();
                if (path != null) {
                    String selection = analysisList.getSelection()[0];
                    String category = selection.substring(0, selection.indexOf(SEP));
                    String name = selection.substring(selection.indexOf(SEP) + SEP_LEN);
                    CustomTraceDefinition def = null;
                        def = CustomXmlTraceDefinition.load(category, name);
                    if (def != null) {
                        def.save(path);
                    }
                }
            }
        });

        deleteButton = new Button(buttonContainer, SWT.PUSH);
        deleteButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
        deleteButton.setText("Delete");
        deleteButton.setEnabled(false);
        deleteButton.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetDefaultSelected(SelectionEvent e) {}

            @Override
            public void widgetSelected(SelectionEvent e) {
                boolean confirm = MessageDialog.openQuestion(
                        getShell(),
                        "Delete XML file",
                        NLS.bind("Are you sure you want to delete {0}", analysisList.getSelection()[0]));
                if (confirm) {
                    String selection = analysisList.getSelection()[0];
                    deleteSupplementaryFile(selection);
                    XmlUtils.deleteFile(selection);
                    fillParserList();
                    XmlAnalysisModuleSource.notifyModuleChange();
                    /*
                     * FIXME: It refreshes the list of analysis under a trace,
                     * but since modules are instantiated when the trace opens,
                     * the changes won't apply to an opened trace, it needs to
                     * be closed then reopened
                     */
                    refreshProject();
                }
            }


        });

        fillParserList();

        getShell().setMinimumSize(300, 275);
        return composite;
    }

  //look for all traces that have this analysis
    //close them if needed
    //delete the supplementary file for this analysis
    private static void deleteSupplementaryFile(String selection) {
        java.util.List<IResource> resourceToDelete = new ArrayList<>();
        java.util.List<String> ids = XmlUtils.getIdsFromFile(selection);
        IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects(0);
        for (IProject project : projects) {
            TmfProjectElement pElement = TmfProjectRegistry.getProject(project);
            if (pElement != null) {
                TmfTraceFolder tracesFolder = pElement.getTracesFolder();
                for (TmfTraceElement tElement : tracesFolder.getTraces()) {
                    boolean closeEditor = false;
                    for (IResource resource : tElement.getSupplementaryResources()) {
                        for (String id : ids) {
                            if (resource.getName().startsWith(id)) {
                                resourceToDelete.add(resource);
                                closeEditor = true;
                            }
                        }
                    }
                    if (closeEditor) {
                        tElement.closeEditors();
                    }
                }
            }
        }
        for (IResource resource : resourceToDelete) {
            try {
                System.out.println(resource.getFullPath());
                resource.delete(false, null);
            } catch (CoreException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.OK_ID, IDialogConstants.CLOSE_LABEL, false);
    }

    private void fillParserList() {
        analysisList.removeAll();
        XmlUtils.loadFile();
            for (String file : XmlUtils.files.keySet()) {
                analysisList.add(file.replace(".xml", ""));
            }
        deleteButton.setEnabled(false);
        exportButton.setEnabled(false);
    }

    /**
     * Refresh the selected project with the new XML file import
     */
    private static void refreshProject() {
        // Check if we are closing down
        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if (window == null) {
            return;
        }

        // Get the selection
        IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        IWorkbenchPart part = page.getActivePart();
        if (part == null) {
            return;
        }
        ISelectionProvider selectionProvider = part.getSite().getSelectionProvider();
        if (selectionProvider == null) {
            return;
        }
        ISelection selection = selectionProvider.getSelection();

        if (selection instanceof TreeSelection) {
            TreeSelection sel = (TreeSelection) selection;
            // There should be only one item selected as per the plugin.xml
            Object element = sel.getFirstElement();
            if (element instanceof TmfProjectModelElement) {
                ((TmfProjectModelElement) element).getProject().refresh();
            }
        }
    }
}
