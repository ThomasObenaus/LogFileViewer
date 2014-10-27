/*
 *  Copyright (C) 2014, Thomas Obenaus. All rights reserved.
 *  Licensed under the New BSD License (3-clause lic)
 *  See attached license-file.
 *  
 *	Author: 	Thomas Obenaus
 *	EMail:		obenaus.thomas@gmail.com
 *  Project:    EthTrace
 */

package thobe.logfileviewer.kernel.source;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import thobe.logfileviewer.kernel.memory.IMemoryWatchable;
import thobe.logfileviewer.kernel.source.err.LogLineBufferException;
import thobe.logfileviewer.kernel.source.err.LogStreamException;
import thobe.logfileviewer.kernel.source.listeners.ILogStreamContentPublisherListener;
import thobe.logfileviewer.kernel.source.listeners.ILogStreamDataListener;
import thobe.logfileviewer.kernel.source.listeners.ILogStreamStateListener;
import thobe.logfileviewer.kernel.source.logline.ILogLine;
import thobe.logfileviewer.kernel.source.logline.ILogLineBuffer;
import thobe.logfileviewer.kernel.source.logline.ILogLineFactoryAccess;
import thobe.logfileviewer.kernel.source.logline.LogLine;
import thobe.logfileviewer.kernel.source.logline.LogLineBuffer;
import thobe.logfileviewer.kernel.source.logline.LogLineFactory;
import thobe.logfileviewer.kernel.source.reader.LogStreamReader;
import thobe.tools.log.ILoggable;

/**
 * The resource representing the log-file (access to the log-file).
 * @author Thomas Obenaus
 * @source LogStream.java
 * @date May 29, 2014
 */
public class LogStream extends ILoggable implements ILogStreamContentPublisherListener, ILogStreamAccess, IMemoryWatchable
{
	private static final String								NAME				= "thobe.logfileviewer.source.LogStream";

	private static final int								LOG_LINE_CACHE_SIZE	= 100000;
	/**
	 * {@link Thread} that reads the log-file asynchronously.
	 */
	private LogStreamReader									logStreamReader;

	/**
	 * {@link Thread} that publishes events fired by the {@link LogStreamReader} (e.g. new line, opened, closed,...)
	 */
	private LogStreamContentPublisher						publishThread;

	/**
	 * List of listeners that are interested in state-changes of the log-file ({@link LogStream}) e.g. open, closed, eofReached.
	 */
	private List<ILogStreamStateListener>					logStreamStateListeners;

	/**
	 * Map of listeners that are interested in data of the log-file ({@link LogStream}). Within this map the listeners are ordered by their
	 * line-filter. Map<line-filter,set of listeners>
	 */
	private Map<Pattern, Set<ILogStreamDataListener>>		logStreamDataListeners;

	/**
	 * Mapping for a block of {@link LogLine}s to the registered {@link ILogStreamDataListener}s having the same filter.
	 */
	private Map<Pattern, LogLineBlockToLogStreamListener>	logLineBlockToLSDLMap;

	/**
	 * Factory responsible for the creation (and caching) of loglines.
	 */
	private LogLineFactory									logLineFactory;

	/**
	 * Buffer for {@link ILogLine}s.
	 */
	private LogLineBuffer									logLineBuffer;

	/**
	 * Ctor
	 */
	public LogStream( )
	{
		this.logLineBlockToLSDLMap = new HashMap<>( );
		this.logStreamStateListeners = new ArrayList<>( );
		this.logStreamDataListeners = new HashMap<>( );
		this.logStreamReader = null;
		this.publishThread = new LogStreamContentPublisher( );
		this.publishThread.start( );
		this.publishThread.addListener( this );
		this.logLineFactory = new LogLineFactory( LOG_LINE_CACHE_SIZE );
		this.logLineBuffer = new LogLineBuffer( );
	}

	/**
	 * Add a new {@link ILogStreamStateListener}.
	 * @param l
	 */
	public void addLogStreamStateListener( ILogStreamStateListener l )
	{
		synchronized ( this.logStreamStateListeners )
		{
			this.logStreamStateListeners.add( l );
		}
	}

	/**
	 * Remove a {@link ILogStreamStateListener}.
	 * @param l
	 */
	public void removeLogStreamStateListener( ILogStreamStateListener l )
	{
		synchronized ( this.logStreamStateListeners )
		{
			this.logStreamStateListeners.remove( l );
		}
	}

