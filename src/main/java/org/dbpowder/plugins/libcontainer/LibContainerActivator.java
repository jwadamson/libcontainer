package org.dbpowder.plugins.libcontainer;

import java.util.MissingResourceException;
import java.util.ResourceBundle;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.*;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * @author Joris Verschoor <j.verschoor@insomniq.com>
 * @author Tadashi Murakami <tadashi@dbpowder.org>
 *
 * The main plugin class to be used in the desktop.
 * REMOVE THIS CLASS, AND ADD/UPDATE TO JDT
 */
public class LibContainerActivator extends AbstractUIPlugin {

	/** The shared instance. */
	private static LibContainerActivator plugin;

	/** Resource bundle. */
	private ResourceBundle resourceBundle;

	// public LibContainer(IPluginDescriptor iplugindescriptor) {
	// super(iplugindescriptor);
	// initialize();
	// }

	/**
	 * The constructor.
	 */
	public LibContainerActivator() {
		super();
		plugin = this;
		try {
			resourceBundle = ResourceBundle.getBundle("org.dbpowder.plugins.libcontainer.Resources"); //$NON-NLS-1$
		} catch (MissingResourceException e) {
			resourceBundle = null;
		}

		String s = "Activation Succeed (1.1.0.20071021_1).";
		Status status = new Status(Status.ERROR, "LibClasspathPlugin", Status.OK, s, null);
		getDefault().getLog().log(status);
	}

	/**
	 * Returns the shared instance.
	 */
	public static LibContainerActivator getDefault() {
		return plugin;
	}

	/**
	 * Returns the workspace instance.
	 */
	public static IWorkspace getWorkspace() {
		return ResourcesPlugin.getWorkspace();
	}

	/**
	 * Returns the plugin's resource bundle,
	 */
	public ResourceBundle getResourceBundle() {
		return resourceBundle;
	}
}
