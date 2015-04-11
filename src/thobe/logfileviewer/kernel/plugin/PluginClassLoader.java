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

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import thobe.logfileviewer.plugin.api.IPlugin;
import thobe.logfileviewer.plugin.source.logline.ILogLine;
import thobe.logfileviewer.plugin.source.logstream.ILogStream;

/**
 * @author Thomas Obenaus
 * @source PluginClassLoader.java
 * @date Apr 11, 2015
 */
public class PluginClassLoader extends thobe.tools.plugin.PluginClassLoader
{

	public PluginClassLoader( ClassLoader parent, ZipFile jarFile ) throws ZipException, IOException, URISyntaxException
	{
		super( parent, jarFile );
	}

	@Override
	protected boolean useParentClassLoader( String name )
	{
		String apiPkt = IPlugin.class.getPackage( ).getName( );
		String sourceLLPkt = ILogLine.class.getPackage( ).getName( );
		String sourceLSPkt = ILogStream.class.getPackage( ).getName( );

		final boolean isOneOfTheApiPackages = ( name != null ) && ( ( name.contains( apiPkt ) || name.contains( sourceLSPkt ) || name.contains( sourceLLPkt ) ) );

		return isOneOfTheApiPackages;
	}

}
