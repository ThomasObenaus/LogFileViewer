/*
 *  Copyright (C) 2014, Thomas Obenaus. All rights reserved.
 *  Licensed under the New BSD License (3-clause lic)
 *  See attached license-file.
 *
 *	Author: 	Thomas Obenaus
 *	EMail:		obenaus.thomas@gmail.com
 *  Project:    LogFileViewer
 */

package thobe.logfileviewer.kernel.preferences;

import java.util.HashMap;
import java.util.Map;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 * @author Thomas Obenaus
 * @source PluginManagerPrefs.java
 * @date Mar 22, 2015
 */
public class PluginManagerPrefs implements ISubPrefs
{
	private static final String		NODE					= "PluginManager";
	private static final String		NODE_PLUGINS_ENABLED	= "PluginsEnabled";

	private Map<String, Boolean>	pluginEnabledMap;

	public PluginManagerPrefs( )
	{
		this.pluginEnabledMap = new HashMap<String, Boolean>( );
	}

	@Override
	public void load( Preferences applicationRoot )
	{
		Preferences root = applicationRoot.node( NODE );

		// load plugins enabled 
		try
		{
			Preferences pluginsEnabled = root.node( NODE_PLUGINS_ENABLED );
			String[] keys = pluginsEnabled.keys( );
			for ( String key : keys )
			{
				boolean enabled = pluginsEnabled.getBoolean( key, true );
				this.pluginEnabledMap.put( key, enabled );
			}
		}
		catch ( BackingStoreException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace( );
		}

	}

	@Override
	public void save( Preferences applicationRoot )
	{
		Preferences root = applicationRoot.node( NODE );

		Preferences pluginsEnabled = root.node( NODE_PLUGINS_ENABLED );
		for ( Map.Entry<String, Boolean> entry : this.pluginEnabledMap.entrySet( ) )
		{
			String pluginName = entry.getKey( );
			pluginsEnabled.putBoolean( pluginName, entry.getValue( ) );
		}
	}

	public boolean isPluginEnabled( String pluginName )
	{
		Boolean result = this.pluginEnabledMap.get( pluginName );

		// by default an unknown plugin will be enabled
		if ( result == null )
			return true;
		return result;
	}

	public void setPluginEnabled( String pluginName, boolean enabled )
	{
		this.pluginEnabledMap.put( pluginName, enabled );
	}
}
