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
import java.net.URISyntaxException;
import java.net.URL;
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

import thobe.logfileviewer.kernel.LogFileViewerConfiguration;
import thobe.logfileviewer.kernel.memory.IMemoryWatchable;
import thobe.logfileviewer.kernel.preferences.PluginManagerPrefs;
import thobe.logfileviewer.plugin.Plugin;
import thobe.logfileviewer.plugin.PluginApiVersion;
import thobe.logfileviewer.plugin.api.IConsole;
import thobe.logfileviewer.plugin.api.IPlugin;
import thobe.logfileviewer.plugin.api.IPluginAccess;
import thobe.logfileviewer.plugin.api.IPluginApiVersion;
import thobe.logfileviewer.plugin.api.IPluginUI;

/**
 * @author Thomas Obenaus
 * @source PluginManager.java
 * @date May 31, 2014
 */
public class PluginManager implements IPluginAccess, IMemoryWatchable
{
	private static final String		NAME	= "thobe.logfileviewer.kernel.PluginManager";

	private Map<String, IPlugin>	plugins;
	private Map<String, IPlugin>	incompatiblePlugins;
	private Logger					log;
	private PluginManagerPrefs		prefs;
	private File					pluginDirectory;

	public PluginManager( PluginManagerPrefs prefs, File pluginDirectory )
	{
		this.prefs = prefs;
		this.pluginDirectory = pluginDirectory;
		this.log = Logger.getLogger( NAME );
		this.plugins = new HashMap<>( );
		this.incompatiblePlugins = new HashMap<>( );
	}

	public String getPluginDirectory( )
	{
		return ( pluginDirectory != null ? this.pluginDirectory.getAbsolutePath( ) : "N/A" );
	}

	public PluginApiVersion getPluginApiVersion( )
	{
		return new PluginApiVersion( );
	}

	private void checkPluginDirectory( ) throws PluginManagerException
	{
		final File def = LogFileViewerConfiguration.getDefaultPluginDir( );
		if ( this.pluginDirectory == null )
		{
			LOG( ).warning( "No plugin-directory set, trying the default one ('" + def.getAbsolutePath( ) + "')." );
			this.pluginDirectory = def;
		}
		else if ( !this.pluginDirectory.exists( ) )
		{
			LOG( ).warning( "Plugin-directory '" + this.pluginDirectory.getAbsolutePath( ) + "' does not exist, trying the default one ('" + def.getAbsolutePath( ) + "')." );
			this.pluginDirectory = def;
		}
		else if ( !this.pluginDirectory.canRead( ) )
		{
			LOG( ).warning( "Plugin-directory '" + this.pluginDirectory.getAbsolutePath( ) + "' is not readable, trying the default one ('" + def.getAbsolutePath( ) + "')." );
			this.pluginDirectory = def;
		}

		if ( ( this.pluginDirectory == null ) || ( !this.pluginDirectory.canRead( ) ) || ( !this.pluginDirectory.exists( ) ) )
		{
			String msg = "Unable to use given plugin-directory (" + ( ( this.pluginDirectory != null ) ? "'" + this.pluginDirectory.getAbsolutePath( ) + "'" : "Not set, its null" ) + ") since it is not readable or does not exsit.";
			LOG( ).severe( msg );
			throw new PluginManagerException( msg );
		}// if ( ( this.pluginDirectory == null ) || ( !this.pluginDirectory.canRead( ) ) || ( !this.pluginDirectory.exists( ) ) )

	}

