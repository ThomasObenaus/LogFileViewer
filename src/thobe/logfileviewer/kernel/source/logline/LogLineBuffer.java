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
	private static final String	NAME	= "thobe.logfileviewer.kernel.source.logline.LogLineBuffer";
	private List<ILogLine>		internalBuffer;
	private int					maxCapacity;
	private float				loadFactor;
	private Logger				log;
	private Timer				bufferOverflowWatcherTimer;
	private long				memory;

	/**
	 * DefCtor with maxCapacity=100000 and loadFactor=0.75
	 */
	public LogLineBuffer( )
	{
		this( 1000000, 0.75f );
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
	public void addSorted( List<ILogLine> entries ) throws LogLineBufferException
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