	/**
	 * Add a new {@link ILogStreamDataListener}.
	 * @param l
	 */
	public void addLogStreamDataListener( ILogStreamDataListener l )
	{
		synchronized ( this.logStreamDataListeners )
		{
			Set<ILogStreamDataListener> listeners = this.logStreamDataListeners.get( l.getLineFilter( ) );
			if ( listeners == null )
			{
				// create an empty set if no listeners with the given filter are available
				listeners = new HashSet<>( );
				this.logStreamDataListeners.put( l.getLineFilter( ), listeners );
			}
			listeners.add( l );
		}

		// add the entry to the mapping LogLine<->LogStreamStateListener too
		synchronized ( this.logLineBlockToLSDLMap )
		{
			LogLineBlockToLogStreamListener entry = this.logLineBlockToLSDLMap.get( l.getLineFilter( ) );
			if ( entry == null )
			{
				entry = new LogLineBlockToLogStreamListener( new ArrayList<LogLine>( ), new HashSet<ILogStreamDataListener>( ) );
				this.logLineBlockToLSDLMap.put( l.getLineFilter( ), entry );
			}// if ( entry == null ) .
			entry.value.add( l );
		}// synchronized ( this.logLineBlockToLSDLMap ) .
	}

	/**
	 * Remove a {@link ILogStreamDataListener}.
	 * @param l
	 */
	public void removeLogStreamDataListener( ILogStreamDataListener l )
	{
		synchronized ( this.logStreamDataListeners )
		{
			Set<ILogStreamDataListener> listeners = this.logStreamDataListeners.get( l.getLineFilter( ) );
			if ( listeners != null )
			{
				listeners.remove( l );
			}
		}

		// remove the entry to the mapping LogLine<->LogStreamStateListener too
		synchronized ( this.logLineBlockToLSDLMap )
		{
			LogLineBlockToLogStreamListener entry = this.logLineBlockToLSDLMap.get( l.getLineFilter( ) );
			if ( entry != null )
			{
				// remove the listener
				entry.getValue( ).remove( l );

				// remove the complete entry if no more listeners are attached
				if ( entry.getValue( ).isEmpty( ) )
					this.logLineBlockToLSDLMap.remove( l.getLineFilter( ) );
			}// if ( entry != null ) .
		}// synchronized ( this.logLineBlockToLSDLMap ) .
	}

	/**
	 * Open/ start reading from a log-file represented by the given {@link LogStreamReader}.
	 * @param source
	 * @throws LogStreamException
	 */
	public void open( LogStreamReader source ) throws LogStreamException
	{
		if ( this.logStreamReader != null && this.logStreamReader.isOpen( ) )
		{
			this.close( );
		}

		this.logStreamReader = source;
		this.logStreamReader.open( );
		this.logStreamReader.start( );

		this.publishThread.startPublishing( logStreamReader );

		LOG( ).info( "LogStream opened [" + this.logStreamReader.getClass( ).getSimpleName( ) + "]" );
	}

	/**
	 * Returns true if the {@link LogStream} is open, false otherwise.
	 * @return
	 */
	public boolean isOpen( )
	{
		if ( this.logStreamReader == null )
			return false;
		if ( !this.logStreamReader.isOpen( ) )
			return false;
		return true;
	}

	/**
	 * Closes this {@link LogStream}.
	 * @throws LogStreamException
	 */
	public void close( ) throws LogStreamException
	{
		if ( this.logStreamReader != null )
		{
			this.logStreamReader.close( );
			this.publishThread.stopPublishing( );
			LOG( ).info( "LogStream closed [" + this.logStreamReader.getClass( ).getSimpleName( ) + "]" );
			this.logStreamReader = null;
		}
	}

	@Override
	protected String getLogChannelName( )
	{
		return NAME;
	}

