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
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Logger;

import thobe.logfileviewer.kernel.memory.IMemoryWatchable;
import thobe.logfileviewer.kernel.plugin.console.Console;

/**
 * @author Thomas Obenaus
 * @source PluginManager.java
 * @date May 31, 2014
 */
public class PluginManager implements IPluginAccess, IMemoryWatchable
{
	private static final String	NAME	= "thobe.logfileviewer.kernel.PluginManager";

	private Map<String, Plugin>	plugins;
	private Logger				log;

	public PluginManager( )
	{
		this.log = Logger.getLogger( NAME );
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

	public void freeMemory( )
	{
		synchronized ( this.plugins )
		{
			for ( Entry<String, Plugin> entry : this.plugins.entrySet( ) )
			{
				Plugin plugin = entry.getValue( );
				long memBeforeFree = plugin.getMemory( );
				entry.getValue( ).freeMemory( );

				long memAfterFree = plugin.getMemory( );
				if ( ( memBeforeFree != 0 ) && ( memBeforeFree <= memAfterFree ) )
				{
					LOG( ).warning( "Plugin '" + plugin.getName( ) + "' failed to free memory (remaining: " + ( memAfterFree / 1024f / 1024f ) + "MB)" );
				}// if ( ( memBeforeFree != 0 ) && ( memBeforeFree <= memAfterFree ) ).
			}// for ( Entry<String, Plugin> entry : this.plugins.entrySet( ) ).
		}// synchronized ( this.plugins ) .
	}

	protected Logger LOG( )
	{
		return this.log;
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

	@Override
	public Set<IPluginUI> getPluginsNotAttachedToGui( )
	{
		Set<IPluginUI> result = new HashSet<>( );

		for ( Map.Entry<String, Plugin> entry : this.plugins.entrySet( ) )
		{
			if ( !entry.getValue( ).isAttachedToGUI( ) )
			{
				result.add( entry.getValue( ) );
			}// if ( !entry.getValue( ).isAttachedToGUI( ) )
		}// for ( Map.Entry<String, Plugin> entry : this.plugins.entrySet( ) )

		return result;
	}

	@Override
	public long getMemory( )
	{
		long completeMemory = 0;
		for ( Entry<String, Plugin> entry : this.getPlugins( ).entrySet( ) )
		{
			Plugin plugin = entry.getValue( );
			completeMemory += plugin.getMemory( );
		}// for ( Entry<String, Plugin> entry : this.getPlugins( ).entrySet( ) ) .
		return completeMemory;
	}

	@Override
	public String getNameOfMemoryWatchable( )
	{
		return NAME;
	}
}
