/*
 *  Copyright (C) 2014, Thomas Obenaus. All rights reserved.
 *  Licensed under the New BSD License (3-clause lic)
 *  See attached license-file.
 *
 *	Author: 	Thomas Obenaus
 *	EMail:		obenaus.thomas@gmail.com
 *  Project:    LogFileViewer
 */

package thobe.logfileviewer.kernel.source.logstream;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import thobe.logfileviewer.kernel.source.err.LogStreamException;
import thobe.logfileviewer.kernel.source.extreader.ExternalLogStreamReader;
import thobe.logfileviewer.plugin.source.logstream.IInternalLogStreamReaderListener;

/**
 * Class that is responsible to obtain contents read from the log via {@link ExternalLogStreamReader} in a separate Thread. The data will
 * published to all {@link IInternalLogStreamReaderListener}s.
 * @author Thomas Obenaus
 * @source InternalLogStreamReader.java
 * @date May 29, 2014
 */
public class InternalLogStreamReader extends Thread
{
	/**
	 * Reference to an open stream-reader.
	 */
	private ExternalLogStreamReader					traceSource;

	/**
	 * Flag for requesting to quit this thread
	 */
	private AtomicBoolean							quitRequested;

	/**
	 * Logger
	 */
	private Logger									log;

	/**
	 * Listeners registered for data from logstream.
	 */
	private List<IInternalLogStreamReaderListener>	listeners;

	/**
	 * Current state of the logstream
	 */
	private LogStreamReaderState					stateOfLogStream;

	/**
	 * The time to sleep between two lines that where read from the source (in ms).
	 */
	private AtomicInteger							sleepTime;

	public InternalLogStreamReader( )
	{
		super( "thobe.logfileviewer.source.LogStreamContentPublisher" );
		this.quitRequested = new AtomicBoolean( false );
		this.log = Logger.getLogger( "thobe.logfileviewer.source.InternalLogStreamReader" );
		this.sleepTime = new AtomicInteger( 100 );
		this.listeners = new ArrayList<>( );
		this.stateOfLogStream = LogStreamReaderState.CLOSED;
	}

	/**
	 * Adds a new listener (thread-safe)
	 * @param listener
	 */
	void addListener( IInternalLogStreamReaderListener listener )
	{
		synchronized ( listeners )
		{
			this.listeners.add( listener );
		}
	}

	/**
	 * Removes the given listener (thread-safe)
	 * @param listener
	 */
	void removeListener( IInternalLogStreamReaderListener listener )
	{
		synchronized ( listeners )
		{
			this.listeners.remove( listener );
		}
	}

	public void startPublishing( ExternalLogStreamReader traceSource )
	{
		synchronized ( this )
		{
			this.traceSource = traceSource;
		}
	}

	public void stopPublishing( )
	{
		synchronized ( this )
		{
			this.traceSource = null;
		}
	}

	@Override
	public void run( )
	{

		LOG( ).info( "Started " + this.getClass( ).getSimpleName( ) );

		while ( !this.quitRequested.get( ) )
		{
			synchronized ( this )
			{
				// check state of stream
				LogStreamReaderState state = LogStreamReaderState.CLOSED;
				if ( this.traceSource != null )
				{
					state = this.traceSource.getCurrentState( );
				}

				// publish event if necessary
				this.publishState( state );

				// process new line 
				if ( state == LogStreamReaderState.OPEN && traceSource.hasNextLine( ) )
				{
					try
					{
						List<String> nextBlock = this.traceSource.nextLines( );
						if ( !nextBlock.isEmpty( ) )
						{
							this.fireNewBlock( nextBlock );
						}// if ( !nextBlock.isEmpty( ) ) .
					}
					catch ( LogStreamException e )
					{
						LOG( ).severe( "Unable to obtain next line from log: " + e.getLocalizedMessage( ) );
					}
				}// if ( state == LogStreamState.OPEN && traceSource.hasNextLine( ) ) .
			}// synchronized ( this ) .

			try
			{
				Thread.sleep( this.sleepTime.get( ) );
			}
			catch ( InterruptedException e )
			{
				LOG( ).info( "" + this.getClass( ).getSimpleName( ) + " interrupted: " + e.getLocalizedMessage( ) );
				break;
			}
		}

		LOG( ).info( "" + this.getClass( ).getSimpleName( ) + " stopped." );
	}