	@SuppressWarnings ( "unchecked")
	public void findAndRegisterPlugins( ) throws PluginManagerException
	{
		// determine/ find plugin-directory, throws an exception if it can't be found
		this.checkPluginDirectory( );

		LOG( ).info( "0. Looking for plugins in '" + this.pluginDirectory.getAbsolutePath( ) + "'" );
		File[] plugins = this.pluginDirectory.listFiles( new FilenameFilter( )
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
		LOG( ).info( "1. Now build the ULR array for loading the jars to class-path (" + plugins.length + ")" );
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

		// 2. Now find the plugins. 
		ClassLoader appClassLoader = PluginManager.class.getClassLoader( );
		Set<Class<? extends IPlugin>> pluginClasses = new HashSet<Class<? extends IPlugin>>( );
		LOG( ).info( "2. Now find the plugins." );
		for ( JarFile jarFile : jarFiles )
		{
			PluginClassLoader pluginClassLoader = null;
			try
			{
				pluginClassLoader = new PluginClassLoader( appClassLoader, jarFile );
			}
			catch ( IOException | URISyntaxException e1 )
			{
				LOG( ).severe( "Unable to load classes from jar-file '" + jarFile.getName( ) + "' (the corresponding plugin won't be loaded)." );
				continue;
			}

			Enumeration<JarEntry> entries = jarFile.entries( );
			Class<? extends IPlugin> pluginClass = null;

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
						Class<?> classToLoad = pluginClassLoader.loadClass( plainClassName );

						// check if it is a plugin
						final boolean bImplementsIPlugin = IPlugin.class.isAssignableFrom( classToLoad );
						final boolean bIsAbstract = Modifier.isAbstract( classToLoad.getModifiers( ) );
						final boolean bIsInterface = classToLoad.isInterface( );
						if ( bImplementsIPlugin && !bIsAbstract && !bIsInterface )
						{
							LOG( ).info( "\tPlugin found: '" + classToLoad.getName( ) + "'" );
							pluginClass = ( Class<? extends Plugin> ) classToLoad;
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

			// extract the version file and write it into a temp-folder
			if ( pluginClass != null )
			{
				pluginClasses.add( pluginClass );
				LOG( ).info( "\tPlugin seems to be valid '" + pluginClass.getName( ) + "'." );
			}// if ( pluginClass != null && versionFileStr != null )
			else
			{
				LOG( ).warning( "\tPlugin '" + pluginClass + "' ignored." );
			}

		}// for ( JarFile jarFile : jarFiles )

		IPluginApiVersion apiVersionOfLogFileViewer = new PluginApiVersion( );
		// 3. Now register the plugins. 
		LOG( ).info( "3. Now register the plugins (" + pluginClasses.size( ) + "), api of plugin-api of LogFileViewer=" + apiVersionOfLogFileViewer );
		for ( Class<? extends IPlugin> pluginClass : pluginClasses )
		{
			try
			{
				IPlugin plugin = pluginClass.newInstance( );
				IPluginApiVersion apiVersionOfPlugin = plugin.getPluginApiVersion( );

				if ( !apiVersionOfLogFileViewer.isCompatible( apiVersionOfPlugin ) )
				{
					this.incompatiblePlugins.put( plugin.getPluginName( ), plugin );
					LOG( ).warning( "\tPlugin '" + pluginClass.getSimpleName( ) + "' will be ignored. API-missmatch:  ApiOfLogFileViewer='" + apiVersionOfLogFileViewer + "' apiOfPlugin='" + apiVersionOfPlugin + "'" );
				}// if ( !apiVersionOfLogFileViewer.isCompatible( pluginApiOfPlugin ) )
				else
				{
					boolean pluginEnabled = prefs.isPluginEnabled( plugin.getPluginName( ) );
					plugin.setEnabled( pluginEnabled );

					this.registerPlugin( plugin );
					LOG( ).info( "\tPlugin '" + plugin.getPluginName( ) + "' sucessfully registered [plugin api: " + apiVersionOfPlugin + ", plugin-api of LogFileViewer: " + apiVersionOfLogFileViewer + "], the plugin is " + ( plugin.isEnabled( ) ? "enabled" : "disabled" ) );

				}// if ( !apiVersionOfLogFileViewer.isCompatible( pluginApiOfPlugin ) ) ... else ...
			}
			catch ( InstantiationException | IllegalAccessException e )
			{
				LOG( ).severe( "\tError creating plugin: " + e.getLocalizedMessage( ) );
			}
			catch ( NoClassDefFoundError e )
			{
				LOG( ).severe( "\tError creating plugin '" + pluginClass + "' (NoClassDefFoundError): " + e.getLocalizedMessage( ) );
			}
		}// for ( Class<? extends Plugin> pluginClass : pluginClasses )
	}

	public void registerPlugin( IPlugin plugin )
	{
		this.plugins.put( plugin.getPluginName( ), plugin );
	}

	public void unregisterPlugin( IPlugin plugin )
	{
		this.plugins.remove( plugin );
	}

	public void unregisterAllPlugins( )
	{
		this.plugins.clear( );
	}

	public Map<String, IPlugin> getPlugins( )
	{
		return plugins;
	}

	public Map<String, IPlugin> getIncompatiblePlugins( )
	{
		return incompatiblePlugins;
	}

	@Override
	public IPlugin getPlugin( String pluginName )
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
			for ( Entry<String, IPlugin> entry : this.plugins.entrySet( ) )
			{
				IPlugin plugin = entry.getValue( );
				long memBeforeFree = plugin.getMemory( );
				entry.getValue( ).freeMemory( );

				long memAfterFree = plugin.getMemory( );
				if ( ( memBeforeFree != 0 ) && ( memBeforeFree <= memAfterFree ) )
				{
					LOG( ).warning( "Plugin '" + plugin.getPluginName( ) + "' failed to free memory (remaining: " + ( memAfterFree / 1024f / 1024f ) + "MB)" );
				}// if ( ( memBeforeFree != 0 ) && ( memBeforeFree <= memAfterFree ) ).
			}// for ( Entry<String, Plugin> entry : this.plugins.entrySet( ) ).
		}// synchronized ( this.plugins ) .
	}

	protected Logger LOG( )
	{
		return this.log;
	}

	@Override
	public IConsole getConsole( )
	{
		IConsole console = null;

		for ( Map.Entry<String, IPlugin> entry : this.plugins.entrySet( ) )
		{
			if ( entry.getValue( ) instanceof IConsole )
			{
				console = ( IConsole ) entry.getValue( );
			}
		}
		return console;
	}

	@Override
	public Set<IPluginUI> getPluginsNotAttachedToGui( )
	{
		Set<IPluginUI> result = new HashSet<>( );

		for ( Map.Entry<String, IPlugin> entry : this.plugins.entrySet( ) )
		{
			IPluginUI uiEntry = ( IPluginUI ) entry.getValue( );
			if ( !uiEntry.isAttachedToGUI( ) && entry.getValue( ).isEnabled( ) )
			{
				result.add( uiEntry );
			}// if ( !entry.getValue( ).isAttachedToGUI( ) && entry.getValue( ).isEnabled( ) )
		}// for ( Map.Entry<String, Plugin> entry : this.plugins.entrySet( ) )

		return result;
	}

	@Override
	public long getMemory( )
	{
		long completeMemory = 0;
		for ( Entry<String, IPlugin> entry : this.getPlugins( ).entrySet( ) )
		{
			IPlugin plugin = entry.getValue( );
			completeMemory += plugin.getMemory( );
		}// for ( Entry<String, Plugin> entry : this.getPlugins( ).entrySet( ) ) .
		return completeMemory;
	}

	@Override
	public String getNameOfMemoryWatchable( )
	{
		return NAME;
	}

	public PluginManagerPrefs getPrefs( )
	{
		return prefs;
	}
}
