/*
 *  Copyright (C) 2014, Thomas Obenaus. All rights reserved.
 *  Licensed under the New BSD License (3-clause lic)
 *  See attached license-file.
 *
 *	Author: 	Thomas Obenaus
 *	EMail:		obenaus.thomas@gmail.com
 *  Project:    LogFileViewer
 */

package thobe.logfileviewer.kernel;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Logger;

import thobe.logfileviewer.kernel.plugin.PluginManager;

/**
 * @author Thomas Obenaus
 * @source LogFileViewerConfiguration.java
 * @date Apr 4, 2015
 */
public class LogFileViewerConfiguration
{
	private static final String		L_NAME					= "thobe.logfileviewer.kernel.LogFileViewerConfiguration";
	private static final String		DEF_CONFIG_FILE_NAME	= "logfileviewer.conf";

	/**
	 * Settings for the {@link PluginManager}
	 */
	private static final String		PREF_PLUGIN_MANAGER		= "PluginManager.";
	private static final String		KEY_PM_PLUGIN_DIR		= PREF_PLUGIN_MANAGER + "pluginDirectory";
	private static final String		DEF_PM_PLUGIN_DIR		= "plugins";

	/**
	 * Settings for the Stats
	 */
	private static final String		PREF_STATS_PRINTER		= "StatsPrinter.";
	private static final String		KEY_SP_ENABLED			= PREF_STATS_PRINTER + "enabled";
	private static final boolean	DEF_SP_ENABLED			= false;
	private static final String		KEY_SP_UPD_INTERVAL		= PREF_STATS_PRINTER + "updateInterval";
	private static final int		DEF_SP_UPD_INTERVAL		= 10000;

	private Logger					log;
	private File					pluginDirectory;
	private boolean					statsPrinterEnabled;
	private int						statsPrinterUpdateInterval;

	public LogFileViewerConfiguration( File configFile )
	{
		this.log = Logger.getLogger( L_NAME );

		try
		{
			Properties p = new Properties( );
			p.load( new FileInputStream( configFile ) );
			this.readProperties( p );
		}
		catch ( IOException e )
		{
			LOG( ).severe( "Unable to load configuration for LogFileViewer: " + e.getLocalizedMessage( ) + " Using default values." );
			this.setDefaultProps( );
		}
	}

	private void readProperties( Properties props )
	{
		this.pluginDirectory = new File( props.getProperty( KEY_PM_PLUGIN_DIR, DEF_PM_PLUGIN_DIR ) );
		this.statsPrinterEnabled = getProp( props, KEY_SP_ENABLED, DEF_SP_ENABLED );
		this.statsPrinterUpdateInterval = getProp( props, KEY_SP_UPD_INTERVAL, DEF_SP_UPD_INTERVAL );
	}

	private void setDefaultProps( )
	{
		this.pluginDirectory = getDefaultPluginDir( );
		this.statsPrinterEnabled = DEF_SP_ENABLED;
		this.statsPrinterUpdateInterval = DEF_SP_UPD_INTERVAL;
	}

	public boolean isStatsPrinterEnabled( )
	{
		return this.statsPrinterEnabled;
	}

	public int getStatsPrinterUpdateInterval( )
	{
		return statsPrinterUpdateInterval;
	}

	protected Logger LOG( )
	{
		return this.log;
	}

	public File getPluginDirectory( )
	{
		return pluginDirectory;
	}

	public static File getDefaultPluginDir( )
	{
		return new File( DEF_PM_PLUGIN_DIR );
	}

	public static String getDefaultConfigFileName( )
	{
		return DEF_CONFIG_FILE_NAME;
	}

	private static int getProp( Properties props, String key, int defaultValue )
	{
		int result = defaultValue;

		String valueStr = props.getProperty( key, defaultValue + "" );
		if ( valueStr != null )
		{
			try
			{
				result = Integer.parseInt( valueStr );
			}
			catch ( NumberFormatException e )
			{}
		}

		return result;
	}

	private static boolean getProp( Properties props, String key, boolean defaultValue )
	{
		boolean result = defaultValue;

		String valueStr = props.getProperty( key, defaultValue + "" );
		if ( valueStr != null )
		{
			try
			{
				result = Boolean.parseBoolean( valueStr );
			}
			catch ( Exception e )
			{}
		}

		return result;
	}
}
