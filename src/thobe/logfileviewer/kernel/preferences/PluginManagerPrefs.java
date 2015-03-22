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

import java.io.File;
import java.util.prefs.Preferences;

/**
 * @author Thomas Obenaus
 * @source PluginManagerPrefs.java
 * @date Mar 22, 2015
 */
public class PluginManagerPrefs implements ISubPrefs
{
	private static final String	DEF_PLUGIN_DIR	= "plugins";

	private static final String	NODE			= "PluginManager";
	private static final String	PRP_PLUGIN_DIR	= "plugin-dir";

	private File				pluginDir;

	public PluginManagerPrefs( )
	{
		this.pluginDir = null;
	}

	@Override
	public void load( Preferences applicationRoot )
	{
		Preferences root = applicationRoot.node( NODE );

		String strPluginDir = root.get( PRP_PLUGIN_DIR, DEF_PLUGIN_DIR );
		this.pluginDir = new File( strPluginDir );

	}

	@Override
	public void save( Preferences applicationRoot )
	{
		Preferences root = applicationRoot.node( NODE );
		if ( ( this.pluginDir != null ) && ( this.pluginDir.isDirectory( ) ) && this.pluginDir.exists( ) )
		{
			root.put( PRP_PLUGIN_DIR, this.pluginDir.getAbsolutePath( ) );
		}
	}

	public File getPluginDir( )
	{
		return pluginDir;
	}

}
