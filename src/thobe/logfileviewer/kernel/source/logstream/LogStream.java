/*
 *  Copyright (C) 2014, Thomas Obenaus. All rights reserved.
 *  Licensed under the New BSD License (3-clause lic)
 *  See attached license-file.
 *  
 *	Author: 	Thomas Obenaus
 *	EMail:		obenaus.thomas@gmail.com
 *  Project:    EthTrace
 */

package thobe.logfileviewer.kernel.source.logstream;

import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import thobe.logfileviewer.kernel.memory.IMemoryWatchable;
import thobe.logfileviewer.kernel.source.err.LogLineBufferException;
import thobe.logfileviewer.kernel.source.err.LogStreamException;
import thobe.logfileviewer.kernel.source.extreader.ExternalLogStreamReader;
import thobe.logfileviewer.kernel.source.logline.ILogLineBuffer;
import thobe.logfileviewer.kernel.source.logline.ILogLineFactoryAccess;
import thobe.logfileviewer.kernel.source.logline.LogLine;
import thobe.logfileviewer.kernel.source.logline.LogLineBuffer;
import thobe.logfileviewer.kernel.source.logline.LogLineFactory;
import thobe.logfileviewer.plugin.source.logline.ILogLine;
import thobe.logfileviewer.plugin.source.logstream.IInternalLogStreamReaderListener;
import thobe.logfileviewer.plugin.source.logstream.ILogStreamAccess;
import thobe.logfileviewer.plugin.source.logstream.ILogStreamDataListener;
import thobe.logfileviewer.plugin.source.logstream.ILogStreamRequester;
import thobe.logfileviewer.plugin.source.logstream.ILogStreamStateListener;

/**
 * The resource representing the log-file (access to the log-file). The contents of the logfile can be obtained through the
 * {@link ILogStreamDataListener}. The current states of the logfile (open, eof, closed, ..) can be obtained through the
 * {@link ILogStreamStateListener}.
 * @author Thomas Obenaus
 * @source LogStream.java
 * @date May 29, 2014
 */
public class LogStream extends Thread implements IInternalLogStreamReaderListener, ILogStreamAccess, IMemoryWatchable
{
	private static final String								NAME				= "thobe.logfileviewer.source.LogStream";

	/**
	 * Default size of the cache
	 */
	private static final int								LOG_LINE_CACHE_SIZE	= 100000;

	/**
	 * {@link Thread} that reads the log-file asynchronously.
	 */
	private ExternalLogStreamReader							logStreamReader;

	/**
	 * {@link Thread} that publishes events fired by the {@link ExternalLogStreamReader} (e.g. new line, opened, closed,...)
	 */
	private InternalLogStreamReader							publishThread;

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

	private int												nextRequestId;

	/**
	 * Queue of requests.
	 */
	private Deque<Request>									requestQueue;

	/**
	 * Semaphore for the internal event-main-loop
	 */
	private Semaphore										eventSemaphore;

	/**
	 * The logger
	 */
	private Logger											log;

	/**
	 * Flag indicating if quitting this Thread is requested externally.
	 */
	private AtomicBoolean									quitRequested;

	/**
	 * Ctor
	 */
	public LogStream( )
	{
		super( NAME );
		this.logLineBlockToLSDLMap = new HashMap<>( );
		this.logStreamStateListeners = new ArrayList<>( );
		this.logStreamDataListeners = new HashMap<>( );
		this.logStreamReader = null;
		this.publishThread = new InternalLogStreamReader( );
		this.publishThread.start( );
		this.publishThread.addListener( this );
		this.logLineFactory = new LogLineFactory( LOG_LINE_CACHE_SIZE );
		this.logLineBuffer = new LogLineBuffer( );
		this.nextRequestId = 0;
		this.requestQueue = new ConcurrentLinkedDeque<LogStream.Request>( );
		this.log = Logger.getLogger( NAME );
		this.quitRequested = new AtomicBoolean( false );
		this.eventSemaphore = new Semaphore( 0 );
	}

	@Override
	public void run( )
	{
		LOG( ).info( "Thread " + this.getName( ) + " started" );
		try
		{
			while ( !this.quitRequested.get( ) )
			{
				processRequests( );

				this.eventSemaphore.acquire( );

			}
		}
		catch ( InterruptedException e )
		{
			LOG( ).severe( "Thread interruped ... stopping." );
		}

		LOG( ).info( "Thread " + this.getName( ) + " stopped" );
	}

