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
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import thobe.logfileviewer.kernel.memory.MemoryWatchDog;
import thobe.logfileviewer.kernel.plugin.PluginManager;
import thobe.logfileviewer.kernel.plugin.PluginManagerException;
import thobe.logfileviewer.kernel.preferences.LogFileViewerPreferences;
import thobe.logfileviewer.kernel.source.connector.LogStreamConnector;
import thobe.logfileviewer.kernel.source.logstream.LogStream;
import thobe.logfileviewer.kernel.util.StatsPrinter;
import thobe.logfileviewer.plugin.Plugin;
import thobe.logfileviewer.plugin.api.IPluginPreferences;
import thobe.logfileviewer.plugin.source.logstream.ILogStreamStateListener;
import thobe.tools.preferences.PreferenceManager;
import thobe.tools.preferences.PrefsException;

/**
 * @author Thomas Obenaus
 * @source LogFileViewerApp.java
 * @date May 15, 2014
 */
public class LogFileViewerApp extends Thread implements ILogStreamStateListener
{
	/**
	 * {@link Deque} holding all events for the app
	 */
	private Deque<LogFileViewerAppEvent>	events;

	/**
	 * {@link Logger} of this app
	 */
	private Logger							log;

	/**
	 * The {@link LogStream} providing the contents of the logfile that should be displayed
	 */
	private LogStream						logStream;

	/**
	 * List of listeners that want to monitor the {@link LogFileViewerApp}.
	 */
	private List<ILogFileViewerAppListener>	listeners;

	/**
	 * Manager responsible to find an manage {@link Plugin}s.
	 */
	private PluginManager					pluginManager;

	/**
	 * Semaphore for the internal event-queue.
	 */
	private Semaphore						eventSem;

	/**
	 * Thread responsible for opening and keeping connections alive.
	 */
	private LogStreamConnector				logStreamConnector;

	/**
	 * Backgroundtask that porints out some statistics
	 */
	private StatsPrinter					statsPrinter;

	/**
	 * Thread responsible for watching and clearing memory of (e.g. of plungins)
	 */
	private MemoryWatchDog					memoryWatchDog;

	/**
	 * Preferences for the {@link LogFileViewerApp}.
	 */
	private LogFileViewerPreferences		preferences;

	/**
	 * Configuration for the LogFileViewer.
	 */
	private LogFileViewerConfiguration		configuration;

	public LogFileViewerApp( )
	{
		super( "LogFileViewerApp" );
		this.log = Logger.getLogger( "thobe.logfileviewer.kernel.LogFileViewerApp" );
		this.listeners = new ArrayList<>( );
		this.events = new ConcurrentLinkedDeque<>( );
		this.logStream = new LogStream( );
		this.logStream.addLogStreamStateListener( this );
		this.eventSem = new Semaphore( 0, true );

		// create/load preferences
		LOG( ).info( "Create/Load preferences..." );
		this.preferences = new LogFileViewerPreferences( );
		PreferenceManager.createPrefs( preferences );
		LOG( ).info( "Create/Load preferences...done" );

		// load configuration
		File configFile = new File( LogFileViewerConfiguration.getDefaultConfigFileName( ) );
		LOG( ).info( "Load configuration from '" + configFile.getAbsolutePath( ) + "'..." );
		this.configuration = new LogFileViewerConfiguration( configFile );
		LOG( ).info( "Load configuration from '" + configFile.getAbsolutePath( ) + "'...done." );

		// create the plugin-manager		
		LOG( ).info( "Create the pluginmanager..." );
		this.pluginManager = new PluginManager( this.preferences.getPluginManagerPreferences( ), this.configuration.getPluginDirectory( ) );
		LOG( ).info( "Create the pluginmanager...done" );

		LOG( ).info( "Create background tasks..." );
		// create background task, that prints out some statistics
		this.statsPrinter = new StatsPrinter( this.pluginManager, this.logStream );

		// create background task, that opens and keeps connections alive
		this.logStreamConnector = new LogStreamConnector( this.logStream );

		// create background task, that watches and clears memory
		this.memoryWatchDog = new MemoryWatchDog( );
		LOG( ).info( "Create background tasks...done" );
	}

