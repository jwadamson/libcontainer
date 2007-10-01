package org.dbpowder.plugins.libcontainer;

import java.util.ArrayList;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jdt.internal.ui.dialogs.StatusInfo;
import org.eclipse.jdt.internal.ui.wizards.TypedElementSelectionValidator;
import org.eclipse.jdt.internal.ui.wizards.TypedViewerFilter;
import org.eclipse.jdt.internal.ui.wizards.buildpaths.FolderSelectionDialog;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.DialogField;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.IDialogFieldListener;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.IStringButtonAdapter;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.LayoutUtil;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.SelectionButtonDialogField;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.StringButtonDialogField;
import org.eclipse.jdt.ui.wizards.IClasspathContainerPage;
import org.eclipse.jdt.ui.wizards.IClasspathContainerPageExtension;
import org.eclipse.jdt.ui.wizards.NewElementWizardPage;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.ui.model.BaseWorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;

/**
 * @author Joris Verschoor <j.verschoor@insomniq.com>
 * @author Stuart McGrigor <Stuart@Retail.geek.nz>
 * @author Tadashi Murakami <tadashi@dbpowder.org>
 * 
 * The 'wizard' for configuring a new LibContainer which consists
 * of all JAR and ZIP files in a particular workbench folder.
 *  
 * TODO: Button "Edit Library" needs to either do the right thing
 *  or be turned off for these user defined libraries.
 * 
 * TODO: Need to be able to pick & choose JAR files in external dir
 * (so that Javadoc & Src jars don't clutter up the classpath 
 */
