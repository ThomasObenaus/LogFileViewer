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

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Logger;

import thobe.logfileViewer.plugins.console.Console;
import thobe.logfileviewer.kernel.memory.IMemoryWatchable;

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

	@SuppressWarnings ( "unchecked")
	public void findAndRegisterPlugins( )
	{
		File pluginDir = new File( "plugins" );

		File[] plugins = pluginDir.listFiles( new FilenameFilter( )
		{

			@Override
			public boolean accept( File dir, String name )
			{
				if ( name == null )
					return false;
				if ( name.trim( ).isEmpty( ) )
					return false;

				if ( !name.matches( ".*\\.jar$" ) )
					return false;
				return true;
			}
		} );

		List<JarFile> jarFiles = new ArrayList<JarFile>( );

		// 1. Now build the ULR array for loading the jars to class-path
		LOG( ).info( "1. Now build the ULR array for loading the jars to class-path." );
		URL[] pluginUrls = new URL[plugins.length];
		for ( int i = 0; i < plugins.length; ++i )
		{
			try
			{
				jarFiles.add( new JarFile( plugins[i] ) );
				pluginUrls[i] = plugins[i].toURI( ).toURL( );
				LOG( ).info( "\t'" + pluginUrls[i] + "' will be added to class-path." );
			}
			catch ( MalformedURLException e )
			{
				LOG( ).severe( "Unable to add '" + pluginUrls[i] + "' to class-path." );
			}
			catch ( IOException e )
			{
				LOG( ).severe( "Unable to create jar-file from '" + plugins[i] + "' (this one won't be available at class-path)." );
			}
		}// for ( int i = 0; i < plugins.length; ++i )

		// 2. Now load the jars to class-path
		LOG( ).info( "2. Now load the jars to class-path." );
		URLClassLoader classLoader = new URLClassLoader( pluginUrls );

		// 3. Now find the plugins. 
		List<Class<? extends Plugin>> pluginClasses = new ArrayList<Class<? extends Plugin>>( );
		LOG( ).info( "3. Now find the plugins." );
		for ( JarFile jarFile : jarFiles )
		{
			Enumeration<JarEntry> entries = jarFile.entries( );
			while ( entries.hasMoreElements( ) )
			{
				JarEntry entry = entries.nextElement( );
				String name = entry.getName( );
				int extIndex = name.lastIndexOf( ".class" );
				if ( extIndex > 0 )
				{
					String plainClassName = name.substring( 0, extIndex );
					plainClassName = plainClassName.replaceAll( "/", "." );

					try
					{
						Class<?> classToLoad = classLoader.loadClass( plainClassName );

						// check if it is a plugin
						final boolean bImplementsIPlugin = Plugin.class.isAssignableFrom( classToLoad );
						final boolean bIsAbstract = Modifier.isAbstract( classToLoad.getModifiers( ) );
						final boolean bIsInterface = classToLoad.isInterface( );
						if ( bImplementsIPlugin && !bIsAbstract && !bIsInterface )
						{
							LOG( ).info( "\tPlugin found: '" + classToLoad.getName( ) + "'" );
							pluginClasses.add( ( Class<? extends Plugin> ) classToLoad );
						}
					}
					catch ( NoClassDefFoundError e )
					{
						LOG( ).warning( "\tUnable to load class " + name + " using '" + plainClassName + "': NoClassDefFoundError '" + e.getLocalizedMessage( ) + "'" );
					}
					catch ( ClassNotFoundException e )
					{
						LOG( ).warning( "\tUnable to load class " + name + " using '" + plainClassName + "': ClassNotFoundException '" + e.getLocalizedMessage( ) + "'" );
					}

				}// if ( extIndex > 0 )
			}// while ( entries.hasMoreElements( ) )
		}// for ( JarFile jarFile : jarFiles )

		// 4. Now register the plugins. 
		LOG( ).info( "4. Now register the plugins (" + pluginClasses.size( ) + ")" );
		for ( Class<? extends Plugin> pluginClass : pluginClasses )
		{
			try
			{
				Plugin plugin = pluginClass.newInstance( );
				this.registerPlugin( plugin );
				LOG( ).info( "\tPlugin '" + plugin.getName( ) + "' sucessfully registered." );
			}
			catch ( InstantiationException | IllegalAccessException e )
			{
				LOG( ).severe( "\tError creating plugin: " + e.getLocalizedMessage( ) );
			}
		}// for ( Class<? extends Plugin> pluginClass : pluginClasses )

		// now finally close the loader 
		LOG( ).info( "5. Finally close the loader " );
		try
		{
			classLoader.close( );
		}
		catch ( IOException e )
		{
			LOG( ).severe( "Error closing classLoder: " + e.getLocalizedMessage( ) );
		}

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
