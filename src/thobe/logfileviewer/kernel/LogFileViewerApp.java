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
import thobe.logfileviewer.kernel.source.DataSource;

/**
 * @author Thomas Obenaus
 * @source LogFileViewerApp.java
 * @date May 15, 2014
 */
public class LogFileViewerApp extends Thread
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
	 * The {@link DataSource} providing the contents of the logfile that should be displayed
	 */
	private DataSource						dataSource;
	private PluginManager					pluginManager;
	private Semaphore						eventSem;

	public LogFileViewerApp( )
	{
		super( "LogFileViewerApp" );
		this.log = Logger.getLogger( "thobe.logfileviewer.kernel.LogFileViewerApp" );
		this.events = new ConcurrentLinkedDeque<>( );
		this.dataSource = new DataSource( );
		this.pluginManager = new PluginManager( );
		this.eventSem = new Semaphore( 0, true );

	}

	public DataSource getDataSource( )
	{
		return dataSource;
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
		LOG( ).info( "Thread " + this.getName( ) + " started" );

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
				LogFileViewerAppEvent event = this.events.peekFirst( );

				switch ( event )
				{
				case QUIT:
					quitRequested = true;
					continue;
				case DS_OPENED:
					onDataSourceOpened( );
					break;
				case DS_CLOSED:
					onDataSourceClosed( );
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

		LOG( ).info( "Thread " + this.getName( ) + " stopped" );
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
		// detach all plugins from the datasource
		this.onDataSourceClosed( );

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

	private void onDataSourceClosed( )
	{
		// 2b. prepare closing the datasource
		long elapsedTime = System.currentTimeMillis( );
		LOG( ).info( "2b. PrepareDataSource closed --> notify all plugins ... " );
		for ( Entry<String, IPlugin> entry : this.pluginManager.getPlugins( ).entrySet( ) )
		{
			IPlugin plugin = entry.getValue( );
			plugin.onPrepareCloseDataSource( );
		}// for ( Entry<String, IPlugin> entry : this.pluginManager.getPlugins( ).entrySet( ) ) .
		elapsedTime = System.currentTimeMillis( ) - elapsedTime;
		LOG( ).info( "2b. PrepareDataSource closed --> notify all plugins ... done; took " + ( elapsedTime / 1000.0f ) + "s" );

		// 2c. closing the datasource
		elapsedTime = System.currentTimeMillis( );
		LOG( ).info( "2c. DataSource closed --> notify all plugins ... " );
		for ( Entry<String, IPlugin> entry : this.pluginManager.getPlugins( ).entrySet( ) )
		{
			IPlugin plugin = entry.getValue( );
			plugin.onDataSourceClosed( );
		}// for ( Entry<String, IPlugin> entry : this.pluginManager.getPlugins( ).entrySet( ) ) .
		elapsedTime = System.currentTimeMillis( ) - elapsedTime;
		LOG( ).info( "2c. DataSource closed --> notify all plugins ... done; took " + ( elapsedTime / 1000.0f ) + "s" );
	}

	private void onDataSourceOpened( )
	{
		// 2a. opening the datasource
		long elapsedTime = System.currentTimeMillis( );
		LOG( ).info( "2a. DataSource opened --> notify all plugins ... " );
		for ( Entry<String, IPlugin> entry : this.pluginManager.getPlugins( ).entrySet( ) )
		{
			IPlugin plugin = entry.getValue( );
			plugin.onDataSourceOpened( );
		}// for ( Entry<String, IPlugin> entry : this.pluginManager.getPlugins( ).entrySet( ) ) .
		elapsedTime = System.currentTimeMillis( ) - elapsedTime;
		LOG( ).info( "2a. DataSource opened --> notify all plugins ... done; took " + ( elapsedTime / 1000.0f ) + "s" );
	}

	private Logger LOG( )
	{
		return this.log;
	}
}