	@Override
	public void onNewLine( String newLine )
	{
		final boolean logFine = LOG( ).isLoggable( Level.FINE );

		if ( logFine )
		{
			LOG( ).fine( "New line '" + newLine + "' will be send to listeners." );
		}

		// build the log-line
		LogLine line = null;

		synchronized ( this.logStreamDataListeners )
		{
			for ( Entry<Pattern, Set<ILogStreamDataListener>> entry : this.logStreamDataListeners.entrySet( ) )
			{
				Pattern linePattern = entry.getKey( );

				try
				{
					// look if the filter matches the line
					if ( newLine != null && matches( linePattern, newLine ) )
					{
						// only build the line if at least one filter matches
						if ( line == null )
						{
							line = this.buildLogLine( newLine );

							// add the line to the buffer
							try
							{
								this.logLineBuffer.add( line );
							}
							catch ( LogLineBufferException e )
							{
								LOG( ).severe( "Error adding LogLine to LogStream.buffer: " + e.getLocalizedMessage( ) );
							}
						}// if ( line == null ).

						// send the line to all registered listeners
						for ( ILogStreamDataListener l : entry.getValue( ) )
						{
							l.onNewLine( line );
						}// for ( LogStreamDataListener l : entry.getValue( ) ).

					}// if ( newLine != null && newLine.matches( lineFilter ) ).
				}// try
				catch ( PatternSyntaxException e )
				{
					LOG( ).warning( "Unable to process line '" + newLine + "' using line-filter '" + linePattern.pattern( ) + "': " + e.getLocalizedMessage( ) );
				}// catch ( PatternSyntaxException e ).
			}// for ( Entry<String, Set<LogStreamDataListener>> entry : this.logStreamDataListeners.entrySet( ) ) .
		}// synchronized ( this.logStreamDataListeners ) .

	}

	private static boolean matches( final Pattern pattern, final String line )
	{
		if ( pattern == null || pattern.pattern( ).trim( ).isEmpty( ) )
			return false;

		boolean result = false;

		try
		{
			Matcher m = pattern.matcher( line );
			result = m.matches( );
		}
		catch ( PatternSyntaxException e )
		{}

		return result;
	}

	private LogLine buildLogLine( String newLine )
	{
		LogLine logLine = ( LogLine ) this.logLineFactory.buildLogLine( newLine );
		return logLine;
	}

	@Override
	public void onEOFReached( )
	{
		this.fireOnEOFReached( );
	}

	@Override
	public void onOpened( )
	{
		this.fireOnOpened( );
	}

	@Override
	public void onClosed( )
	{
		this.fireOnClosed( );
	}

	@Override
	public String getLogStreamListenerName( )
	{
		return this.getLogChannelName( );
	}

	private void fireOnClosed( )
	{
		synchronized ( this.logStreamStateListeners )
		{
			for ( ILogStreamStateListener l : this.logStreamStateListeners )
			{
				long elapsedTime = System.currentTimeMillis( );
				l.onClosed( );
				elapsedTime = System.currentTimeMillis( ) - elapsedTime;

				if ( elapsedTime > 100 )
				{
					LOG( ).warning( "Listener '" + l.getLogStreamListenerName( ) + "' needs " + ( elapsedTime / 1000.0f ) + "s to process the closed event." );
				}// if ( elapsedTime > 100 ).
			}// for ( LogStreamStateListener l : this.logStreamStateListeners ) .
		}// synchronized ( this.logStreamStateListeners ) .
	}

	private void fireOnOpened( )
	{
		synchronized ( this.logStreamStateListeners )
		{
			for ( ILogStreamStateListener l : this.logStreamStateListeners )
			{
				long elapsedTime = System.currentTimeMillis( );
				l.onOpened( );
				elapsedTime = System.currentTimeMillis( ) - elapsedTime;

				if ( elapsedTime > 100 )
				{
					LOG( ).warning( "Listener '" + l.getLogStreamListenerName( ) + "' needs " + ( elapsedTime / 1000.0f ) + "s to process the opened event." );
				}// if ( elapsedTime > 100 ).
			}// for ( LogStreamStateListener l : this.logStreamStateListeners ) .
		}// synchronized ( this.logStreamStateListeners ) .
	}

	private void fireOnEOFReached( )
	{
		synchronized ( this.logStreamStateListeners )
		{
			for ( ILogStreamStateListener l : this.logStreamStateListeners )
			{
				long elapsedTime = System.currentTimeMillis( );
				l.onEOFReached( );
				elapsedTime = System.currentTimeMillis( ) - elapsedTime;

				if ( elapsedTime > 100 )
				{
					LOG( ).warning( "Listener '" + l.getLogStreamListenerName( ) + "' needs " + ( elapsedTime / 1000.0f ) + "s to process the eof event." );
				}// if ( elapsedTime > 100 ).
			}// for ( LogStreamStateListener l : this.logStreamStateListeners ) .
		}// synchronized ( this.logStreamStateListeners ) .
	}

