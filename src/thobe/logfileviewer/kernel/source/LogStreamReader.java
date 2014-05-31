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

import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

/**
 * Abstract class representing a source that reads and offers contents of a log (file or over ip).
 * @author Thomas Obenaus
 * @source LogStreamReader.java
 * @date May 15, 2014
 */
public abstract class LogStreamReader extends Thread
{
	/**
	 * deque containing the contents of the log line by line.
	 */
	private Deque<String>	lineBuffer;

	/**
	 * true if the termination/ quit of reading the source/log, was requested.
	 */
	private AtomicBoolean	quitRequested;

	/**
	 * true if the corresponding source is open --> reading from this source was started.
	 */
	private AtomicBoolean	opened;

	/**
	 * The time to sleep between two lines that where read from the source (in ms).
	 */
	private AtomicInteger	sleepTime;

	/**
	 * True if the end of file was reached.
	 */
	private AtomicBoolean	EOFReached;

	/**
	 * The internal logger
	 */
	private Logger			log;

	/**
	 * Ctor
	 */
	public LogStreamReader( )
	{
		this.lineBuffer = new ConcurrentLinkedDeque<>( );
		this.quitRequested = new AtomicBoolean( false );
		this.opened = new AtomicBoolean( false );
		this.EOFReached = new AtomicBoolean( false );
		this.sleepTime = new AtomicInteger( 100 );
		this.log = Logger.getLogger( "thobe.logfileviewer.source.LogStreamReader" );
	}

	/**
	 * Returns the next line that is available (was read from the log source). On accessing this method the line will be consumed, that
	 * means further calls to this method will result in different results (the next line).
	 * @return - the next line as {@link String}
	 * @throws TraceSourceException - Thrown if no more lines are available. Please use {@link LogStreamReader#hasNextLine()} to check if more
	 *             lines are available.
	 */
	public String nextLine( ) throws TraceSourceException
	{
		if ( !this.hasNextLine( ) )
			throw new TraceSourceException( "The queue is empty" );
		return this.lineBuffer.pop( );
	}

	/**
	 * Returns true if at least one more line is available, false otherwise.
	 * @return
	 */
	public boolean hasNextLine( )
	{
		return !this.lineBuffer.isEmpty( );
	}

	@Override
	public void run( )
	{
		LOG( ).info( "Thread: " + this.getClass( ).getSimpleName( ) + " started." );

		// Main-loop, will only terminate if close was called
		while ( !this.quitRequested.get( ) && !this.EOFReached.get( ) )
		{
			// directly interrupt reading from the source if the source is not open
			if ( !isOpen( ) )
			{
				LOG( ).info( this.getClass( ).getSimpleName( ) + " interrupted: Source is not open." );
				break;
			}// if ( !isOpen( ) ) .

			try
			{
				// delegate the reading to the specific source-implementation
				String newLine = readLineImpl( );

				if ( newLine != null )
				{
					// add the line to the buffer
					this.lineBuffer.add( newLine );
				}// if ( newLine != null ) . 
				else
				{
					// end of file reached
					this.EOFReached.set( true );
					LOG( ).info( "End of file reached" );
				}// if ( newLine != null ) ... else ...
			}
			catch ( TraceSourceException e1 )
			{
				LOG( ).warning( this.getClass( ).getSimpleName( ) + " error reading next line: " + e1.getLocalizedMessage( ) );
			}// catch ( TraceSourceException e1 ) .

			// sleep only if we don't have already reached the EOF
			if ( !this.EOFReached.get( ) )
			{
				try
				{
					Thread.sleep( this.sleepTime.longValue( ) );
				}
				catch ( InterruptedException e )
				{
					LOG( ).info( "" + this.getClass( ).getSimpleName( ) + " interrupted: " + e.getLocalizedMessage( ) );
					break;
				}// catch ( InterruptedException e ) .
			}// if ( !bEOFReached ) .

		}// while ( !this.quitRequested.get( ) ) .

		LOG( ).info( "Thread: " + this.getClass( ).getSimpleName( ) + " stopped." );
	}

	/**
	 * Sets the time to sleep between two lines that where read from the source (in ms).
	 * @param sleepTime
	 */
	public void setSleepTime( int sleepTime )
	{
		this.sleepTime.set( sleepTime );
	}

	public boolean isEOFReached( )
	{
		return EOFReached.get( );
	}

	public void close( ) throws TraceSourceException
	{
		// set request to terminate the main-loop
		this.quitRequested.set( true );

		if ( this.opened.get( ) )
		{
			try
			{
				// call the source-specific close-method
				this.closeImpl( );
				this.opened.set( false );
			}
			catch ( TraceSourceException e )
			{
				LOG( ).severe( "Failed to close: " + e.getLocalizedMessage( ) );
				throw e;
			}
		}// if ( this.opened.get( ) ).
		LOG( ).info( "TraceSource [" + this.getClass( ).getSimpleName( ) + "] closed." );
	}

	/**
	 * Implement this method in the specific trace-source. This method will called each time a new line should be read from the source.
	 * @return - the line that was read from the source
	 * @throws TraceSourceException
	 */
	protected abstract String readLineImpl( ) throws TraceSourceException;

	/**
	 * Implement this method in the specific trace-source. This method will called each time a new source should be opened.
	 * @return - the line that was read from the source
	 * @throws TraceSourceException
	 */
	protected abstract void openImpl( ) throws TraceSourceException;

	/**
	 * Implement this method in the specific trace-source. This method will called each time the source should be closed.
	 * @return - the line that was read from the source
	 * @throws TraceSourceException
	 */
	protected abstract void closeImpl( ) throws TraceSourceException;

	/**
	 * Open the {@link LogStreamReader}.
	 * @throws TraceSourceException
	 */
	public void open( ) throws TraceSourceException
	{
		// already open
		if ( this.opened.get( ) )
		{
			throw new TraceSourceException( "Already open" );
		}// if ( this.opened.get( ) ) .

		try
		{
			// delegate opening to the specific trace-source
			this.openImpl( );
			this.opened.set( true );
		}
		catch ( TraceSourceException e )
		{
			LOG( ).severe( "Unable to open: " + e.getLocalizedMessage( ) );
			throw e;
		}// catch ( TraceSourceException e ) .
	}

	/**
	 * Returns true if the {@link LogStreamReader} is open, false otherwise.
	 * @return
	 */
	public boolean isOpen( )
	{
		return this.opened.get( );
	}

	/**
	 * Access to the internal logger.
	 * @return
	 */
	protected Logger LOG( )
	{
		return this.log;
	}
}