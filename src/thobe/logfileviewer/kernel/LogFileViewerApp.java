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

import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import thobe.logfileviewer.kernel.plugin.IPlugin;
import thobe.logfileviewer.kernel.plugin.PluginManager;
import thobe.logfileviewer.kernel.plugin.console.Console;
import thobe.logfileviewer.kernel.source.LogStream;
import thobe.logfileviewer.kernel.source.listeners.LogStreamStateListener;

/**
 * @author Thomas Obenaus
 * @source LogFileViewerApp.java
 * @date May 15, 2014
 */
public class LogFileViewerApp extends Thread implements LogStreamStateListener
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
	private PluginManager					pluginManager;
	private Semaphore						eventSem;

	public LogFileViewerApp( )
	{
		super( "LogFileViewerApp" );
		this.log = Logger.getLogger( "thobe.logfileviewer.kernel.LogFileViewerApp" );
		this.events = new ConcurrentLinkedDeque<>( );
		this.logStream = new LogStream( );
		this.logStream.addLogStreamStateListener( this );
		this.pluginManager = new PluginManager( );
		this.eventSem = new Semaphore( 0, true );
	}

	public LogStream getLogStream( )
	{
		return logStream;
	}

	public PluginManager getPluginManager( )
	{
		return pluginManager;
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
		for ( Entry<String, IPlugin> entry : this.pluginManager.getPlugins( ).entrySet( ) )
		{
			IPlugin plugin = entry.getValue( );
			LOG( ).info( "\t- Start: '" + plugin.getPluginName( ) + "'" );
			plugin.start( );
		}// for ( Entry<String, IPlugin> entry : this.pluginManager.getPlugins( ).entrySet( ) ) .
		for ( Entry<String, IPlugin> entry : this.pluginManager.getPlugins( ).entrySet( ) )
		{
			IPlugin plugin = entry.getValue( );
			plugin.onStarted( );
		}// for ( Entry<String, IPlugin> entry : this.pluginManager.getPlugins( ).entrySet( ) ) .
		elapsedTime = System.currentTimeMillis( ) - elapsedTime;
		LOG( ).info( "1. Start --> notify all plugins ... done; took " + ( elapsedTime / 1000.0f ) + "s" );

		// 2. register all plugins
		elapsedTime = System.currentTimeMillis( );
		LOG( ).info( "2. Register --> notify all plugins ... " );
		for ( Entry<String, IPlugin> entry : this.pluginManager.getPlugins( ).entrySet( ) )
		{
			IPlugin plugin = entry.getValue( );
			LOG( ).info( "\t- Register: '" + plugin.getPluginName( ) + "'" );
			plugin.onRegistered( this.pluginManager );
		}// for ( Entry<String, IPlugin> entry : this.pluginManager.getPlugins( ).entrySet( ) ) .
		elapsedTime = System.currentTimeMillis( ) - elapsedTime;
		LOG( ).info( "2. Register --> notify all plugins ... done; took " + ( elapsedTime / 1000.0f ) + "s" );

	}

	private void onQuit( )
	{
		// detach all plugins from the LogStream
		this.onLogStreamClosed( );

		// 3. unregister all plugins
		long elapsedTime = System.currentTimeMillis( );
		LOG( ).info( "3. Unregister --> notify all plugins ... " );
		Map<String, IPlugin> tmpPlugins = new HashMap<>( this.pluginManager.getPlugins( ) );
		for ( Entry<String, IPlugin> entry : tmpPlugins.entrySet( ) )
		{
			IPlugin plugin = entry.getValue( );
			LOG( ).info( "\t- Unregister: '" + plugin.getPluginName( ) + "'" );
			plugin.onUnRegistered( );
			this.pluginManager.unregisterPlugin( plugin );
		}// for (  Entry<String, IPlugin> entry : tmpPlugins.entrySet( ) ).

		elapsedTime = System.currentTimeMillis( ) - elapsedTime;
		LOG( ).info( "3. Unregister --> notify all plugins ... done; took " + ( elapsedTime / 1000.0f ) + "s" );

		// 4. stop all plugins
		elapsedTime = System.currentTimeMillis( );
		LOG( ).info( "4. Stopped--> notify all plugins ... " );
		for ( Entry<String, IPlugin> entry : tmpPlugins.entrySet( ) )
		{
			IPlugin plugin = entry.getValue( );
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

	}

	private void onLogStreamClosed( )
	{
		// 2b. prepare closing the LogStream
		long elapsedTime = System.currentTimeMillis( );
		LOG( ).info( "2b. Prepare LogStream closed --> notify all plugins ... " );
		for ( Entry<String, IPlugin> entry : this.pluginManager.getPlugins( ).entrySet( ) )
		{
			IPlugin plugin = entry.getValue( );
			plugin.onPrepareCloseLogStream( );
		}// for ( Entry<String, IPlugin> entry : this.pluginManager.getPlugins( ).entrySet( ) ) .
		elapsedTime = System.currentTimeMillis( ) - elapsedTime;
		LOG( ).info( "2b. Prepare LogStream closed --> notify all plugins ... done; took " + ( elapsedTime / 1000.0f ) + "s" );

		// 2c. closing the LogStream
		elapsedTime = System.currentTimeMillis( );
		LOG( ).info( "2c. LogStream closed --> notify all plugins ... " );
		for ( Entry<String, IPlugin> entry : this.pluginManager.getPlugins( ).entrySet( ) )
		{
			IPlugin plugin = entry.getValue( );
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
		for ( Entry<String, IPlugin> entry : this.pluginManager.getPlugins( ).entrySet( ) )
		{
			IPlugin plugin = entry.getValue( );
			plugin.onLogStreamOpened( );
		}// for ( Entry<String, IPlugin> entry : this.pluginManager.getPlugins( ).entrySet( ) ) .
		elapsedTime = System.currentTimeMillis( ) - elapsedTime;
		LOG( ).info( "2a. LogStream opened --> notify all plugins ... done; took " + ( elapsedTime / 1000.0f ) + "s" );
	}

	private Logger LOG( )
	{
		return this.log;
	}

	@Override
	public void onEOFReached( )
	{
		// TODO Auto-generated method stub

	}

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
}