public class LibContainerPageProject extends NewElementWizardPage implements
		IClasspathContainerPage, IClasspathContainerPageExtension {

	private IJavaProject javaProject;
	private StringButtonDialogField fProjectButton;//, fFileSysButton;
	private SelectionButtonDialogField fRecurseField;

	/**
	 * Constructor for ClasspathContainerDefaultPage.
	 */
	public LibContainerPageProject() {
		super("LibContainerDefaultPage"); //$NON-NLS-1$
		setTitle(PluginUtils.getResourceString("project.selectfolder.title")); //$NON-NLS-1$
		setDescription(PluginUtils.getResourceString("project.selectfolder.description")); //$NON-NLS-1$
		setImageDescriptor(JavaPluginImages.DESC_WIZBAN_ADD_LIBRARY);

		fRecurseField = new SelectionButtonDialogField(SWT.CHECK); // SWT.CHECK = 32
		fRecurseField.setLabelText(PluginUtils.getResourceString("recursefolders")); //$NON-NLS-1$

		LibFolderAdapter adapter = new LibFolderAdapter();

		fProjectButton = new StringButtonDialogField(adapter);
		fProjectButton.setButtonLabel(PluginUtils.getResourceString("project.browse")); //$NON-NLS-1$
		fProjectButton.setDialogFieldListener(adapter);
		fProjectButton.setLabelText(PluginUtils.getResourceString("project.librarypath")); //$NON-NLS-1$

		validatePath();
	}

	private void validatePath() {
		StatusInfo status = new StatusInfo();
		String text = fProjectButton.getText();
		if (text.length() == 0) {
			status.setError(PluginUtils.getResourceString("project.path.error.missing")); //$NON-NLS-1$
		} else if (!Path.ROOT.isValidPath(text)) {
			status.setError(PluginUtils.getResourceString("project.path.error.invalid")); //$NON-NLS-1$
		} else {
			IPath path = new Path(text);
			if (path.segmentCount() == 0) {
				status.setError(PluginUtils.getResourceString("project.path.error.noroot")); //$NON-NLS-1$
			}
			//			else if (fUsedPaths.contains(path))
			//			{
			//				status.setError(LibContainer.getResourceString("project.path.error.duplicate"))")); //$NON-NLS-1$
			//			}
		}
		updateStatus(status);
	}

	/**
	 * @see IDialogPage#createControl(Composite)
	 */
	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, 0);
		LayoutUtil.doDefaultLayout(composite, new DialogField[] { fProjectButton, fRecurseField }, true);
		LayoutUtil.setHorizontalGrabbing(fProjectButton.getTextControl(null));

		fProjectButton.setFocus();

		setControl(composite);
	}

	/**
	 * @see IClasspathContainerPage#finish()
	 */
	public boolean finish() {
		return true;
	}

	/**
	 * @see IClasspathContainerPage#getSelection()
	 */
	public IClasspathEntry getSelection() {
		String libPath = fProjectButton.getText();
		boolean isRecurse = fRecurseField.getSelectionButton(null).getSelection();
		IPath containerPath = LibContainerInitializer.buildContainerPath(javaProject, libPath, isRecurse, false);
		PluginUtils.log("projGetSelection:" + containerPath.toString(), 1);
		return JavaCore.newContainerEntry(containerPath);
	}

	/**
	 * @see IClasspathContainerPage#setSelection(IClasspathEntry)
	 */
	public void setSelection(IClasspathEntry containerEntry) {
	}

	/**
	 * Copied from outputlocationdialog
	 */
	private IFolder chooseLibraryFolder() {
		final WorkbenchLabelProvider lp = new WorkbenchLabelProvider();
//		final BaseWorkbenchContentProvider cp = new BaseWorkbenchContentProvider();
		final ITreeContentProvider cp = new BaseWorkbenchContentProvider();
		final FolderSelectionDialog dlg = new FolderSelectionDialog(getShell(), lp, cp);

//		final Class<?>[] accceptedClasses = new Class[2];
//		try {
//			accceptedClasses[0] = Class.forName(
//					"[Lorg.eclipse.core.resources.IContainer;")
//					.getComponentType();
//			accceptedClasses[1] = Class.forName(
//					"[Lorg.eclipse.core.resources.IFolder;").getComponentType();
//			// aclass[0] =
//			// Class.forName("org.eclipse.core.resources.IContainer;");
//			// aclass[1] = Class.forName("org.eclipse.core.resources.IFolder;");
//		} catch (ClassNotFoundException e) {
//			e.printStackTrace();
//			throw new NoClassDefFoundError(e.getMessage());
//		}
		final Class<?>[] accceptedClasses = new Class[]{IContainer.class, IFolder.class};

		final IProject project = javaProject.getProject();
		final IWorkspaceRoot root = project.getWorkspace().getRoot();

		IResource initSelection = null;
		if (fProjectButton.getText() != null) {
			initSelection = project.findMember(fProjectButton.getText());
		}

		final ArrayList<IProject> rejectProjList = new ArrayList<IProject>();
		for (IProject proj : root.getProjects()) {
			if (!JavaProject.hasJavaNature(proj)) {
				rejectProjList.add(proj);
			}
		}

		final TypedElementSelectionValidator validator = new TypedElementSelectionValidator(accceptedClasses, false);
		final TypedViewerFilter filter = new TypedViewerFilter(accceptedClasses, rejectProjList.toArray());

		dlg.setTitle(PluginUtils.getResourceString("project.selectfolder.title"));
//		dlg.setTitle(NewWizardMessages.OutputLocationDialog_ChooseOutputFolder_title);
		dlg.setValidator(validator);

		dlg.setMessage(PluginUtils.getResourceString("project.selectfolder.description"));
//		dlg.setMessage(NewWizardMessages.OutputLocationDialog_ChooseOutputFolder_description);
		dlg.addFilter(filter);
		dlg.setInput(root);
		dlg.setInitialSelection(initSelection);

		// ResourceSorter not found...
		// dlg.setSorter(new ResourceSorter(1));
		// dlg.setSorter(new ResourceComparator());
		dlg.setSorter(new ViewerSorter());

		if (dlg.open() == ElementTreeSelectionDialog.OK) { // 0
			Object obj = dlg.getFirstResult();
			if (obj instanceof IFolder) {
				return (IFolder) obj;
			}
		}
		return null;
	}

	/**
	 * @see org.eclipse.jdt.ui.wizards.IClasspathContainerPageExtension#initialize(org.eclipse.jdt.core.IJavaProject, org.eclipse.jdt.core.IClasspathEntry[])
	 */
	public void initialize(IJavaProject project, IClasspathEntry[] currentEntries) {
		javaProject = project;
	}

	private void pathChangeControlPressed(DialogField field) {
		if (field != fProjectButton) {
			return;
		}
		IFolder ifolder = chooseLibraryFolder();
		if (ifolder == null) {
			return;
		}
		String libPath = ifolder.getFullPath().toString();
		libPath = libPath.substring(libPath.indexOf('/') + 1);

		final int idxSla = libPath.indexOf('/');
		final String projectName = libPath.substring(0, idxSla);
		final String folderName = libPath.substring(idxSla + 1);

		fProjectButton.setText("/" + projectName + "/" + folderName);
	}

	private void pathDialogFieldChanged(DialogField field) {
		if (field != fProjectButton) {
			return;
		}
		validatePath();
	}


	private class LibFolderAdapter implements IStringButtonAdapter, IDialogFieldListener {
		// -------- IStringButtonAdapter --------
		public void changeControlPressed(DialogField field) {
			pathChangeControlPressed(field);
		}
		// ---------- IDialogFieldListener --------
		public void dialogFieldChanged(DialogField field) {
			pathDialogFieldChanged(field);
		}
	}
}
