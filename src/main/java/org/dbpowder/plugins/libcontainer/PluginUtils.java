package org.dbpowder.plugins.libcontainer;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.JavaModelException;

/**
 * @author Joris Verschoor <j.verschoor@insomniq.com>
 * @author Stuart McGrigor <Stuart@Retail.geek.nz>
 * @author Tadashi Murakami <tadashi@dbpowder.org>
 * 
 */
public class PluginUtils {

	public static boolean isLibraryFile(String ext) {
		if (ext == null) {
			return false;
		}
		ext = ext.toLowerCase();
		return "jar".equals(ext) || "zip".equals(ext);  //$NON-NLS-1$
	}

	/**
	 * Returns the string from the plugin's resource bundle,
	 * or 'key' if not found.
	 */
	public static String getResourceString(String key) {
		ResourceBundle resourcebundle = LibContainerActivator.getDefault().getResourceBundle();
		if (resourcebundle == null) {
			return key;
		}

		try {
			return resourcebundle.getString(key);
		} catch (MissingResourceException e) {
			return key;
		}
	}


	/**
	   The main barrier to external file system folders acting as Libraries
	   is the damned emedded ':' character on Windows systems. These two
	   methods orchestrate the switch between ':' and "%3b" as required. 	
	 **/
	
	// deNormalize %3b into ':'  so we can have absolute paths...
	public static String deNormalizePath(String s) {
		String path = s;
		int pos = path.indexOf("%3b");
		while(pos != -1) {
			path = path.substring(0, pos) + ':' + path.substring(pos+3);
			pos = path.indexOf("%3b");
		}
		return path;	
	}
	
	// Normalize ':' into %3b  so we can have absolute paths...
	@SuppressWarnings("unused")
	public static String normalizePath(String s) {
		String path = s;
		int pos = path.indexOf(':');
		while(pos != -1) {
			path = path.substring(0, pos) + "%3b" + path.substring(pos+1);
			pos = path.indexOf(':');
		}
		return path;
	}


	//
	// log functions 
	//
	public static void log(Throwable t) {
		log(t, (String) null);
	}

	public static void log(Throwable t, String msg) {
		if (t instanceof JavaModelException) {
			Throwable t1 = ((JavaModelException) t).getException();
			if (t1 != null) {
				t = t1;
			}
		}
		Status status = new Status(Status.ERROR, LibContainerActivator
				.getDefault().getBundle().getSymbolicName(), Status.ERROR, msg,
				t);
		LibContainerActivator.getDefault().getLog().log(status);
	}

	public static void log(String msg, int severity) {
		Status status = new Status(severity, LibContainerActivator.getDefault()
				.getBundle().getSymbolicName(), Status.OK, msg, null);
		LibContainerActivator.getDefault().getLog().log(status);
	}
}
