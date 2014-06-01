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

/**
 * @author Thomas Obenaus
 * @source PluginManager.java
 * @date May 31, 2014
 */
public class PluginManager implements IPluginAccess
{
	private Map<String, IPlugin>	plugins;

	public PluginManager( )
	{
		this.plugins = new HashMap<>( );
	}

	public void findAndRegisterPlugins( )
	{
		// TODO: implement
	}

	public void registerPlugin( IPlugin plugin )
	{
		this.plugins.put( plugin.getPluginName( ), plugin );
	}

	public void unregisterPlugin( IPlugin plugin )
	{
		this.plugins.remove( plugin );
	}

	public void unregisterAllPlugins( )
	{
		this.plugins.clear( );
	}

	public Map<String, IPlugin> getPlugins( )
	{
		return plugins;
	}

	@Override
	public IPlugin getPlugin( String pluginName )
	{
		return this.plugins.get( pluginName );
	}

	@Override
	public boolean hasPlugin( String pluginName )
	{
		return this.plugins.containsKey( pluginName );
	}
}