	public LogFileViewerConfiguration getConfiguration( )
	{
		return configuration;
	}

	public PluginManager getPluginManager( )
	{
		return pluginManager;
	}

	public LogStreamConnector getLogStreamConnector( )
	{
		return logStreamConnector;
	}

	public void removeListener( ILogFileViewerAppListener l )
	{
		synchronized ( this.listeners )
		{
			this.listeners.remove( l );
		}
	}

	public void addListener( ILogFileViewerAppListener l )
	{
		synchronized ( this.listeners )
		{
			this.listeners.add( l );
		}
	}

	public LogStream getLogStream( )
	{
		return logStream;
	}

	public void quit( )
	{
		this.events.push( LogFileViewerAppEvent.QUIT );
		this.eventSem.release( );
	}

	@Override
	public void run( )
	{
		LOG( ).info( "Thread " + this.getLogStreamListenerName( ) + " started" );

		// INITAL start bg-tasks ########################################## 
		LOG( ).info( "Starting background tasks..." );
		// starting background task LogStream
		this.logStream.start( );

		// starting background task, that prints out some statistics
		this.statsPrinter.start( );

		// starting background task, that opens and keeps connections alive
		this.logStreamConnector.start( );

		// starting background task, that watches and clears memory
		this.memoryWatchDog.register( this.pluginManager );
		this.memoryWatchDog.register( this.logStream );
		this.memoryWatchDog.start( );
		LOG( ).info( "Starting background tasks...done" );

		try
		{
			// look for new plugins
			pluginManager.findAndRegisterPlugins( );
		}
		catch ( PluginManagerException e )
		{
			LOG( ).severe( "Unable to load plugins: " + e.getLocalizedMessage( ) );
		}

		// start
		onStart( );

		boolean quitRequested = false;
		while ( !quitRequested )
		{
			// process events 
			if ( !this.events.isEmpty( ) )
			{
				LogFileViewerAppEvent event = this.events.pollFirst( );

				switch ( event )
				{
				case QUIT:
					quitRequested = true;
					continue;
				case LS_OPENED:
					onLogStreamOpened( );
					break;
				case LS_CLOSED:
					onLogStreamClosed( );
					break;
				}
			}// if(!this.events.isEmpty( )) .

			try
			{
				this.eventSem.tryAcquire( 2, TimeUnit.SECONDS );
			}
			catch ( InterruptedException e )
			{
				LOG( ).severe( "Semaphore wait was interrupted: " + e.getLocalizedMessage( ) );
			}
		}// while ( !quitRequested ).

		// quit the application
		onQuit( );

		LOG( ).info( "Thread " + this.getLogStreamListenerName( ) + " stopped" );
	}

