/*
 *  Copyright (C) 2014, Thomas Obenaus. All rights reserved.
 *  Licensed under the New BSD License (3-clause lic)
 *  See attached license-file.
 *
 *	Author: 	Thomas Obenaus
 *	EMail:		obenaus.thomas@gmail.com
 *  Project:    LogFileViewer
 */

package thobe.logfileviewer.kernel.plugin;

import java.util.Map;

/**
 * @author Thomas Obenaus
 * @source IPluginAccess.java
 * @date Jun 1, 2014
 */
public interface IPluginAccess
{
	/**
	 * Returns true if the {@link IPlugin} with the given name is available, false otherwise.
	 * @param pluginName
	 * @return
	 */
	public boolean hasPlugin( String pluginName );

	/**
	 * Returns the {@link IPlugin} with the given name or null if no such {@link IPlugin} is available.
	 * @param pluginName
	 * @return
	 */
	public IPlugin getPlugin( String pluginName );

	/**
	 * Returns all {@link IPlugin} currently registered (Map<plugin-name,IPlugin>).
	 * @return
	 */
	public Map<String, IPlugin> getPlugins( );
}
