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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

/**
 * A watchdog keeping track of the current memory consumption.
 * @author Thomas Obenaus
 * @source MemoryWatchdog.java
 * @date 23.09.2014
 */
public class MemoryWatchDog extends Thread
{
	private static final String		NAME	= "thobe.logfileviewer.kernel.memory.MemoryWatchDog";

	/**
	 * List of instances that should be monitored.
	 */
	private List<IMemoryWatchable>	toBeWatched;

	private Logger					log;
	private AtomicBoolean			quitRequested;
	private AtomicInteger			intervalTime;
	private AtomicLong				memoryThreshold;

	public MemoryWatchDog( )
	{
		super( NAME );
		this.toBeWatched = new ArrayList<>( );

		this.log = Logger.getLogger( NAME );
		this.quitRequested = new AtomicBoolean( false );
		this.intervalTime = new AtomicInteger( 1000 );
		this.memoryThreshold = new AtomicLong( 1000 * 1024 * 1024 );
	}

	/**
	 * Register a {@link IMemoryWatchable}
	 * @param memoryWatchable
	 */
	public void register( IMemoryWatchable memoryWatchable )
	{
		synchronized ( this.toBeWatched )
		{
			this.toBeWatched.add( memoryWatchable );
			LOG( ).info( "'" + memoryWatchable.getNameOfMemoryWatchable( ) + "' registered ... will be monitored now." );
		}// synchronized ( this.toBeWatched ).
	}

	/**
	 * Quit this thread/ service
	 */
	public void quit( )
	{
		this.quitRequested.set( true );
	}

	/**
	 * Set the sleep-time/ interval for next watches
	 * @param intervalTime
	 */
	public void setIntervalTime( AtomicInteger intervalTime )
	{
		this.intervalTime = intervalTime;
	}

	/**
	 * Sets the threshold at which a free of the memory should be called.
	 * @param memoryThreshold
	 */
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

			// collect the current memory-usage
			synchronized ( this.toBeWatched )
			{
				for ( IMemoryWatchable watchable : this.toBeWatched )
				{
					completeMemory += watchable.getMemory( );
				}// for(IMemoryWatchable watchable : this.toBeWatched ).
			}// synchronized ( this.toBeWatched ).

			// check threshold
			if ( completeMemory >= memoryThreshold.get( ) )
			{
				LOG( ).info( "Memorythreshold exceeded (threshold=" + ( memoryThreshold.get( ) / 1024f / 1024f ) + "MB, currentMemory=" + ( completeMemory / 1024f / 1024f ) + "MB)" );

				// free memory
				synchronized ( this.toBeWatched )
				{
					for ( IMemoryWatchable watchable : this.toBeWatched )
					{
						LOG( ).info( "Free memory of '" + watchable.getNameOfMemoryWatchable( ) + "'" );
						watchable.freeMemory( );

					}// for(IMemoryWatchable watchable : this.toBeWatched ).
				}// synchronized ( this.toBeWatched ).

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