	private void onStart( )
	{
		// 1. start all plugins
		long elapsedTime = System.currentTimeMillis( );
		LOG( ).info( "1. Start --> notify all plugins ... " );
		for ( Entry<String, Plugin> entry : this.pluginManager.getPlugins( ).entrySet( ) )
		{
			Plugin plugin = entry.getValue( );
			if ( plugin.isEnabled( ) )
			{
				LOG( ).info( "\t- Start: '" + plugin.getPluginName( ) + "'" );
				IPluginPreferences pluginPrefs = plugin.getPluginPreferences( );
				if ( pluginPrefs != null )
				{
					this.preferences.loadPluginPreferences( pluginPrefs, plugin.getName( ) );
					LOG( ).info( "\t- Preferences: of '" + plugin.getPluginName( ) + "' loaded." );
				}
				plugin.start( );
			}// if ( plugin.isEnabled( ) )
			else
			{
				LOG( ).info( "\t- Start: Plugin '" + plugin.getPluginName( ) + "' won't be started since it is disabled." );
			}// if ( plugin.isEnabled( ) ) ... else ..

		}// for ( Entry<String, IPlugin> entry : this.pluginManager.getPlugins( ).entrySet( ) ) .
		for ( Entry<String, Plugin> entry : this.pluginManager.getPlugins( ).entrySet( ) )
		{
			Plugin plugin = entry.getValue( );
			if ( plugin.isEnabled( ) )
			{
				plugin.onStarted( );
			}// if ( plugin.isEnabled( ) )
		}// for ( Entry<String, IPlugin> entry : this.pluginManager.getPlugins( ).entrySet( ) ) .
		elapsedTime = System.currentTimeMillis( ) - elapsedTime;
		LOG( ).info( "1. Start --> notify all plugins ... done; took " + ( elapsedTime / 1000.0f ) + "s" );

		// 2. register all plugins
		elapsedTime = System.currentTimeMillis( );
		LOG( ).info( "2. Register --> notify all plugins ... " );
		for ( Entry<String, Plugin> entry : this.pluginManager.getPlugins( ).entrySet( ) )
		{
			Plugin plugin = entry.getValue( );
			if ( plugin.isEnabled( ) )
			{
				LOG( ).info( "\t- Register: '" + plugin.getPluginName( ) + "'" );
				plugin.onRegistered( this.pluginManager );
			}// if ( plugin.isEnabled( ) )
		}// for ( Entry<String, IPlugin> entry : this.pluginManager.getPlugins( ).entrySet( ) ) .
		elapsedTime = System.currentTimeMillis( ) - elapsedTime;
		LOG( ).info( "2. Register --> notify all plugins ... done; took " + ( elapsedTime / 1000.0f ) + "s" );

		this.fireOnPluginsAvailable( );
	}

	private void onLogStreamOpened( )
	{
		// 2a. LogStream available
		long elapsedTime = System.currentTimeMillis( );
		LOG( ).info( "2a. LogStream available --> notify all plugins ... " );
		for ( Entry<String, Plugin> entry : this.pluginManager.getPlugins( ).entrySet( ) )
		{
			Plugin plugin = entry.getValue( );
			if ( plugin.isEnabled( ) )
			{
				plugin.onLogStreamAvailable( this.logStream );
			}// if ( plugin.isEnabled( ) )
		}// for ( Entry<String, IPlugin> entry : this.pluginManager.getPlugins( ).entrySet( ) ) .
		elapsedTime = System.currentTimeMillis( ) - elapsedTime;
		LOG( ).info( "2a. LogStream available --> notify all plugins ...; took " + ( elapsedTime / 1000.0f ) + "s" );

		// 2b. opening the LogStream
		elapsedTime = System.currentTimeMillis( );
		LOG( ).info( "2b. LogStream opened --> notify all plugins ... " );
		for ( Entry<String, Plugin> entry : this.pluginManager.getPlugins( ).entrySet( ) )
		{
			Plugin plugin = entry.getValue( );
			if ( plugin.isEnabled( ) )
			{
				plugin.onLogStreamOpened( );
			}// if ( plugin.isEnabled( ) )
		}// for ( Entry<String, IPlugin> entry : this.pluginManager.getPlugins( ).entrySet( ) ) .
		elapsedTime = System.currentTimeMillis( ) - elapsedTime;
		LOG( ).info( "2b. LogStream opened --> notify all plugins ... done; took " + ( elapsedTime / 1000.0f ) + "s" );
	}

