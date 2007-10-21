package org.dbpowder.plugins.libcontainer;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.*;
import org.eclipse.jdt.core.*;

/**
 * @author Joris Verschoor <j.verschoor@insomniq.com>
 * @author Stuart McGrigor <Stuart@Retail.geek.nz>
 * @author Tadashi Murakami <tadashi@dbpowder.org>
 *
 * The wizard for configuring a new LibContainer. 
 * Now recognizes embedded "%3b"  as ':'  allowing windows absolute paths.  
 */
public class LibContainerInitializer extends ClasspathContainerInitializer {
	public static final String FILESYS = "fileSys";
	public static final String PROJECT = "project";

	public static final String RECURSE = "recurse"; //$NON-NLS-1$
	public static final String FLAT = "flat"; //$NON-NLS-1$
	public static final String DEFAULT_FOLDER = "lib"; //$NON-NLS-1$

	public LibContainerInitializer() {
	}

	/**
	 * Constructs a .classpath IPath: the.id.of.the.plugin/recurse/path/of/the/folder
	 * See the .classpath file kind="con"
	 * 
	 * @param libPath The path to the library folder
	 * @param isRecurse Specifies wether or not subfolders should be scanned
	 * @param fileSys Specifies the libPath is in File System rather than workspace project
	 * @return 
	 */	
	public static IPath buildContainerPath(IJavaProject project, String libPath, boolean isRecurse, boolean fileSys) {
		String cPathStr = LibClasspathContainer.CLASSPATH_CONTAINER_ID + "/"
				+ (isRecurse ? RECURSE : FLAT) + "~" + (fileSys ? FILESYS : PROJECT) + "/" + PluginUtils.normalizePath(libPath);
		return new Path(cPathStr);
	}

	/**
	 * @see org.eclipse.jdt.core.ClasspathContainerInitializer#initialize(org.eclipse.core.runtime.IPath, org.eclipse.jdt.core.IJavaProject)
	 */
	public void initialize(IPath containerPath, IJavaProject project)
			throws CoreException {
		// The containerPath should look like: org..ID../recursetype/projectname/the/path/to/the/folder
		final int segmentCount = containerPath.segmentCount();

		if (segmentCount <= 0
				|| !containerPath.segment(0).equals(LibClasspathContainer.CLASSPATH_CONTAINER_ID)) {
			project.getProject().refreshLocal(1, null);
			return;
		}

		final boolean isRecurse;
		final boolean isFileSys;
		final String libPath;
		final String javaVer;
		if (segmentCount <= 1) {
			isRecurse = false;
			isFileSys = false;
			libPath = DEFAULT_FOLDER;
			javaVer = null;
		} else {
			String[] recurseStrArr = containerPath.segment(1).split("~");
//			String[] recurseStrArr = containerPath.segment(1).split("\\|");
//			PluginUtils.log("LogSort: " + recurseStrArr[0], 0);
			isRecurse = RECURSE.equals(recurseStrArr[0]);
			if (!isRecurse) {
				if (!FLAT.equals(recurseStrArr[0])) {
					project.getProject().refreshLocal(1, null);
					return;
				}
			}
			isFileSys = recurseStrArr.length <= 1 ? false : FILESYS.equals(recurseStrArr[1]);
			javaVer = recurseStrArr.length <= 2 ? null : recurseStrArr[2];
			if (segmentCount <= 2) {
				libPath = DEFAULT_FOLDER;
			} else {
				StringBuffer buf = new StringBuffer();
				String[] segArr = containerPath.segments();

				buf.append(PluginUtils.deNormalizePath(segArr[2]));
				for (int i = 3; i < segArr.length; i++) {
					buf.append('/').append(PluginUtils.deNormalizePath(segArr[i]));
				}
				// if 2nd char is ':', platform is windows
				if (isFileSys && buf.indexOf(":")!=1){
					libPath = "/"+buf.toString();
				} else {
					libPath = buf.toString();
				}
			}
		}
		PluginUtils.log("PathInitialize: r" + isRecurse+" f"+isFileSys+" "+libPath+" v"+javaVer, 0);

		LibClasspathContainer con = new LibClasspathContainer(project, libPath, isRecurse, isFileSys, javaVer);
		@SuppressWarnings("unused")
		IPath path = new Path(LibClasspathContainer.CLASSPATH_CONTAINER_ID);

		JavaCore.setClasspathContainer(containerPath,
				new IJavaProject[] { project },
				new IClasspathContainer[] { con }, null);

		project.getProject().refreshLocal(IResource.DEPTH_ONE, null);
	}

	/**
	 * Returns an object which identifies a container for comparison purpose. This allows
	 * to eliminate redundant containers when accumulating classpath entries (e.g.
	 * runtime classpath computation). When requesting a container comparison ID, one
	 * should ensure using its corresponding container initializer. Indeed, a random container
	 * initializer cannot be held responsible for determining comparison IDs for arbitrary
	 * containers.
	 * <p>
	 * @param containerPath the path of the container which is being checked
	 * @param project the project for which the container is to being checked
	 * @return returns an Object identifying the container for comparison
	 * @since 3.0
	 */
	public Object getComparisonID(IPath containerPath, IJavaProject project) {

		// By default, containers are identical if they have the same containerPath first segment,
		// but this may be refined by other container initializer implementations.
		if (containerPath == null) {
			return null;
		} else {
			System.out.println(containerPath.toString());
			return containerPath.toString();
//			return containerPath.segment(0);
		}
	}

}