	private synchronized void publishState( LogStreamReaderState state )
	{
		if ( state != this.stateOfLogStream )
		{
			this.stateOfLogStream = state;
			switch ( this.stateOfLogStream )
			{
			case CLOSED:
				this.fireClosed( );
				break;
			case OPEN:
				this.fireOpened( );
				break;
			default:
			case EOF_REACHED:
				this.fireEOFReached( );
				break;
			}// switch ( this.stateOfLogStream ).
		}// if ( state != this.stateOfLogStream ).
	}

	/**
	 * Sets the time to sleep between publishing new contents read from the log (in ms).
	 * @param sleepTime
	 */
	public void setSleepTime( int sleepTime )
	{
		this.sleepTime.set( sleepTime );
	}

	public void kill( ) throws LogStreamException
	{
		this.quitRequested.set( true );
	}

	protected Logger LOG( )
	{
		return this.log;
	}

	private void fireNewBlock( List<String> newBlock )
	{
		synchronized ( listeners )
		{
			for ( IInternalLogStreamReaderListener l : this.listeners )
			{
				long elapsedTime = System.currentTimeMillis( );
				l.onNewBlock( newBlock );
				elapsedTime = System.currentTimeMillis( ) - elapsedTime;

				if ( elapsedTime > 100 )
				{
					LOG( ).warning( "Listener '" + l.getLogStreamListenerName( ) + "' needs " + ( elapsedTime / 1000.0f ) + "s to process the new-block event." );
				}// if ( elapsedTime > 100 ).
			}// for ( LogStreamContentPublisherListener l : this.listeners ).
		}// synchronized ( listeners ).
	}

	private void fireEOFReached( )
	{
		LOG( ).info( "LogStream eof reached" );
		synchronized ( listeners )
		{
			for ( IInternalLogStreamReaderListener l : this.listeners )
			{
				long elapsedTime = System.currentTimeMillis( );
				l.onEOFReached( );
				elapsedTime = System.currentTimeMillis( ) - elapsedTime;

				if ( elapsedTime > 100 )
				{
					LOG( ).warning( "Listener '" + l.getLogStreamListenerName( ) + "' needs " + ( elapsedTime / 1000.0f ) + "s to process the EOF-reached event." );
				}// if ( elapsedTime > 100 ).
			}// for ( LogStreamContentPublisherListener l : this.listeners ).
		}// synchronized ( listeners ).
	}

	private void fireOpened( )
	{
		LOG( ).info( "LogStream opened" );
		synchronized ( listeners )
		{
			for ( IInternalLogStreamReaderListener l : this.listeners )
			{
				long elapsedTime = System.currentTimeMillis( );
				l.onOpened( );
				elapsedTime = System.currentTimeMillis( ) - elapsedTime;

				if ( elapsedTime > 100 )
				{
					LOG( ).warning( "Listener '" + l.getLogStreamListenerName( ) + "' needs " + ( elapsedTime / 1000.0f ) + "s to process the opened event." );
				}// if ( elapsedTime > 100 ).
			}// for ( LogStreamContentPublisherListener l : this.listeners ).
		}// synchronized ( listeners ).
	}

	private void fireClosed( )
	{
		LOG( ).info( "LogStream closed" );
		synchronized ( listeners )
		{
			for ( IInternalLogStreamReaderListener l : this.listeners )
			{
				long elapsedTime = System.currentTimeMillis( );
				l.onClosed( );
				elapsedTime = System.currentTimeMillis( ) - elapsedTime;

				if ( elapsedTime > 100 )
				{
					LOG( ).warning( "Listener '" + l.getLogStreamListenerName( ) + "' needs " + ( elapsedTime / 1000.0f ) + "s to process the closed event." );
				}// if ( elapsedTime > 100 ).
			}// for ( LogStreamContentPublisherListener l : this.listeners ).
		}// synchronized ( listeners ).
	}

}
