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
import thobe.tools.preferences.PreferenceObject;

/**
 * @author Thomas Obenaus
 * @source Preferences.java
 * @date Oct 31, 2014
 */
public class LogFileViewerPreferences extends PreferenceObject
{
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

}