	private void onLogStreamClosed( )
	{
		// 3a. prepare closing the LogStream
		long elapsedTime = System.currentTimeMillis( );
		LOG( ).info( "3a. Prepare LogStream closed --> notify all plugins ... " );
		for ( Entry<String, Plugin> entry : this.pluginManager.getPlugins( ).entrySet( ) )
		{
			Plugin plugin = entry.getValue( );
			if ( plugin.isEnabled( ) )
			{
				plugin.onPrepareCloseLogStream( );
			}// if ( plugin.isEnabled( ) )
		}// for ( Entry<String, IPlugin> entry : this.pluginManager.getPlugins( ).entrySet( ) ) .
		elapsedTime = System.currentTimeMillis( ) - elapsedTime;
		LOG( ).info( "3a. Prepare LogStream closed --> notify all plugins ... done; took " + ( elapsedTime / 1000.0f ) + "s" );

		// 3b. LogStream is leaving scope
		elapsedTime = System.currentTimeMillis( );
		LOG( ).info( "3b. LogStream is leaving scope --> notify all plugins ... " );
		for ( Entry<String, Plugin> entry : this.pluginManager.getPlugins( ).entrySet( ) )
		{
			Plugin plugin = entry.getValue( );
			if ( plugin.isEnabled( ) )
			{
				plugin.onLogStreamLeavingScope( );
			}// if ( plugin.isEnabled( ) )
		}// for ( Entry<String, IPlugin> entry : this.pluginManager.getPlugins( ).entrySet( ) ) .
		elapsedTime = System.currentTimeMillis( ) - elapsedTime;
		LOG( ).info( "3b. LogStream is leaving scope --> notify all plugins ... done; took " + ( elapsedTime / 1000.0f ) + "s" );

		// 2c. closing the LogStream
		elapsedTime = System.currentTimeMillis( );
		LOG( ).info( "3c. LogStream closed --> notify all plugins ... " );
		for ( Entry<String, Plugin> entry : this.pluginManager.getPlugins( ).entrySet( ) )
		{
			Plugin plugin = entry.getValue( );
			if ( plugin.isEnabled( ) )
			{
				plugin.onLogStreamClosed( );
			}// if ( plugin.isEnabled( ) )
		}// for ( Entry<String, IPlugin> entry : this.pluginManager.getPlugins( ).entrySet( ) ) .
		elapsedTime = System.currentTimeMillis( ) - elapsedTime;
		LOG( ).info( "3c. LogStream closed --> notify all plugins ... done; took " + ( elapsedTime / 1000.0f ) + "s" );
	}

