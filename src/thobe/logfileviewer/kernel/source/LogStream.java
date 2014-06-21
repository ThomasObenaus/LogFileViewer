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
import java.util.regex.PatternSyntaxException;

import thobe.logfileviewer.kernel.source.err.LogStreamException;
import thobe.logfileviewer.kernel.source.listeners.LogStreamContentPublisherListener;
import thobe.logfileviewer.kernel.source.listeners.LogStreamDataListener;
import thobe.logfileviewer.kernel.source.listeners.LogStreamStateListener;
import thobe.logfileviewer.kernel.source.timestamp.LineAndTime;
import thobe.logfileviewer.kernel.source.timestamp.TimeStampExtractor;
import thobe.tools.log.ILoggable;

/**
 * The resource representing the log-file (access to the log-file).
 * @author Thomas Obenaus
 * @source LogStream.java
 * @date May 29, 2014
 */
public class LogStream extends ILoggable implements LogStreamContentPublisherListener, ILogStreamAccess
{
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
	private List<LogStreamStateListener>					logStreamStateListeners;

	/**
	 * Map of listeners that are interested in data of the log-file ({@link LogStream}). Within this map the listeners are ordered by their
	 * line-filter. Map<line-filter,set of listeners>
	 */
	private Map<String, Set<LogStreamDataListener>>			logStreamDataListeners;

	/**
	 * Id of the {@link LogLine}s;
	 */
	private int												logLineId;

	/**
	 * Mapping for a block of {@link LogLine}s to the registered {@link LogStreamDataListener}s having the same filter.
	 */
	private Map<String, LogLineBlockToLogStreamListener>	logLineBlockToLSDLMap;

	private TimeStampExtractor								timeStampExtractor;

	/**
	 * Ctor
	 */
	public LogStream( )
	{
		this.logLineBlockToLSDLMap = new HashMap<>( );
		this.timeStampExtractor = new TimeStampExtractor( );
		this.logStreamStateListeners = new ArrayList<>( );
		this.logStreamDataListeners = new HashMap<>( );
		this.logStreamReader = null;
		this.publishThread = new LogStreamContentPublisher( );
		this.publishThread.start( );
		this.publishThread.addListener( this );
	}

	/**
	 * Add a new {@link LogStreamStateListener}.
	 * @param l
	 */
	public void addLogStreamStateListener( LogStreamStateListener l )
	{
		synchronized ( this.logStreamStateListeners )
		{
			this.logStreamStateListeners.add( l );
		}
	}

	/**
	 * Remove a {@link LogStreamStateListener}.
	 * @param l
	 */
	public void removeLogStreamStateListener( LogStreamStateListener l )
	{
		synchronized ( this.logStreamStateListeners )
		{
			this.logStreamStateListeners.remove( l );
		}
	}

