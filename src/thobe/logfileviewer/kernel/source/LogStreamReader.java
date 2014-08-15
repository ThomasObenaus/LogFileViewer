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
import java.util.Deque;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import thobe.logfileviewer.kernel.source.err.LogStreamException;
import thobe.logfileviewer.kernel.source.err.LogStreamTimeoutException;

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
	 * If true the Thread terminates on reaching eof, otherwise the thread goes on reading.
	 */
	private boolean			stopOnReachingEOF;

	/**
	 * The internal logger
	 */
	private Logger			log;

	/**
	 * Size of one block (in case block-mode is used)
	 */
	private int				maxBlockSize;

	/**
	 * Max time (in ms) one read should block.
	 */
	private int				maxBlockTime;

	/**
	 * If true, block-mode instead of line-mode is used.
	 */
	private boolean			blockModeEnabled;

	/**
	 * Number of lines read since opening the source
	 */
	private long			numLinesRead;

	/**
	 * The time at which the source was opened.
	 */
	private long			timeStampOfOpeningSource;

	/**
	 * Ctor
	 */
	public LogStreamReader( )
	{
		this.lineBuffer = new ConcurrentLinkedDeque<>( );
		this.quitRequested = new AtomicBoolean( false );
		this.opened = new AtomicBoolean( false );
		this.EOFReached = new AtomicBoolean( false );
		this.sleepTime = new AtomicInteger( 0 );
		this.stopOnReachingEOF = true;
		this.log = Logger.getLogger( "thobe.logfileviewer.source.LogStreamReader" );
		this.maxBlockSize = 100;
		this.maxBlockTime = 2000;
		this.blockModeEnabled = true;
		this.numLinesRead = 0;
		this.timeStampOfOpeningSource = 0;
	}

	/**
	 * Returns the next line that is available (was read from the log source). On accessing this method the line will be consumed, that
	 * means further calls to this method will result in different results (the next line).
	 * @return - the next line as {@link String}
	 * @throws LogStreamException - Thrown if no more lines are available. Please use {@link LogStreamReader#hasNextLine()} to check if
	 *             more
	 *             lines are available.
	 */
	public String nextLine( ) throws LogStreamException
	{
		String result = null;
		synchronized ( this.lineBuffer )
		{
			if ( !this.hasNextLine( ) )
				throw new LogStreamException( "The queue is empty" );
			result = this.lineBuffer.pop( );
		}// synchronized ( this.lineBuffer ) .
		return result;
	}

	/**
	 * Returns the next lines that are available (was read from the log source). On accessing this method all lines will be consumed, that
	 * means further calls to this method will result in different results (the next lines).
	 * @return - the next lines as list of {@link String}'s
	 * @throws LogStreamException - Thrown if no more lines are available. Please use {@link LogStreamReader#hasNextLine()} to check if more
	 *             lines are available.
	 */
	public List<String> nextLines( ) throws LogStreamException
	{
		List<String> block = null;
		synchronized ( this.lineBuffer )
		{
			if ( !this.hasNextLine( ) )
				throw new LogStreamException( "The queue is empty" );

			block = new ArrayList<>( this.lineBuffer );
			this.lineBuffer.clear( );
		}// synchronized ( this.lineBuffer ) .		
		return block;
	}

	/**
	 * Returns true if at least one more line is available, false otherwise.
	 * @return
	 */
	public boolean hasNextLine( )
	{
		return !this.lineBuffer.isEmpty( );
	}

	/**
	 * Setting this flag to false the {@link LogStreamReader} continues reading even if the end of file was reached, assuming more lines
	 * will be added to the file soon (like tail -f <file>). Setting this flag to true the {@link LogStreamReader} will terminate
	 * immediately if the end of the file is reached.
	 * @param stopOnReachingEOF
	 */
	public void setStopOnReachingEOF( boolean stopOnReachingEOF )
	{
		this.stopOnReachingEOF = stopOnReachingEOF;
	}

	@Override
	public void run( )
	{
		LOG( ).info( "Thread: " + this.getClass( ).getSimpleName( ) + " started." );

		// Main-loop, will only terminate if close was called or if eof-was reached
		while ( !this.quitRequested.get( ) && ( !this.EOFReached.get( ) || !this.stopOnReachingEOF ) )
		{
			// directly interrupt reading from the source if the source is not open
			if ( !isOpen( ) )
			{
				LOG( ).info( this.getClass( ).getSimpleName( ) + " interrupted: Source is not open." );
				break;
			}// if ( !isOpen( ) ) .

			try
			{
				boolean somethingAdded = false;

				// BLOCK-MODE
				if ( this.isBlockModeEnabled( ) )
				{
					// delegate the reading to the specific source-implementation
					List<String> block = readBlockImpl( 200, this.maxBlockTime, 10, this.maxBlockSize );

					if ( !block.isEmpty( ) )
					{
						somethingAdded = true;
						// add the lines to the buffer
						this.lineBuffer.addAll( block );
						this.numLinesRead += block.size( );
					}// if ( !block.isEmpty( ) ) .
				}// if ( this.isBlockModeEnabled( ) ) .				
				else
				{
					// LINE-MODE
					// delegate the reading to the specific source-implementation
					String newLine = readLineImpl( this.maxBlockTime );
					if ( newLine != null )
					{
						somethingAdded = true;
						// add the line to the buffer
						this.lineBuffer.add( newLine );
						this.numLinesRead++;
					}// if ( newLine != null ) .
				}

				// set EOF if nothing was read/added
				if ( !somethingAdded )
				{
					// end of file reached
					this.EOFReached.set( true );
					LOG( ).info( "End of file reached" );
				}// if ( newLine != null ) ... else ...
			}
			catch ( LogStreamTimeoutException e )
			{
				// The call to readLineImpl() timed out --> EOF
				LOG( ).warning( this.getClass( ).getSimpleName( ) + " error reading next line: '" + e.getLocalizedMessage( ) + ( this.stopOnReachingEOF ? "'. stop reading." : "'. continue reading" ) );
				this.EOFReached.set( true );
			}//catch ( LogStreamTimeoutException e ) .
			catch ( LogStreamException e )
			{
				LOG( ).warning( this.getClass( ).getSimpleName( ) + " error reading next line: '" + e.getLocalizedMessage( ) + "'. stop reading." );
				this.opened.set( false );
				break;
			}// catch ( LogStreamException e ) .

			// sleep only if we don't have already reached the EOF or if we don't want to stop at the end of file
			if ( !this.EOFReached.get( ) || !this.stopOnReachingEOF )
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
			}// if ( !this.EOFReached.get( ) || !this.stopOnReachingEOF ) .
		}// while ( !this.quitRequested.get( ) && ( !this.EOFReached.get( ) || !this.stopOnReachingEOF ) ).

		// close the LogStreamReader in case of EOF reading should be stopped  
		if ( this.EOFReached.get( ) && this.stopOnReachingEOF )
		{
			this.opened.set( false );
		}
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

	/**
	 * Returns the lines per second read from the opened source
	 * @return
	 */
	public double getLinesPerSecond( )
	{
		if ( !this.isOpen( ) )
			return 0;
		long elapsed = System.currentTimeMillis( ) - this.timeStampOfOpeningSource;
		return ( this.numLinesRead / ( elapsed / 1000.0d ) );
	}

	public boolean isEOFReached( )
	{
		return EOFReached.get( );
	}

	public void close( ) throws LogStreamException
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
				this.numLinesRead = 0;
				this.timeStampOfOpeningSource = 0;
			}
			catch ( LogStreamException e )
			{
				LOG( ).severe( "Failed to close: " + e.getLocalizedMessage( ) );
				throw e;
			}
		}// if ( this.opened.get( ) ).
		LOG( ).info( "TraceSource [" + this.getClass( ).getSimpleName( ) + "] closed." );
	}

	/**
	 * Implement this method in the specific trace-source. This method will called each time a new line should be read from the source.
	 * @param maxBlockTime - max time in ms the method should block (need to return)
	 * @return - the line that was read from the source
	 * @throws LogStreamException
	 * @throws LogStreamTimeoutException
	 */
	protected abstract String readLineImpl( int maxBlockTime ) throws LogStreamException, LogStreamTimeoutException;

	/**
	 * Implement this method in the specific trace-source. This method will called each time a new block of lines should be read from the
	 * source.
	 * @param maxBlockTime - max time in ms the method should block (need to return)
	 * @param maxBlockSize - max number of lines the block should contain
	 * @return - list of lines (the block) read from the source
	 * @throws LogStreamException
	 * @throws LogStreamTimeoutException
	 */
	protected abstract List<String> readBlockImpl( int minBlockTime, int maxBlockTime, int minBlockSize, int maxBlockSize ) throws LogStreamException, LogStreamTimeoutException;

	/**
	 * Implement this method in the specific trace-source. This method will called each time a new source should be opened.
	 * @param timeout - max time (in ms) this method will be blocked until an {@link LogStreamException} is thrown.
	 * @return - the line that was read from the source
	 * @throws LogStreamException
	 */
	protected abstract void openImpl( int timeout ) throws LogStreamException;

	/**
	 * Implement this method in the specific trace-source. This method will called each time the source should be closed.
	 * @return - the line that was read from the source
	 * @throws LogStreamException
	 */
	protected abstract void closeImpl( ) throws LogStreamException;

	/**
	 * Open the {@link LogStreamReader}.
	 * @throws LogStreamException
	 */
	public void open( ) throws LogStreamException
	{
		// already open
		if ( this.opened.get( ) )
		{
			throw new LogStreamException( "Already open" );
		}// if ( this.opened.get( ) ) .

		try
		{
			this.numLinesRead = 0;
			this.timeStampOfOpeningSource = System.currentTimeMillis( );

			// delegate opening to the specific trace-source
			this.openImpl( 2000 );
			this.opened.set( true );
		}
		catch ( LogStreamException e )
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

	public int getMaxBlockSize( )
	{
		return maxBlockSize;
	}

	public int getMaxBlockTime( )
	{
		return maxBlockTime;
	}

	public void setMaxBlockSize( int maxBlockSize )
	{
		this.maxBlockSize = maxBlockSize;
	}

	public void setMaxBlockTime( int maxBlockTime )
	{
		this.maxBlockTime = maxBlockTime;
	}

	public boolean isBlockModeEnabled( )
	{
		return blockModeEnabled;
	}

	public void setBlockModeEnabled( boolean blockModeEnabled )
	{
		this.blockModeEnabled = blockModeEnabled;
	}
}
