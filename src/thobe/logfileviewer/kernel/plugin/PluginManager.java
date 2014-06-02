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

import java.util.HashMap;
import java.util.Map;

import thobe.logfileviewer.kernel.plugin.console.Console;

/**
 * @author Thomas Obenaus
 * @source PluginManager.java
 * @date May 31, 2014
 */
public class PluginManager implements IPluginAccess
{
	private Map<String, Plugin>	plugins;

	public PluginManager( )
	{
		this.plugins = new HashMap<>( );
	}

	public void findAndRegisterPlugins( )
	{
		// TODO: implement
	}

	public void registerPlugin( Plugin plugin )
	{
		this.plugins.put( plugin.getPluginName( ), plugin );
	}

	public void unregisterPlugin( Plugin plugin )
	{
		this.plugins.remove( plugin );
	}

	public void unregisterAllPlugins( )
	{
		this.plugins.clear( );
	}

	public Map<String, Plugin> getPlugins( )
	{
		return plugins;
	}

	@Override
	public Plugin getPlugin( String pluginName )
	{
		return this.plugins.get( pluginName );
	}

	@Override
	public boolean hasPlugin( String pluginName )
	{
		return this.plugins.containsKey( pluginName );
	}

	@Override
	public Console getConsole( )
	{
		Plugin plugin = this.plugins.get( Console.FULL_PLUGIN_NAME );
		Console console = null;
		if ( plugin instanceof Console )
		{
			console = ( Console ) plugin;
		}
		return console;
	}
}
