package org.dbpowder.plugins.libcontainer;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jdt.internal.ui.dialogs.StatusInfo;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.DialogField;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.IDialogFieldListener;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.IStringButtonAdapter;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.LayoutUtil;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.SelectionButtonDialogField;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.StringButtonDialogField;
import org.eclipse.jdt.ui.wizards.IClasspathContainerPage;
import org.eclipse.jdt.ui.wizards.IClasspathContainerPageExtension;
import org.eclipse.jdt.ui.wizards.NewElementWizardPage;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;

/**
 * @author Joris Verschoor <j.verschoor@insomniq.com>
 * @author Stuart McGrigor <Stuart@Retail.geek.nz>
 * 
 * The 'wizard' for configuring a new LibContainer which consists
 * of all JAR and ZIP files in a particular files system directory.
 *  
 * TODO: Button "Edit Library" needs to either do the right thing
 *  or be turned off for these user defined libraries.
 * 
 * TODO: Need to be able to pick & choose JAR files in external dir
 * (so that Javadoc & Src jars don't clutter up the classpath
 */
public class LibContainerPageFileSys extends NewElementWizardPage implements IClasspathContainerPage, IClasspathContainerPageExtension
{
	private IJavaProject javaProject;
	private final StringButtonDialogField fFileSysButton;
	private final SelectionButtonDialogField fRecurseField;

	/**
	 * Constructor for ClasspathContainerDefaultPage.
	 */
	public LibContainerPageFileSys()
	{
		super("LibContainerDefaultPage"); //$NON-NLS-1$
		setTitle(PluginUtils.getResourceString("filesys.selectfolder.title")); //$NON-NLS-1$
		setDescription(PluginUtils.getResourceString("filesys.selectfolder.description")); //$NON-NLS-1$
		setImageDescriptor(JavaPluginImages.DESC_WIZBAN_ADD_LIBRARY);

		fRecurseField = new SelectionButtonDialogField(SWT.CHECK);
		fRecurseField.setLabelText(PluginUtils.getResourceString("recursefolders")); //$NON-NLS-1$

		LibFolderAdapter adapter = new LibFolderAdapter();

		fFileSysButton = new StringButtonDialogField(adapter);
		fFileSysButton.setButtonLabel(PluginUtils.getResourceString("filesys.browse")); //$NON-NLS-1$

		fFileSysButton.setDialogFieldListener(adapter);
		fFileSysButton.setLabelText(PluginUtils.getResourceString("filesys.librarypath")); //$NON-NLS-1$

		validatePath();
	}

	private void validatePath()
	{
		StatusInfo status = new StatusInfo();
		String str = fFileSysButton.getText();
		if (str.length() == 0)
		{
			status.setError(PluginUtils.getResourceString("filesys.path.error.missing")); //$NON-NLS-1$
		}
		else if (!Path.ROOT.isValidPath(str))
		{
			status.setError(PluginUtils.getResourceString("filesys.path.error.invalid")); //$NON-NLS-1$
		}
		else
		{
			IPath path = new Path(str);
			if (path.segmentCount() == 0)
			{
				status.setError(PluginUtils.getResourceString("filesys.path.error.noroot")); //$NON-NLS-1$
			}
		}
		updateStatus(status);
	}

	/* (non-Javadoc)
	 * @see IDialogPage#createControl(Composite)
	 */
	public void createControl(Composite parent)
	{
		Composite composite = new Composite(parent, SWT.NONE);
		LayoutUtil.doDefaultLayout(composite, new DialogField[] {fFileSysButton, fRecurseField }, true);
		LayoutUtil.setHorizontalGrabbing(fFileSysButton.getTextControl(null));

		fFileSysButton.setFocus();

		setControl(composite);
	}

	/* (non-Javadoc)
	 * @see IClasspathContainerPage#finish()
	 */
	public boolean finish()
	{
		return true;
	}

	/* (non-Javadoc)
	 * @see IClasspathContainerPage#getSelection()
	 */
	public IClasspathEntry getSelection()
	{
		String libPath = fFileSysButton.getText();
		boolean recurse = fRecurseField.getSelectionButton(null).getSelection();
		IPath containerPath = LibContainerInitializer.buildContainerPath(javaProject, libPath, recurse, true);
		PluginUtils.log("fileGetSelection:" + containerPath.toString(), 1);				
		return JavaCore.newContainerEntry(containerPath);
	}

	/* (non-Javadoc)
	 * @see IClasspathContainerPage#setSelection(IClasspathEntry)
	 */
	public void setSelection(IClasspathEntry containerEntry)
	{
	}

	/*
	 * Just show off a DirectoryDialog
	 */
	private String chooseLibraryFolder()
	{
		DirectoryDialog dialog = new DirectoryDialog(getShell());
		
		dialog.setText(PluginUtils.getResourceString("filesys.selectfolder.title"));
		dialog.setMessage(PluginUtils.getResourceString("filesys.selectfolder.description"));
	
		String dir = dialog.open();
		
		if (dir != null) {
			return dir;
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.ui.wizards.IClasspathContainerPageExtension#initialize(org.eclipse.jdt.core.IJavaProject, org.eclipse.jdt.core.IClasspathEntry[])
	 */
	public void initialize(IJavaProject project, IClasspathEntry[] currentEntries)
	{
		this.javaProject = project;
	}

	private void pathChangeControlPressed(DialogField field)
	{
		if (field == fFileSysButton)
		{
			String dirPath = chooseLibraryFolder();
			if (dirPath != null) {
				fFileSysButton.setText(dirPath);
			}
		}
	}

	private void pathDialogFieldChanged(DialogField field)
	{
		if (field == fFileSysButton)
		{
			validatePath();
		}
	}

	private class LibFolderAdapter implements IStringButtonAdapter, IDialogFieldListener
	{

		// -------- IStringButtonAdapter --------
		public void changeControlPressed(DialogField field)
		{
			pathChangeControlPressed(field);
		}

		// ---------- IDialogFieldListener --------
		public void dialogFieldChanged(DialogField field)
		{
			pathDialogFieldChanged(field);
		}
	}
}
