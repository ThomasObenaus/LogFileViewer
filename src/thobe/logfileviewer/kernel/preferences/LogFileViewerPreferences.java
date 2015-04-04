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

import java.util.prefs.Preferences;

import thobe.logfileviewer.LogFileViewerInfo;
import thobe.logfileviewer.kernel.plugin.PluginManager;
import thobe.logfileviewer.plugin.api.IPluginPreferences;
import thobe.tools.preferences.PreferenceObject;

/**
 * @author Thomas Obenaus
 * @source Preferences.java
 * @date Oct 31, 2014
 */
public class LogFileViewerPreferences extends PreferenceObject
{
	private static final String	NODE_PLUGIN_ROOT	= "Plugins";

	/**
	 * Preferences considering the source (of the log-stream, e.g. IP or file).
	 */
	private SourcePrefs			sourcePreferences;

	/**
	 * Preferences for the {@link PluginManager}.
	 */
	private PluginManagerPrefs	pluginManagerPreferences;

	public LogFileViewerPreferences( )
	{
		super( LogFileViewerInfo.getAppName( ) );

		this.sourcePreferences = new SourcePrefs( );
		this.pluginManagerPreferences = new PluginManagerPrefs( );
	}

	@Override
	public void load( Preferences applicationRoot )
	{
		this.sourcePreferences.load( applicationRoot );
		this.pluginManagerPreferences.load( applicationRoot );
	}

	@Override
	public void save( Preferences applicationRoot )
	{
		this.sourcePreferences.save( applicationRoot );
		this.pluginManagerPreferences.save( applicationRoot );
	}

	public SourcePrefs getSourcePreferences( )
	{
		return sourcePreferences;
	}

	public PluginManagerPrefs getPluginManagerPreferences( )
	{
		return pluginManagerPreferences;
	}

	public void loadPluginPreferences( IPluginPreferences pluginPrefs, String pluginName )
	{
		if ( pluginPrefs != null )
		{
			Preferences root = Preferences.userRoot( );
			Preferences appRoot = root.node( this.getApplicationName( ) );
			Preferences pluginsRoot = appRoot.node( NODE_PLUGIN_ROOT );
			Preferences pluginRoot = pluginsRoot.node( pluginName.replaceAll( "\\.", "-") );
			pluginPrefs.load( pluginRoot );
		}// if ( pluginPrefs != null )
	}

	public void savePluginPreferences( IPluginPreferences pluginPrefs, String pluginName )
	{
		if ( pluginPrefs != null )
		{
			Preferences root = Preferences.userRoot( );
			Preferences appRoot = root.node( this.getApplicationName( ) );
			Preferences pluginsRoot = appRoot.node( NODE_PLUGIN_ROOT );
			Preferences pluginRoot = pluginsRoot.node( pluginName.replaceAll( "\\.", "-") );
			pluginPrefs.save( pluginRoot );
		}// if ( pluginPrefs != null )
	}

}
