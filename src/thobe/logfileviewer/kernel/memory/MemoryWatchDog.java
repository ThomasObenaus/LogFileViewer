/*
 *  Copyright (C) 2014, Thomas Obenaus. All rights reserved.
 *  Licensed under the New BSD License (3-clause lic)
 *  See attached license-file.
 *
 *	Author: 	Thomas Obenaus
 *	EMail:		obenaus.thomas@gmail.com
 *  Project:    LogFileViewer
 */

package thobe.logfileviewer.kernel.memory;

import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

import thobe.logfileviewer.kernel.plugin.Plugin;
import thobe.logfileviewer.kernel.plugin.PluginManager;
import thobe.logfileviewer.kernel.source.LogStream;

/**
 * A watchdog keeping track of the current memory consumption.
 * @author Thomas Obenaus
 * @source MemoryWatchdog.java
 * @date 23.09.2014
 */
public class MemoryWatchDog extends Thread
{
	private static final String	NAME	= "thobe.logfileviewer.kernel.memory.MemoryWatchDog";

	private PluginManager		pluginManager;
	private Logger				log;
	private AtomicBoolean		quitRequested;
	private AtomicInteger		intervalTime;
	private AtomicLong			memoryThreshold;

	private LogStream			logStream;

	public MemoryWatchDog( PluginManager pluginManager, LogStream logStream )
	{
		super( NAME );
		this.logStream = logStream;
		this.log = Logger.getLogger( NAME );
		this.quitRequested = new AtomicBoolean( false );
		this.intervalTime = new AtomicInteger( 1000 );
		this.memoryThreshold = new AtomicLong( 1000 * 1024 * 1024 );
		this.pluginManager = pluginManager;
	}

	public void quit( )
	{
		this.quitRequested.set( true );
	}

	public void setIntervalTime( AtomicInteger intervalTime )
	{
		this.intervalTime = intervalTime;
	}

	public void setMemoryThreshold( long memoryThreshold )
	{
		this.memoryThreshold.set( memoryThreshold );
	}

	@Override
	public void run( )
	{
		LOG( ).info( "Thread: " + NAME + " started" );
		while ( !this.quitRequested.get( ) )
		{
			long completeMemory = 0;
			for ( Entry<String, Plugin> entry : this.pluginManager.getPlugins( ).entrySet( ) )
			{
				Plugin plugin = entry.getValue( );
				completeMemory += plugin.getCurrentMemory( );
			}// for ( Entry<String, Plugin> entry : this.pluginManager.getPlugins( ).entrySet( ) ) .

			// add memory of logstream
			completeMemory += this.logStream.getLogLineFactory( ).getCacheMemory( );

			if ( completeMemory >= memoryThreshold.get( ) )
			{
				LOG( ).info( "Memorythreshold exceeded (threshold=" + ( memoryThreshold.get( ) / 1024f / 1024f ) + "MB, currentMemory=" + ( completeMemory / 1024f / 1024f ) + "MB)" );
				for ( Entry<String, Plugin> entry : this.pluginManager.getPlugins( ).entrySet( ) )
				{
					entry.getValue( ).freeMemory( );
				}// for ( Entry<String, Plugin> entry : this.pluginManager.getPlugins( ).entrySet( ) ).

				// free memory of LogStream too
				this.logStream.getLogLineFactory( ).clearCache( );

			}// if ( completeMemory >= memoryThreshold.get( ) ).

			// wait
			try
			{
				Thread.sleep( this.intervalTime.get( ) );
			}
			catch ( InterruptedException e )
			{
				this.quitRequested.set( true );
			}
		}// while ( !this.quitRequested.get( ) ).
		LOG( ).info( "Thread: " + NAME + " stopped" );
	}

	private Logger LOG( )
	{
		return this.log;
	}
}