	/**
	 * Add a new {@link LogStreamDataListener}.
	 * @param l
	 */
	public void addLogStreamDataListener( LogStreamDataListener l )
	{
		synchronized ( this.logStreamDataListeners )
		{
			Set<LogStreamDataListener> listeners = this.logStreamDataListeners.get( l.getLineFilter( ) );
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
				entry = new LogLineBlockToLogStreamListener( new ArrayList<LogLine>( ), new HashSet<LogStreamDataListener>( ) );
				this.logLineBlockToLSDLMap.put( l.getLineFilter( ), entry );
			}// if ( entry == null ) .
			entry.value.add( l );
		}// synchronized ( this.logLineBlockToLSDLMap ) .
	}

	/**
	 * Remove a {@link LogStreamDataListener}.
	 * @param l
	 */
	public void removeLogStreamDataListener( LogStreamDataListener l )
	{
		synchronized ( this.logStreamDataListeners )
		{
			Set<LogStreamDataListener> listeners = this.logStreamDataListeners.get( l.getLineFilter( ) );
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

		this.logLineId = 0;
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
		return "thobe.logfileviewer.source.LogStream";
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
			for ( Entry<String, Set<LogStreamDataListener>> entry : this.logStreamDataListeners.entrySet( ) )
			{
				String lineFilter = entry.getKey( );

				try
				{
					// look if the filter matches the line
					if ( newLine != null && newLine.matches( lineFilter ) )
					{
						// only build the line if at least one filter matches
						if ( line == null )
						{
							line = this.buildLogLine( newLine );
						}// if ( line == null ).

						// send the line to all registered listeners
						for ( LogStreamDataListener l : entry.getValue( ) )
						{
							l.onNewLine( line );
						}// for ( LogStreamDataListener l : entry.getValue( ) ).

					}// if ( newLine != null && newLine.matches( lineFilter ) ).
				}// try
				catch ( PatternSyntaxException e )
				{
					LOG( ).warning( "Unable to process line '" + newLine + "' using line-filter '" + lineFilter + "': " + e.getLocalizedMessage( ) );
				}// catch ( PatternSyntaxException e ).
			}// for ( Entry<String, Set<LogStreamDataListener>> entry : this.logStreamDataListeners.entrySet( ) ) .
		}// synchronized ( this.logStreamDataListeners ) .

	}

	private LogLine buildLogLine( String newLine )
	{
		LineAndTime lineAndTime = this.timeStampExtractor.splitLineAndTimeStamp( newLine );
		LogLine logLine = new LogLine( this.logLineId, lineAndTime.getTimeStamp( ), lineAndTime.getLineWithoutTimeStamp( ) );
		this.logLineId++;
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
			for ( LogStreamStateListener l : this.logStreamStateListeners )
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
			for ( LogStreamStateListener l : this.logStreamStateListeners )
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
			for ( LogStreamStateListener l : this.logStreamStateListeners )
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
		// for each line of the block
		for ( String newLine : newBlock )
		{
			LogLine logLine = null;
			synchronized ( this.logLineBlockToLSDLMap )
			{
				for ( Entry<String, LogLineBlockToLogStreamListener> entry : this.logLineBlockToLSDLMap.entrySet( ) )
				{
					String lineFilter = entry.getKey( );

					try
					{
						// look if the filter matches the line
						if ( newLine != null && newLine.matches( lineFilter ) )
						{
							// only build the line if at least one filter matches
							if ( logLine == null )
							{
								logLine = this.buildLogLine( newLine );
							}// if ( line == null ).

							// add the logline
							entry.getValue( ).key.add( logLine );
						}// if ( newLine != null && newLine.matches( lineFilter ) ).

					}// try
					catch ( PatternSyntaxException e )
					{
						LOG( ).warning( "Unable to process line '" + newLine + "' using line-filter '" + lineFilter + "': " + e.getLocalizedMessage( ) );
					}// catch ( PatternSyntaxException e ).
				}// for ( Entry<String, LogLineBlockToLogStreamListener> entry : logLineBlockToLSDLMap.entrySet( ) ) .
			}// synchronized ( this.logLineBlockToLSDLMap ) .
		}// for ( String newLine : newBlock ) .

		// now fire the blocks to the listeners
		synchronized ( this.logLineBlockToLSDLMap )
		{
			for ( Entry<String, LogLineBlockToLogStreamListener> entry : this.logLineBlockToLSDLMap.entrySet( ) )
			{
				List<LogLine> logLines = entry.getValue( ).getKey( );
				Set<LogStreamDataListener> listeners = entry.getValue( ).getValue( );

				for ( LogStreamDataListener listener : listeners )
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

	final class LogLineBlockToLogStreamListener implements Map.Entry<List<LogLine>, Set<LogStreamDataListener>>
	{
		private final List<LogLine>			key;
		private Set<LogStreamDataListener>	value;

		public LogLineBlockToLogStreamListener( List<LogLine> key, Set<LogStreamDataListener> value )
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
		public Set<LogStreamDataListener> getValue( )
		{
			return value;
		}

		@Override
		public Set<LogStreamDataListener> setValue( Set<LogStreamDataListener> value )
		{
			Set<LogStreamDataListener> old = this.value;
			this.value = value;
			return old;
		}
	}
}