	private void processRequests( )
	{
		Request req = null;
		synchronized ( this.requestQueue )
		{
			while ( ( req = this.requestQueue.poll( ) ) != null )
			{
				LOG( ).info( "Processing request: " + req );
				ILogStreamRequester requester = req.getRequester( );
				if ( requester != null )
				{
					List<ILogLine> logLines = this.logLineBuffer.getLines( req.getStart( ), req.getEnd( ) );
					if ( req.getFilter( ) == null )
					{
						requester.response( req.getId( ), logLines, true );
					}
					else
					{
						Pattern pat = req.getFilter( );
						List<ILogLine> filteredLines = new ArrayList<ILogLine>( );
						for ( ILogLine l : logLines )
						{
							if ( matches( pat, l.getData( ) ) )
							{
								filteredLines.add( l );
							}
						}
						requester.response( req.getId( ), filteredLines, true );
					}
				}// if ( requester != null )
			}// while ( ( req = this.requestQueue.poll( ) ) != null ) .
		}// synchronized ( this.requestQueue ) .

	}

	public void quit( )
	{
		this.quitRequested.set( true );
		this.eventSemaphore.release( );
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
				entry = new LogLineBlockToLogStreamListener( new ArrayList<ILogLine>( ), new HashSet<ILogStreamDataListener>( ) );
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
	 * Open/ start reading from a log-file represented by the given {@link ExternalLogStreamReader}.
	 * @param source
	 * @throws LogStreamException
	 */
	public void open( ExternalLogStreamReader source ) throws LogStreamException
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
		return NAME;
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
		long elapsed = System.currentTimeMillis( );

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
			this.logLineBuffer.add( newBlockForBuffer );
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
				List<ILogLine> logLines = entry.getValue( ).getKey( );
				Set<ILogStreamDataListener> listeners = entry.getValue( ).getValue( );

				for ( ILogStreamDataListener listener : listeners )
				{
					listener.onNewBlockOfLines( logLines );
				}// for(LogStreamDataListener listener : listeners).

				// clear the block of loglines
				entry.getValue( ).getKey( ).clear( );
			}// for ( Entry<String, LogLineBlockToLogStreamListener> entry : this.logLineBlockToLSDLMap.entrySet( ) ).
		}// synchronized ( this.logLineBlockToLSDLMap   ).

		elapsed = System.currentTimeMillis( ) - elapsed;
		//		System.out.println(elapsed / 1000f + " s");
	}

	/**
	 * Returns the lines per second read by the reader ({@link ExternalLogStreamReader}).
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

	final class LogLineBlockToLogStreamListener implements Map.Entry<List<ILogLine>, Set<ILogStreamDataListener>>
	{
		private final List<ILogLine>			key;
		private Set<ILogStreamDataListener>	value;

		public LogLineBlockToLogStreamListener( List<ILogLine> key, Set<ILogStreamDataListener> value )
		{
			this.key = key;
			this.value = value;
		}

		@Override
		public List<ILogLine> getKey( )
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

	protected Logger LOG( )
	{
		return this.log;
	}

	@Override
	public int requestLogLines( long start, long end, ILogStreamRequester requester, Pattern filter )
	{
		int newId = -1;

		if ( requester != null )
		{
			synchronized ( this.requestQueue )
			{
				newId = this.nextRequestId;
				this.nextRequestId++;

				// build and put the new request to the queue
				this.requestQueue.push( new Request( newId, start, end, requester, filter ) );
			}// synchronized ( this.requestQueue )

			this.eventSemaphore.release( );
		}// if ( requester != null ).

		return newId;
	}

	@Override
	public int requestLogLines( long start, long end, ILogStreamRequester requester )
	{
		return requestLogLines( start, end, requester, null );
	}

	private class Request
	{
		private int					id;
		private long				start;
		private long				end;
		private ILogStreamRequester	requester;
		private Pattern				filter;

		public Request( int id, long start, long end, ILogStreamRequester requester, Pattern filter )
		{
			this.id = id;
			this.start = start;
			this.end = end;
			this.requester = requester;
			this.filter = filter;
		}

		public long getEnd( )
		{
			return end;
		}

		public int getId( )
		{
			return id;
		}

		public ILogStreamRequester getRequester( )
		{
			return requester;
		}

		public long getStart( )
		{
			return start;
		}

		public Pattern getFilter( )
		{
			return filter;
		}

		@Override
		public String toString( )
		{
			return "[" + this.id + "] filter=" + ( ( this.filter != null ) ? this.filter.toString( ) : "n/a" ) + ", range=(" + this.start + "," + this.end + "), req=" + this.requester.getLSRequesterName( );
		}

	}
}
