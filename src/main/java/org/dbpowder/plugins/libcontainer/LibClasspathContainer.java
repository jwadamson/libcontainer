package org.dbpowder.plugins.libcontainer;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.*;

/**
 * @author Joris Verschoor <j.verschoor@insomniq.com>
 * @author Stuart McGrigor <Stuart@Retail.geek.nz>
 * @author Tadashi Murakami <tadashi@dbpowder.org>
 *
 * This ClasspathContainer will add all zip and jar files of a selected folder
 * If recurse is true, it will recurse subdirectories.
 * If fileSys is true, the folder is a file system directory external to workbench
 */
public class LibClasspathContainer implements IClasspathContainer {
	public static final String CLASSPATH_CONTAINER_ID = "org.dbpowder.plugins.LIB_CONTAINER"; //$NON-NLS-1$
//	public static final String CLASSPATH_CONTAINER_ID = "com.insomniq.eclipse.jdt.LIB_CONTAINER"; //$NON-NLS-1$

	final private IJavaProject project;
	final private String libPath;
	final private boolean isRecurse;
	final private String javaVer;
	private boolean fileSys;

	private IClasspathEntry[] entryArrCache;

	public LibClasspathContainer(IJavaProject project, String libPath, boolean isRecurse, boolean fileSys, String javaVer) {
		this.project = project;
		this.libPath = libPath;
		this.isRecurse = isRecurse;
		this.fileSys = fileSys;
		this.javaVer = javaVer;
	}

	public void reset() {
		entryArrCache = null;
	}

	/**
	 * @see org.eclipse.jdt.core.IClasspathContainer#getClasspathEntries()
	 */
	public IClasspathEntry[] getClasspathEntries() {
		if (javaVer != null) {
			try {

			} catch (Exception e) {
				PluginUtils.log(e);
			}
		}
		
		List<IClasspathEntry> entryList = new ArrayList<IClasspathEntry>();
		// Is this an external file system folder ??			
		if(fileSys) {
			addJars(entryList, new File(libPath));
		} else {
			try {
				final int idxSla = libPath.indexOf('/');
				final String projectName = libPath.substring(0, idxSla);
				final String newPath = libPath.substring(idxSla + 1);
				// The root folder, represented as an IFolder
				IFolder libFolder = project.getResource().getWorkspace().getRoot()
						.getProject(projectName).getFolder(newPath);

				// Add all jars in the folder 
				addJars(entryList, libFolder, getResolvedJarZipClasspath(true));

			} catch (Exception e) {
				PluginUtils.log(e);
				return new IClasspathEntry[0];
			}
		}

		// Assemble the array of classPath entries
		entryArrCache = (IClasspathEntry[]) entryList.toArray(new IClasspathEntry[0]);
		return entryArrCache;
	}

	private Set<String> getResolvedJarZipClasspath(boolean flag) {
		IClasspathEntry[] classpathArr;
		try {
			classpathArr = project.getRawClasspath();
			// classpathArr = project.getResolvedClasspath(flag);
		} catch (JavaModelException e) {
			throw new RuntimeException(e);
		}
		Set<String> ret = new HashSet<String>();
		for (IClasspathEntry classpath : classpathArr) {
			if (!PluginUtils.isLibraryFile(classpath.getPath()
					.getFileExtension())) {
				continue;
			}
			IPath path = classpath.getPath();
			ret.add(path.toString());
		}
		return ret;
	}

	/**
	 * Adds all jars in folder <code>folder</code> in the list <code>entryList</code>
	 * if <code>recurse</code> is true, it will recursively call this method to add
	 * any underlying folders.
	 * 
	 * @param entryList the list that will contain all jars
	 * @param folder the folder which will be scanned
	 */
	private void addJars(List<IClasspathEntry> entryList, IFolder folder, Set<String> excludeSet) {
		try {
			Map<String, IClasspathEntry> excludeFoundSet = new HashMap<String, IClasspathEntry>();
//			IResource[] jars = ifolder.members();
			for (IResource jar: folder.members()) {
//				IResource jar = jars[i];
				if (jar.getType() == IResource.FILE) {
					if (!PluginUtils.isLibraryFile(jar.getFileExtension())) {
						continue;
					}
					IPath ipath = null;
					try {
						ipath = jar.getFullPath();
					} catch (Exception e) {
						PluginUtils.log(e, "Failed to get projectRelativePath");
					}
					boolean isExported = true;
					if (ipath == null) {
						isExported = false;
						ipath = jar.getLocation();
					}
					IClasspathEntry entry;
					try {
						entry = JavaCore.newLibraryEntry(ipath, null, null, isExported);
					} catch (Exception e) {
						PluginUtils.log(e, "Failied to get newLibraryEntry");
						entry = JavaCore.newLibraryEntry(jar.getLocation(), null, null, false);
					}
					if (entry == null) {
						PluginUtils.log("Cannot find the entry: " + ipath, Status.ERROR);
						continue;
					}
					final String path = entry.getPath().toString();
					if (path == null) {
						continue;
					}

					if (excludeSet.contains(path)) {
						IClasspathEntry dup = excludeFoundSet.get(path);
						if (dup == null) {
							entryList.add(entry);
							excludeFoundSet.put(path, entry);
						} else {
							entryList.remove(dup);
						}
					} else {
						entryList.add(entry);
					}
				} else if (jar.getType() == IResource.FOLDER && isRecurse) {
					addJars(entryList, project.getResource().getWorkspace()
							.getRoot().getFolder(jar.getFullPath()),
							excludeSet);
				}
			}
		} catch (CoreException coreexception) {
			PluginUtils.log(coreexception, "Error adding folder " + folder);
			return;
		}
	}

	/**
 	* Adds all <code>JAR</code> &amp; <code>ZIP</code> files in the given
 	* <code>dir</code>  to the given <code>entryList</code>.
 	* 
 	* @param entryList the list that will contain all jars.
 	* @param dir path of the file system directory to scan.
 	*/
	private void addJars(List<IClasspathEntry> entryList, File dir)
	{
		File[] files = dir.listFiles();
		if (files==null){
		    return;
		}
				
		for (int i = 0; i < files.length; i++) {		
			// If it is a file
			if (files[i].isFile()) {
				String fName = files[i].getName().toLowerCase();
			
				// Does this one end with ".jar" ??
				if(fName.endsWith(".jar") || fName.endsWith(".zip")) {
					IClasspathEntry entry = JavaCore.newLibraryEntry(new Path(files[i].getPath()), null, null);
					if (entry != null) {
						entryList.add(entry);
					}
				}
			} else if (files[i].isDirectory() && isRecurse) {
				// Go get the JARs and ZIPs from this directory 
				addJars(entryList, files[i]);
			}
		}
	}

	/**
	 * @see org.eclipse.jdt.core.IClasspathContainer#getDescription()
	 */
	public String getDescription() {
		final String resName = fileSys ? "filesys.decoration.description" : "project.decoration.description";
		return PluginUtils.getResourceString(resName) + " [" + libPath + "]";
//		return PluginUtils.getResourceString("project.decoration.description") + " [/" + libPath + "]";
	}

	/**
	 * @see org.eclipse.jdt.core.IClasspathContainer#getKind()
	 */
	public int getKind() {
		return K_APPLICATION;
	}

	/**
	 * @see org.eclipse.jdt.core.IClasspathContainer#getPath()
	 */
	public IPath getPath() {
		return new Path(CLASSPATH_CONTAINER_ID);
	}
}
