/*
 *  Copyright (C) 2014, Thomas Obenaus. All rights reserved.
 *  Licensed under the New BSD License (3-clause lic)
 *  See attached license-file.
 *  
 *	Author: 	Thomas Obenaus
 *	EMail:		obenaus.thomas@gmail.com
 *  Project:    EthTrace
 */

package thobe.logfileviewer.kernel;

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
import thobe.logfileviewer.kernel.plugin.Plugin;
import thobe.logfileviewer.kernel.plugin.PluginManager;
import thobe.logfileviewer.kernel.plugin.console.Console;
import thobe.logfileviewer.kernel.plugin.taskview.TaskView;
import thobe.logfileviewer.kernel.source.LogStream;
import thobe.logfileviewer.kernel.source.connector.LogStreamConnector;
import thobe.logfileviewer.kernel.source.listeners.ILogStreamStateListener;
import thobe.logfileviewer.kernel.util.StatsPrinter;

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

	public LogFileViewerApp( )
	{
		super( "LogFileViewerApp" );
		this.log = Logger.getLogger( "thobe.logfileviewer.kernel.LogFileViewerApp" );
		this.listeners = new ArrayList<>( );
		this.events = new ConcurrentLinkedDeque<>( );
		this.logStream = new LogStream( );
		this.logStream.addLogStreamStateListener( this );
		this.pluginManager = new PluginManager( );
		this.eventSem = new Semaphore( 0, true );

		// starting background task, that prints out some statistics
		this.statsPrinter = new StatsPrinter( this.pluginManager, this.logStream );
		this.statsPrinter.start( );

		// starting background task, that opens and keeps connections alive
		this.logStreamConnector = new LogStreamConnector( this.logStream );
		this.logStreamConnector.start( );

		// starting background task, that watches and clears memory
		this.memoryWatchDog = new MemoryWatchDog( );
		this.memoryWatchDog.register( this.pluginManager );
		this.memoryWatchDog.register( this.logStream );
		this.memoryWatchDog.start( );
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

		// register console-plugin
		pluginManager.registerPlugin( new Console( ) );

		// register the performance-plugin
		//		pluginManager.registerPlugin( new PerformanceMonitor( ) );

		// register the task-view-plugin
		pluginManager.registerPlugin( new TaskView( ) );

		// look for new plugins
		pluginManager.findAndRegisterPlugins( );

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
			LOG( ).info( "\t- Start: '" + plugin.getPluginName( ) + "'" );
			plugin.start( );
		}// for ( Entry<String, IPlugin> entry : this.pluginManager.getPlugins( ).entrySet( ) ) .
		for ( Entry<String, Plugin> entry : this.pluginManager.getPlugins( ).entrySet( ) )
		{
			Plugin plugin = entry.getValue( );
			plugin.onStarted( );
		}// for ( Entry<String, IPlugin> entry : this.pluginManager.getPlugins( ).entrySet( ) ) .
		elapsedTime = System.currentTimeMillis( ) - elapsedTime;
		LOG( ).info( "1. Start --> notify all plugins ... done; took " + ( elapsedTime / 1000.0f ) + "s" );

		// 2. register all plugins
		elapsedTime = System.currentTimeMillis( );
		LOG( ).info( "2. Register --> notify all plugins ... " );
		for ( Entry<String, Plugin> entry : this.pluginManager.getPlugins( ).entrySet( ) )
		{
			Plugin plugin = entry.getValue( );
			LOG( ).info( "\t- Register: '" + plugin.getPluginName( ) + "'" );
			plugin.onRegistered( this.pluginManager );
		}// for ( Entry<String, IPlugin> entry : this.pluginManager.getPlugins( ).entrySet( ) ) .
		elapsedTime = System.currentTimeMillis( ) - elapsedTime;
		LOG( ).info( "2. Register --> notify all plugins ... done; took " + ( elapsedTime / 1000.0f ) + "s" );

		this.fireOnPluginsAvailable( );
	}

	private void onQuit( )
	{
		// detach all plugins from the LogStream
		this.onLogStreamClosed( );

		// 3. unregister all plugins
		long elapsedTime = System.currentTimeMillis( );
		LOG( ).info( "3. Unregister --> notify all plugins ... " );
		Map<String, Plugin> tmpPlugins = new HashMap<>( this.pluginManager.getPlugins( ) );
		for ( Entry<String, Plugin> entry : tmpPlugins.entrySet( ) )
		{
			Plugin plugin = entry.getValue( );
			LOG( ).info( "\t- Unregister: '" + plugin.getPluginName( ) + "'" );
			plugin.onUnRegistered( );
			this.pluginManager.unregisterPlugin( plugin );
		}// for (  Entry<String, IPlugin> entry : tmpPlugins.entrySet( ) ).

		elapsedTime = System.currentTimeMillis( ) - elapsedTime;
		LOG( ).info( "3. Unregister --> notify all plugins ... done; took " + ( elapsedTime / 1000.0f ) + "s" );

		// 4. stop all plugins
		elapsedTime = System.currentTimeMillis( );
		LOG( ).info( "4. Stopped--> notify all plugins ... " );
		for ( Entry<String, Plugin> entry : tmpPlugins.entrySet( ) )
		{
			Plugin plugin = entry.getValue( );
			LOG( ).info( "\t- Stop: '" + plugin.getPluginName( ) + "'" );
			long elapsedTimeForPlugin = System.currentTimeMillis( );
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
		}// for (  Entry<String, IPlugin> entry : tmpPlugins.entrySet( ) ).
		elapsedTime = System.currentTimeMillis( ) - elapsedTime;
		LOG( ).info( "4. Stopped --> notify all plugins ... done; took " + ( elapsedTime / 1000.0f ) + "s" );

		// quit the StatsPrinter too
		if ( this.statsPrinter != null )
		{
			this.statsPrinter.quit( );
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
			this.logStreamConnector.quit( );
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
			this.memoryWatchDog.quit( );
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
	}

	private void onLogStreamClosed( )
	{
		// 2b. prepare closing the LogStream
		long elapsedTime = System.currentTimeMillis( );
		LOG( ).info( "2b. Prepare LogStream closed --> notify all plugins ... " );
		for ( Entry<String, Plugin> entry : this.pluginManager.getPlugins( ).entrySet( ) )
		{
			Plugin plugin = entry.getValue( );
			plugin.onPrepareCloseLogStream( this.logStream );
		}// for ( Entry<String, IPlugin> entry : this.pluginManager.getPlugins( ).entrySet( ) ) .
		elapsedTime = System.currentTimeMillis( ) - elapsedTime;
		LOG( ).info( "2b. Prepare LogStream closed --> notify all plugins ... done; took " + ( elapsedTime / 1000.0f ) + "s" );

		// 2c. closing the LogStream
		elapsedTime = System.currentTimeMillis( );
		LOG( ).info( "2c. LogStream closed --> notify all plugins ... " );
		for ( Entry<String, Plugin> entry : this.pluginManager.getPlugins( ).entrySet( ) )
		{
			Plugin plugin = entry.getValue( );
			plugin.onLogStreamClosed( );
		}// for ( Entry<String, IPlugin> entry : this.pluginManager.getPlugins( ).entrySet( ) ) .
		elapsedTime = System.currentTimeMillis( ) - elapsedTime;
		LOG( ).info( "2c. LogStream closed --> notify all plugins ... done; took " + ( elapsedTime / 1000.0f ) + "s" );
	}

	private void onLogStreamOpened( )
	{
		// 2a. opening the LogStream
		long elapsedTime = System.currentTimeMillis( );
		LOG( ).info( "2a. LogStream opened --> notify all plugins ... " );
		for ( Entry<String, Plugin> entry : this.pluginManager.getPlugins( ).entrySet( ) )
		{
			Plugin plugin = entry.getValue( );
			plugin.onLogStreamOpened( this.logStream );
		}// for ( Entry<String, IPlugin> entry : this.pluginManager.getPlugins( ).entrySet( ) ) .
		elapsedTime = System.currentTimeMillis( ) - elapsedTime;
		LOG( ).info( "2a. LogStream opened --> notify all plugins ... done; took " + ( elapsedTime / 1000.0f ) + "s" );
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
