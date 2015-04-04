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

/**
 * @author Thomas Obenaus
 * @source LogFileViewerConfiguration.java
 * @date Apr 4, 2015
 */
public class LogFileViewerConfiguration
{
	private static final String	L_NAME			= "thobe.logfileviewer.kernel.LogFileViewerConfiguration";

	private static final String	KEY_PLUGIN_DIR	= "plugin-directory";
	private static final String	DEF_PLUGIN_DIR	= "plugins";

	private Logger				log;
	private File				pluginDirectory;

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
		this.pluginDirectory = new File( props.getProperty( KEY_PLUGIN_DIR, DEF_PLUGIN_DIR ) );
	}

	private void setDefaultProps( )
	{
		this.pluginDirectory = getDefaultPluginDir( );
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
		return new File( DEF_PLUGIN_DIR );
	}
}
