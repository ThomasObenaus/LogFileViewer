/*
 *  Copyright (C) 2014, Thomas Obenaus. All rights reserved.
 *  Licensed under the New BSD License (3-clause lic)
 *  See attached license-file.
 *
 *	Author: 	Thomas Obenaus
 *	EMail:		obenaus.thomas@gmail.com
 *  Project:    LogFileViewer
 */

package thobe.logfileviewer.kernel.source.logline;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

import thobe.logfileviewer.kernel.source.err.LogLineBufferException;
import thobe.logfileviewer.kernel.util.SizeOf;

/**
 * Buffer for instances of {@link ILogLine}s with a max capacity.
 * @author Thomas Obenaus
 * @source LogLineBuffer.java
 * @date Oct 12, 2014
 */
public class LogLineBuffer implements ILogLineBuffer
{
	/**
	 * Default initial capacity (#items)
	 */
	private static final int	DEFAULT_MAX_CAPACITY	= 100000;

	/**
	 * Name of the logchannel
	 */
	private static final String	NAME					= "thobe.logfileviewer.kernel.source.logline.LogLineBuffer";

	/**
	 * Internal list of buffered {@link LogLine}s
	 */
	private List<ILogLine>		internalBuffer;

	/**
	 * Max capacity (#items)
	 */
	private int					maxCapacity;

	/**
	 * Load factor ... number of items the buffer will contain after it has reached its max capacity (this factor is the percentage of the
	 * max capacity).
	 * E.g. if max capacity is 100 and the load factor is 0.75 the buffer will contain 75 items after reaching its max capacity.
	 */
	private float				loadFactor;

	/**
	 * Logger
	 */
	private Logger				log;

	/**
	 * Timer keeping track if this buffer has reached its maximum capacity.
	 */
	private Timer				bufferOverflowWatcherTimer;

	/**
	 * Current memory usage.
	 */
	private long				memory;

	/**
	 * DefCtor with maxCapacity=100000 and loadFactor=0.75
	 */
	public LogLineBuffer( )
	{
		this( DEFAULT_MAX_CAPACITY, 0.75f );
	}

	/**
	 * Ctor
	 * @param maxCapacity - the maximum capacity (entries of {@link ILogLine}s) of this buffer.
	 * @param loadFactor - the percentage of maxCapacity the buffer is filled after it has to be shrink on reaching its maxCapacity.
	 *            For example if the maxCapa is 100 and the loadFactor 0.75 the buffer will contain at least 75 {@link ILogLine}s after
	 *            reaching the limit of 100.
	 */
	public LogLineBuffer( int maxCapacity, float loadFactor )
	{
		this.maxCapacity = maxCapacity;
		this.loadFactor = loadFactor;
		if ( this.maxCapacity < 1 )
			throw new IllegalArgumentException( "MaxCapacity should never be smaller than 1" );
		if ( this.loadFactor > 1.0 )
			throw new IllegalArgumentException( "LoadFactor should never be greater than 1.0 (100%)" );

		this.internalBuffer = new LinkedList<ILogLine>( );
		this.log = Logger.getLogger( NAME );

		bufferOverflowWatcherTimer = new Timer( NAME + ".Timer" );
		bufferOverflowWatcherTimer.schedule( new BufferOverFlowWatcher( this ), 4000, 4000 );
	}

	/**
	 * Add a list (to the end of the buffer) of new {@link ILogLine} which are already sorted by its id (in ascending order)
	 * @param entries
	 * @throws LogLineBufferException if the block/the entries to be added overlaps the entries already in the internal buffer.
	 */
	public void add( List<ILogLine> entries ) throws LogLineBufferException
	{
		// nothing to do
		if ( entries.isEmpty( ) )
			return;

		synchronized ( this.internalBuffer )
		{
			// check if we can add the block or if it overlaps
			if ( !this.internalBuffer.isEmpty( ) )
			{
				ILogLine lastEntry = this.internalBuffer.get( this.internalBuffer.size( ) - 1 );
				ILogLine firstEntry = entries.get( 0 );
				if ( lastEntry.getId( ) >= firstEntry.getId( ) )
				{
					throw new LogLineBufferException( "Unable to add this block, since the ids overlap. idOfLastEntryInBuffer=" + lastEntry.getId( ) + ", idOfFirstEntryInNewBlock=" + firstEntry.getId( ) );
				}
			}// if ( !this.internalBuffer.isEmpty( ) ).

			this.internalBuffer.addAll( this.internalBuffer.size( ), entries );
		}// synchronized ( this.internalBuffer ).

		for ( ILogLine l : entries )
			this.memory += l.getMemory( );
	}

	/**
	 * Add a {@link ILogLine} (to the end of the buffer).
	 * @param entry
	 * @throws LogLineBufferException if the block/the entries to be added overlaps the entries already in the internal buffer.
	 */
	public void add( ILogLine entry ) throws LogLineBufferException
	{
		synchronized ( this.internalBuffer )
		{
			// check if we can add the block or if it overlaps
			if ( !this.internalBuffer.isEmpty( ) )
			{
				ILogLine lastEntry = this.internalBuffer.get( this.internalBuffer.size( ) - 1 );
				if ( lastEntry.getId( ) >= entry.getId( ) )
				{
					throw new LogLineBufferException( "Unable to add this block, since the ids overlap. idOfLastEntryInBuffer=" + lastEntry.getId( ) + ", idOfFirstEntryInNewBlock=" + entry.getId( ) );
				}
			}// if ( !this.internalBuffer.isEmpty( ) ).

			this.internalBuffer.add( entry );
		}// synchronized ( this.internalBuffer ).

		this.memory += SizeOf.STRING( entry.getData( ) );
	}

