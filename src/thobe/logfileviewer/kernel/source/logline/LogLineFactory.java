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

import java.util.concurrent.atomic.AtomicLong;

import thobe.logfileviewer.kernel.memory.LRUCache;
import thobe.logfileviewer.kernel.source.timestamp.LineAndTime;
import thobe.logfileviewer.kernel.source.timestamp.TimeStampExtractor;

/**
 * This class is responsible for creation of {@link ILogLine}s from given Strings.
 * @author Thomas Obenaus
 * @source LogLineFactory.java
 * @date 11.09.2014
 */
public class LogLineFactory implements ILogLineFactoryAccess
{
	/**
	 * Id of the {@link LogLine}s;
	 */
	private long							logLineId;

	/**
	 * Cache for the data of LogLines
	 */
	private LRUCache<String, LogLineDat>	cache;

	/**
	 * Class responsible for extracting the time-stamps from a given LogLine
	 */
	private TimeStampExtractor				timeStampExtractor;

	/**
	 * Cache-Statistics: hits
	 */
	private long							hits	= 0;

	/**
	 * Cache-Statistics: misses
	 */
	private long							misses	= 0;

	/**
	 * Memory consumed by the internal cache
	 */
	private AtomicLong						memory;

	/**
	 * Ctor
	 * @param cacheSize - The size of the cache that should be used. The size value determines how much entries the cache could contain a
	 *            memory-limit is not supported.
	 */
	public LogLineFactory( int cacheSize )
	{
		this.cache = new LRUCache<>( cacheSize );
		this.timeStampExtractor = new TimeStampExtractor( );
		this.logLineId = 0;
		this.misses = 0;
		this.hits = 0;
		this.memory = new AtomicLong( 0 );
	}

	/**
	 * Current size (number of entries) of the cache.
	 * @return
	 */
	public int getCacheSize( )
	{
		return this.cache.size( );
	}

	/**
	 * Returns Cache-Statistics: hits
	 * @return
	 */
	public long getCacheHits( )
	{
		return hits;
	}

	/**
	 * Returns Cache-Statistics: misses
	 * @return
	 */
	public long getCacheMisses( )
	{
		return misses;
	}

	/**
	 * Returns Cache-Statistics: cache-ratio
	 * @return
	 */
	public float getCacheRatio( )
	{
		long overall = hits + misses;
		if ( overall == 0 )
			return 0;
		return ( hits / ( float ) ( overall ) );
	}

	/**
	 * Creates a new {@link ILogLine} from the given String. The contained timestamp will be extracted.
	 * @param newLine
	 * @return
	 */
	public ILogLine buildLogLine( String newLine )
	{
		LineAndTime lineAndTime = this.timeStampExtractor.splitLineAndTimeStamp( newLine );

		String data = lineAndTime.getLineWithoutTimeStamp( );
		long timeStamp = lineAndTime.getTimeStamp( );

		// ask the cache if the data is already there
		LogLineDat logLineDat = this.cache.get( lineAndTime.getLineWithoutTimeStamp( ) );
		if ( logLineDat == null )
		{
			logLineDat = new LogLineDat( data );
			LogLineDat oldLogLineDat = this.cache.put( data, logLineDat );

			this.memory.addAndGet( logLineDat.getMemory( ) );
			if ( oldLogLineDat != null )
			{
				this.memory.addAndGet( oldLogLineDat.getMemory( ) * ( -1 ) );
			}

			misses++;
		}// if ( logLineDat == null ).
		else
		{
			hits++;
		}// if ( logLineDat == null ) ... else ...

		// create the logline
		LogLine logLine = new LogLine( this.logLineId, timeStamp, logLineDat );
		this.logLineId++;

		return logLine;
	}

	/**
	 * Returns the memory consumed by the cache in bytes.
	 * @return
	 */
	public long getCacheMemory( )
	{
		return this.memory.get( );
	}

	/**
	 * Frees the memory used in internal cache.
	 */
	public void clearCache( )
	{
		this.cache.clear( );
	}

	@Override
	public int getMaxCacheSize( )
	{
		return this.cache.getMaxCacheSize( );
	}

}
