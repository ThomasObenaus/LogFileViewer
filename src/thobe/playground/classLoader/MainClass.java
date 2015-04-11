/*
 *  Copyright (C) 2014, Thomas Obenaus. All rights reserved.
 *  Licensed under the New BSD License (3-clause lic)
 *  See attached license-file.
 *
 *	Author: 	Thomas Obenaus
 *	EMail:		obenaus.thomas@gmail.com
 *  Project:    LogFileViewer
 */

package thobe.playground.classLoader;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import thobe.logfileviewer.plugin.PluginApiVersion;
import thobe.logfileviewer.plugin.api.IPlugin;
import thobe.logfileviewer.plugin.api.IPluginApiVersion;

/**
 * @author Thomas Obenaus
 * @source MainClass.java
 * @date Apr 9, 2015
 */
public class MainClass
{
	public static void main( String[] args ) throws InstantiationException, IllegalAccessException, ClassNotFoundException, IOException, URISyntaxException
	{
		ZipFile plugin = new ZipFile( "/home/winnietom/work/projects/LogfileViewer/eclipse-ws/Plugins/thobe.logfileviewer.plugins.console.jar" );

		List<File> dirs = new ArrayList<File>( );
		dirs.add( new File( "/home/winnietom/work/projects/LogfileViewer/eclipse-ws/Plugins/bin" ) );
		dirs.add( new File( "/home/winnietom/work/projects/LogfileViewer/eclipse-ws/Plugins/lib_classes" ) );

		ClassLoader appClassLoader = IPlugin.class.getClassLoader( );
		PluginClassLoader specClassLoader = new MyPluginClassLoader( appClassLoader, dirs );
		//				PluginClassLoader specClassLoader = new MyPluginClassLoader( appClassLoader, plugin );

		System.out.println( "AppClassLoader: " + appClassLoader );
		System.out.println( "SpecialClassLoader: " + specClassLoader );

		IPluginApiVersion apiVersion = new PluginApiVersion( );

		IPlugin objFromSpecialClassLoader = ( IPlugin ) specClassLoader.loadClass( "thobe.logfileviewer.plugins.console.Console" ).newInstance( );
		IPluginApiVersion pluginApiVersion = objFromSpecialClassLoader.getPluginApiVersion( );

		System.out.println( "objFromAppClassLoader=" + apiVersion );
		System.out.println( "objFromSpecialClassLoader=" + pluginApiVersion );

		plugin.close( );

		System.exit( 1 );
	}

	private final static class MyPluginClassLoader extends PluginClassLoader
	{

		public MyPluginClassLoader( ClassLoader parent, List<File> dir ) throws ZipException, IOException, URISyntaxException
		{
			super( parent, dir );
		}

		public MyPluginClassLoader( ClassLoader parent, ZipFile jarFile ) throws ZipException, IOException, URISyntaxException
		{
			super( parent, jarFile );
		}

		@Override
		protected boolean useParentClassLoader( String name )
		{
			return name.startsWith( "thobe.logfileviewer.plugin.api" );
		}
	};

}