	private void onQuit( )
	{
		// detach all plugins from the LogStream
		this.onLogStreamClosed( );

		// 3. unregister all plugins
		long elapsedTime = System.currentTimeMillis( );
		LOG( ).info( "4. Unregister --> notify all plugins ... " );
		Map<String, Plugin> tmpPlugins = new HashMap<>( this.pluginManager.getPlugins( ) );
		for ( Entry<String, Plugin> entry : tmpPlugins.entrySet( ) )
		{
			Plugin plugin = entry.getValue( );
			if ( plugin.isEnabled( ) )
			{
				LOG( ).info( "\t- Unregister: '" + plugin.getPluginName( ) + "'" );
				plugin.onUnRegistered( );
			}// if ( plugin.isEnabled( ) )
			this.pluginManager.unregisterPlugin( plugin );
		}// for (  Entry<String, IPlugin> entry : tmpPlugins.entrySet( ) ).

		elapsedTime = System.currentTimeMillis( ) - elapsedTime;
		LOG( ).info( "4. Unregister --> notify all plugins ... done; took " + ( elapsedTime / 1000.0f ) + "s" );

		// 4. stop all plugins
		elapsedTime = System.currentTimeMillis( );
		LOG( ).info( "5. Stopped--> notify all plugins ... " );
		for ( Entry<String, Plugin> entry : tmpPlugins.entrySet( ) )
		{
			Plugin plugin = entry.getValue( );
			if ( plugin.isEnabled( ) )
			{
				LOG( ).info( "\t- Stop: '" + plugin.getPluginName( ) + "'" );
				long elapsedTimeForPlugin = System.currentTimeMillis( );

				IPluginPreferences pluginPrefs = plugin.getPluginPreferences( );
				if ( pluginPrefs != null )
				{
					this.preferences.savePluginPreferences( pluginPrefs, plugin.getName( ) );
					LOG( ).info( "\t- Preferences: of '" + plugin.getPluginName( ) + "' saved." );
				}

				plugin.quit( );
				plugin.onStopped( );
				try
				{
					plugin.join( );
				}
				catch ( InterruptedException e )
				{
					e.printStackTrace( );
				}

				elapsedTimeForPlugin = System.currentTimeMillis( ) - elapsedTimeForPlugin;
				if ( elapsedTimeForPlugin > 100 )
				{
					LOG( ).warning( "Stopping plugin '" + plugin.getPluginName( ) + "' took " + ( elapsedTimeForPlugin / 1000.0f ) + "s" );
				}
			}// if ( plugin.isEnabled( ) )
		}// for (  Entry<String, IPlugin> entry : tmpPlugins.entrySet( ) ).
		elapsedTime = System.currentTimeMillis( ) - elapsedTime;
		LOG( ).info( "5. Stopped --> notify all plugins ... done; took " + ( elapsedTime / 1000.0f ) + "s" );

		// tell all threads to stop
		if ( this.logStream != null )
			this.logStream.quit( );
		if ( this.statsPrinter != null )
			this.statsPrinter.quit( );
		if ( this.logStreamConnector != null )
			this.logStreamConnector.quit( );
		if ( this.memoryWatchDog != null )
			this.memoryWatchDog.quit( );

		// wait for all threads to finish stopping
		// quit the logStream
		if ( this.logStream != null )
		{
			try
			{
				this.logStream.interrupt( );
				this.logStream.join( );
			}
			catch ( InterruptedException e )
			{
				LOG( ).severe( "Exception while closing LogStream: " + e.getLocalizedMessage( ) );
			}
		}// if ( this.logStream != null ).

		// quit the StatsPrinter too
		if ( this.statsPrinter != null )
		{
			try
			{
				this.statsPrinter.interrupt( );
				this.statsPrinter.join( );
			}
			catch ( InterruptedException e )
			{
				LOG( ).severe( "Exception while closing StatsPrinter: " + e.getLocalizedMessage( ) );
			}
		}// if ( this.statsPrinter != null ).

		// quit the LogStreamConnector
		if ( this.logStreamConnector != null )
		{
			try
			{
				this.logStreamConnector.interrupt( );
				this.logStreamConnector.join( );
			}
			catch ( InterruptedException e )
			{
				LOG( ).severe( "Exception while closing LogStreamConnector: " + e.getLocalizedMessage( ) );
			}
		}// if ( this.logStreamConnector != null ).

		// quit the memorywatchdog
		if ( this.memoryWatchDog != null )
		{
			try
			{
				this.memoryWatchDog.interrupt( );
				this.memoryWatchDog.join( );
			}
			catch ( InterruptedException e )
			{
				LOG( ).severe( "Exception while closing MemoryWatchDog: " + e.getLocalizedMessage( ) );
			}
		}// if ( this.logStreamConnector != null ).

		// save all preferences
		try
		{
			LOG( ).info( "Save all preferences" );
			PreferenceManager.get( ).save( );
		}
		catch ( PrefsException e )
		{
			LOG( ).severe( "Exception while saving the preferences: " + e.getLocalizedMessage( ) );
		}
	}

	private void fireOnPluginsAvailable( )
	{
		synchronized ( this.listeners )
		{
			long elapsedTime = System.currentTimeMillis( );
			LOG( ).info( "Publish: plugins are available (started and registered) ... " );
			for ( ILogFileViewerAppListener l : this.listeners )
			{
				l.newPluginsAvailable( this.pluginManager );
			}// for ( LogFileViewerAppListener l : this.listeners ).
			elapsedTime = System.currentTimeMillis( ) - elapsedTime;
			LOG( ).info( "Publish: plugins are available (started and registered) ... done; took " + ( elapsedTime / 1000.0f ) + "s" );
		}// synchronized ( this.listeners ).
	}

	private Logger LOG( )
	{
		return this.log;
	}

	@Override
	public void onEOFReached( )
	{}

	@Override
	public void onOpened( )
	{
		this.events.push( LogFileViewerAppEvent.LS_OPENED );
		this.eventSem.release( );
	}

	@Override
	public void onClosed( )
	{
		this.events.push( LogFileViewerAppEvent.LS_CLOSED );
		this.eventSem.release( );
	}

	@Override
	public String getLogStreamListenerName( )
	{
		return "LogFileViewerApp";
	}

	public LogFileViewerPreferences getPreferences( )
	{
		return preferences;
	}

	/**
	 * @author Thomas Obenaus
	 * @source LogFileViewerAppEvent.java
	 * @date May 31, 2014
	 */
	private enum LogFileViewerAppEvent
	{
		QUIT, LS_OPENED, LS_CLOSED;
	}
}