	public List<ILogLine> getLines( long start, long end )
	{
		List<ILogLine> lines = new ArrayList<ILogLine>( );

		List<ILogLine> copyOfBuffer = null;
		synchronized ( this.internalBuffer )
		{
			copyOfBuffer = new ArrayList<ILogLine>( this.internalBuffer );
		}

		if ( !copyOfBuffer.isEmpty( ) )
		{
			final long firstId = copyOfBuffer.get( 0 ).getId( );
			final long lastId = copyOfBuffer.get( copyOfBuffer.size( ) - 1 ).getId( );
			final boolean fromStart = ( start == -1 ) || ( firstId >= start );
			final boolean tillEnd = ( end == -1 ) || ( lastId <= end );

			int firstIdx = 0;
			int lastIdx = copyOfBuffer.size( ) - 1;
			if ( !fromStart )
			{
				// 1. obtain index for first 
				// find the first id that matches start or is bigger than start
				// e.g. 7,8,[10,11,12,30,31,32 : where start = 10 ==> take idx=2, which is 10
				// e.g. 7,8,[10,11,12,30,31,32 : where start = 9 ==> take idx=2, which is 10

				for ( int i = 0; i < copyOfBuffer.size( ); ++i )
				{
					// first found that is bigger than or matches start --> use it
					if ( copyOfBuffer.get( i ).getId( ) >= start )
					{
						firstIdx = i;
						break;
					}
				}// for(int i=0;i<copyOfBuffer.size( );++i)

			}// if ( !fromStart )

			if ( !tillEnd )
			{
				// 2. obtain index for last 
				// find the first id that matches end or the last one that is smaller end
				// e.g. 7,8,10,11,12],30,31,32 : where end = 12 ==> take idx=4, which is 12
				// e.g. 7,8,10,11,12],30,31,32 : where end = 28 ==> take idx=4, which is 12

				ListIterator<ILogLine> iter = copyOfBuffer.listIterator( copyOfBuffer.size( ) );
				while ( iter.hasPrevious( ) )
				{
					final ILogLine ll = iter.previous( );
					// last found that is smaller than or matches end --> use it
					if ( ll.getId( ) <= end )
					{
						break;
					}// if ( ll.getId( ) <= end )

					lastIdx--;
				}// while ( iter.hasPrevious( ) )
			}// if ( !tillEnd )

			// now copy the elements from firstIdx to lastIdx
			for ( int i = firstIdx; i < ( lastIdx + 1 ); ++i )
			{
				lines.add( copyOfBuffer.get( i ) );
			}// for(int i=firstIdx;i<(lastIdx+1);++i)

		}// if ( !copyOfBuffer.isEmpty( ) ).
		copyOfBuffer.clear( );
		return lines;
	}

	@Override
	public long getMemory( )
	{
		return memory;
	}

	@Override
	public void freeMemory( )
	{
		this.internalBuffer.clear( );
	}

	@Override
	public String getNameOfMemoryWatchable( )
	{
		return NAME;
	}

	protected void removeEntriesIfMaxCapacityWasReached( )
	{
		if ( this.internalBuffer.isEmpty( ) )
			return;

		List<ILogLine> tmp = new ArrayList<>( );
		synchronized ( this.internalBuffer )
		{
			if ( this.internalBuffer.size( ) >= this.maxCapacity )
			{
				int wantedLoad = ( int ) Math.floor( this.maxCapacity * ( loadFactor ) + 0.5 );
				int linesToRemove = this.internalBuffer.size( ) - wantedLoad;

				tmp = new ArrayList<>( this.internalBuffer.subList( 0, linesToRemove ) );

				this.internalBuffer.subList( 0, linesToRemove ).clear( );
				LOG( ).info( "Removed " + linesToRemove + " lines for LogLineBuffer (currentLoad=" + this.internalBuffer.size( ) + ", loadFactor=" + this.loadFactor + ", maxCapacity=" + this.maxCapacity + ")" );
			}// if ( this.internalBuffer.size( ) >= this.maxCapacity ).
		}// synchronized ( this.internalBuffer ).

		// for memory only
		for ( ILogLine ll : tmp )
			this.memory -= ll.getMemory( );
	}

	private Logger LOG( )
	{
		return this.log;
	}

	/**
	 * Task that removes all {@link ILogLine}s from the buffer that exceed the max capacity.
	 */
	private class BufferOverFlowWatcher extends TimerTask
	{
		private LogLineBuffer	buffer;

		public BufferOverFlowWatcher( LogLineBuffer buffer )
		{
			this.buffer = buffer;
		}

		@Override
		public void run( )
		{
			this.buffer.removeEntriesIfMaxCapacityWasReached( );
		}
	}

	@Override
	public float getLoadFactor( )
	{
		return this.loadFactor;
	}

	@Override
	public int getMaxCapacity( )
	{
		return this.maxCapacity;
	}

	@Override
	public int getCurrentLoad( )
	{
		int currentLoad = 0;
		synchronized ( this.internalBuffer )
		{
			currentLoad = this.internalBuffer.size( );
		}
		return currentLoad;
	}
}
