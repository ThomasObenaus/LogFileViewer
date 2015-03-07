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
import java.util.Set;

import thobe.logfileViewer.plugins.console.Console;

/**
 * @author Thomas Obenaus
 * @source IPluginAccess.java
 * @date Jun 1, 2014
 */
public interface IPluginAccess
{
	/**
	 * Returns true if the {@link Plugin} with the given name is available, false otherwise.
	 * @param pluginName
	 * @return
	 */
	public boolean hasPlugin( String pluginName );

	/**
	 * Returns the {@link Plugin} with the given name or null if no such {@link Plugin} is available.
	 * @param pluginName
	 * @return
	 */
	public Plugin getPlugin( String pluginName );

	/**
	 * Returns all {@link Plugin} currently registered (Map<plugin-name,IPlugin>).
	 * @return
	 */
	public Map<String, Plugin> getPlugins( );

	/**
	 * Returns a set containing all {@link IPluginUI}s that are not already attached to the gui.
	 * @return
	 */
	public Set<IPluginUI> getPluginsNotAttachedToGui( );
	
	/**
	 * Returns the {@link Console}-plugin.
	 * Might return null if the {@link Console}-plugin is not yet available.
	 * @return
	 */
	public Console getConsole( );
}