	@Override
	public void onNewBlock( List<String> newBlock )
	{
		List<ILogLine> newBlockForBuffer = new ArrayList<>( );
		// for each line of the block
		for ( String newLine : newBlock )
		{
			LogLine logLine = null;
			synchronized ( this.logLineBlockToLSDLMap )
			{
				for ( Entry<Pattern, LogLineBlockToLogStreamListener> entry : this.logLineBlockToLSDLMap.entrySet( ) )
				{
					Pattern linePattern = entry.getKey( );

					try
					{
						// look if the filter matches the line
						if ( newLine != null && matches( linePattern, newLine ) )
						{
							// only build the line if at least one filter matches
							if ( logLine == null )
							{
								logLine = this.buildLogLine( newLine );
								newBlockForBuffer.add( logLine );
							}// if ( line == null ).

							// add the logline
							entry.getValue( ).key.add( logLine );
						}// if ( newLine != null && newLine.matches( lineFilter ) ).

					}// try
					catch ( PatternSyntaxException e )
					{
						LOG( ).warning( "Unable to process line '" + newLine + "' using line-filter '" + linePattern.pattern( ) + "': " + e.getLocalizedMessage( ) );
					}// catch ( PatternSyntaxException e ).
				}// for ( Entry<String, LogLineBlockToLogStreamListener> entry : logLineBlockToLSDLMap.entrySet( ) ) .
			}// synchronized ( this.logLineBlockToLSDLMap ) .
		}// for ( String newLine : newBlock ) .

		// add the lines to the buffer
		try
		{
			this.logLineBuffer.addSorted( newBlockForBuffer );
		}
		catch ( LogLineBufferException e )
		{
			LOG( ).severe( "Error adding block of LogLines to LogStream.buffer: " + e.getLocalizedMessage( ) );
		}

		// now fire the blocks to the listeners
		synchronized ( this.logLineBlockToLSDLMap )
		{
			for ( Entry<Pattern, LogLineBlockToLogStreamListener> entry : this.logLineBlockToLSDLMap.entrySet( ) )
			{
				List<LogLine> logLines = entry.getValue( ).getKey( );
				Set<ILogStreamDataListener> listeners = entry.getValue( ).getValue( );

				for ( ILogStreamDataListener listener : listeners )
				{
					listener.onNewBlockOfLines( logLines );
				}// for(LogStreamDataListener listener : listeners).

				// clear the block of loglines
				entry.getValue( ).getKey( ).clear( );
			}// for ( Entry<String, LogLineBlockToLogStreamListener> entry : this.logLineBlockToLSDLMap.entrySet( ) ).
		}// synchronized ( this.logLineBlockToLSDLMap   ).
	}

	/**
	 * Returns the lines per second read by the reader ({@link LogStreamReader}).
	 * @return
	 */
	public double getLogStreamReaderLPS( )
	{
		if ( this.logStreamReader == null )
			return 0;
		return this.logStreamReader.getLinesPerSecond( );
	}

	public ILogLineFactoryAccess getLogLineFactory( )
	{
		return this.logLineFactory;
	}

	public ILogLineBuffer getLogLineBuffer( )
	{
		return this.logLineBuffer;
	}

	final class LogLineBlockToLogStreamListener implements Map.Entry<List<LogLine>, Set<ILogStreamDataListener>>
	{
		private final List<LogLine>			key;
		private Set<ILogStreamDataListener>	value;

		public LogLineBlockToLogStreamListener( List<LogLine> key, Set<ILogStreamDataListener> value )
		{
			this.key = key;
			this.value = value;
		}

		@Override
		public List<LogLine> getKey( )
		{
			return key;
		}

		@Override
		public Set<ILogStreamDataListener> getValue( )
		{
			return value;
		}

		@Override
		public Set<ILogStreamDataListener> setValue( Set<ILogStreamDataListener> value )
		{
			Set<ILogStreamDataListener> old = this.value;
			this.value = value;
			return old;
		}
	}

	@Override
	public long getMemory( )
	{
		return this.logLineFactory.getCacheMemory( ) + this.logLineBuffer.getMemory( );
	}

	@Override
	public void freeMemory( )
	{
		this.logLineFactory.clearCache( );
		this.logLineBuffer.freeMemory( );
	}

	@Override
	public String getNameOfMemoryWatchable( )
	{
		return NAME;
	}
}
